package edu.cs.ucdavis.decal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;

public class OmniController {
	private DumpAstVisitor visitor;
	private AnnotationASTRequestor requestor;
	private PostgreSQLStorer database;
	private Logger logger;

	private String projectName;
	private int projectId;
	private int currentFileId;
	private String currentFileRaw;

	private String sourcePath;
	private String libPath;

	private Map <CompilationUnit, String> compilaionUnitFileNameMap;
	private Collection <String> bindingKeys;
	private int projectSize;
	private List <Token> tokens;
	private int totalTokens;

	public OmniController(String sourcePath) {
		this.visitor = null;
		this.requestor = null;
		this.database = null;
		this.logger = null;
		this.sourcePath = sourcePath;

		this.projectName = "";
		this.projectId = -1;
		this.currentFileId = -1;
		this.currentFileRaw = "";
		this.projectSize = -1;

		this.compilaionUnitFileNameMap = new HashMap<CompilationUnit, String>();
		this.bindingKeys = new HashSet<String>();
	}

	public OmniController addBindingKey(String bindingKey) {
		bindingKeys.add(bindingKey);
		return this;
	}

	public Collection <String> getBindingKeys() {
		return bindingKeys;
	}

	public OmniController addCompilationUnitFileName(CompilationUnit unit, String fileName) {
		compilaionUnitFileNameMap.put(unit, fileName);
		return this;
	}

	public String getCompilationUnitFileName(CompilationUnit unit) {
		return compilaionUnitFileNameMap.get(unit);
	}

	private ASTParser getParser() {

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		String []classpath = Util.getClasspath();
		if (this.libPath == null || this.libPath.equals("")) {
			parser.setEnvironment(classpath, null, null, true);
		} else {
			String []libPath = Util.collectFilePaths(this.libPath, new String[] {"jar"});
			String []combinePath = Util.concat(libPath, classpath);
			parser.setEnvironment(combinePath, null, null, true);
		}
		parser.setUnitName("test");  // seems you should set unit name for whatever you want
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return parser;
	}

	private void init() {
		// init database
		// init visitor
	}

	public void run() {
		init();

		String[] sourceFilePaths = Util.collectFilePaths(this.sourcePath, new String[] {"java"});
		projectSize = sourceFilePaths.length;
		ASTParser parser = getParser();

		System.out.println("Annotating .java files...");
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);

		System.out.println("Resolving bindings...");
		resolveBindings();
	}

	private void resolveBindings() {
		int bindingSize = bindingKeys.size();
		int i = 1;
		boolean resolved = false;
		for (String bindingKey: bindingKeys) {
			System.out.print(String.format("(%d/%d) binding", i, bindingSize));
			i++;
			for (Map.Entry<CompilationUnit, String> entry: compilaionUnitFileNameMap.entrySet()) {
				CompilationUnit unit = entry.getKey();
				ASTNode node = unit.findDeclaringNode(bindingKey);
				if (node != null) {
					retriveCurrentFileNameId(entry.getValue());  // set current file to corresponding id
					saveForeignAstNode(node, bindingKey);

					System.out.println(" o " + bindingKey);
					resolved = true;
					break;
				}
			}
			if (!resolved) {
				System.out.println(" x " + bindingKey);
			}
			resolved = false;
		}
	}

	private void saveForeignAstNode(ASTNode node, String bindingKey) {
		int start_pos = node.getStartPosition();
		int length = node.getLength();
		int nodetype_id = node.getNodeType();
		database.saveForeignAstNode(start_pos, length, nodetype_id, bindingKey, currentFileId);
	}

	// getter/setter
	public DumpAstVisitor getVisitor() {
		return visitor;
	}

	public OmniController setVisitor(DumpAstVisitor visitor) {
		this.visitor = visitor;
		return this;
	}

	public AnnotationASTRequestor getRequestor() {
		return requestor;
	}

	public OmniController setRequestor(AnnotationASTRequestor requestor) {
		this.requestor = requestor;
		return this;
	}

	public PostgreSQLStorer getDatabase() {
		return database;
	}

	public OmniController setDatabase(PostgreSQLStorer database) {
		this.database = database;
		return this;
	}

	public OmniController setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
		retriveProjectId(projectName, sourcePath);
	}

	public void retriveProjectId(String projectName, String sourcePath) {
		int id = database.retrieveProjectId(projectName, sourcePath);
		if (id == -1) {
			throw new IllegalStateException("retrieve project id error");
		}
		projectId = id;
	}

	public void retriveCurrentFileNameId(String sourceFilePath) {
		int id = database.retrieveFileId(sourceFilePath, projectId);
		if (id == -1) {
			throw new IllegalStateException("retrieve file id error");
		}
		try {
			currentFileRaw = FileUtils.readFileToString(new File(sourceFilePath));
			prepareTokens();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Read file content error", e);
		}
		currentFileId = id;
	}

	private void prepareTokens() {
		ANTLRInputStream input = new ANTLRInputStream(currentFileRaw);
		JavaLexer lexel = new JavaLexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexel);
		tokenStream.fill();
		List <Token> trimedTokens = new ArrayList<Token>();
		for (Token token: tokenStream.getTokens()) {
			if (!(token.getType() == JavaLexer.IDENTIFIER ||
				  token.getType() == JavaLexer.NULL_LITERAL ||
				  token.getType() == JavaLexer.BOOLEAN_LITERAL ||
				  token.getType() == JavaLexer.STRING_LITERAL ||
				  token.getType() == JavaLexer.CHARACTER_LITERAL )) {
				trimedTokens.add(token);
			}
		}
		this.tokens = trimedTokens;
		this.totalTokens = this.tokens.size();
	}

	public void saveAstNodeInfo(ASTNode node, CompilationUnit unit) {

		final String string = node.toString();
		final int nodetype_id = node.getNodeType();
		final int start_pos = node.getStartPosition();
		final int length = node.getLength();

		final int start_line_number = unit.getLineNumber(start_pos);
		final int start_column_number = unit.getColumnNumber(start_pos);

		final int end_line_number = unit.getLineNumber(start_pos + length);
		final int end_column_number = unit.getColumnNumber(start_pos + length);


		String cross_ref_key = "";
		if (node instanceof Name) {
			Name n = (Name)node;
			if (n.resolveBinding() != null) {
				cross_ref_key = n.resolveBinding().getKey();
				bindingKeys.add(cross_ref_key);
			}
		}
		final int parentId = getAstnodeId(node.getParent());
		if (start_pos == -1) {
			throw new IllegalStateException("should not happen");
		}
		database.saveAstNodeInfo(start_pos, length,
				start_line_number, start_column_number,
				end_line_number, end_column_number,
				nodetype_id, cross_ref_key, string, currentFileId, currentFileRaw, parentId);
	}

	private int findFirstInInterval(int start_pos) {
		int lower_bound = 0;
		int upper_bound = this.totalTokens - 1;
		int mid = (lower_bound + upper_bound) / 2;

		while (upper_bound < lower_bound) {

			if (tokens.get(mid).getStartIndex() > start_pos) {
				lower_bound = mid;
			} else if (tokens.get(mid).getStartIndex() < start_pos) {
				upper_bound = mid;
			} else if (tokens.get(mid).getStartIndex() == start_pos) {
				break;
			}
			mid = (lower_bound + upper_bound) / 2;
		}

		int i = 0;
		for (i = mid; i >= 0; i--) {
			if (tokens.get(i).getStartIndex() <= start_pos) {
				break;
			}
		}
		return i;
	}

	private int findIndex(int start_pos) {
		int max = this.tokens.size() - 1;
		int min = 0;

		int mid = (max + min)/2;
		while (max > min) {
			mid = (max + min)/2;
			if (this.tokens.get(mid).getStartIndex() > start_pos) {
				max = mid;
			} else if (this.tokens.get(mid).getStartIndex() < start_pos) {
				min = mid + 1;
			} else {
				return mid;
			}
		}

		// not found
		while (mid > 0) {
			if (this.tokens.get(mid).getStartIndex() < start_pos){
				break;
			}
			mid --;
		}
		return mid;
	}

	// TODO: interval tree to find out tokens?
	public void saveTokenInfo(ASTNode node, CompilationUnit unit) {
		final int node_start_pos = node.getStartPosition();
		final int node_end_pos = node.getStartPosition() + node.getLength();

		final int parentId = getAstnodeId(node); // token's parent is current id

		String tokenStatus = String.format("%d/%d", totalTokens - this.tokens.size(), totalTokens);
		String bar = String.format("%2.2f%%", (1-(float)this.tokens.size()/totalTokens)*100);
		System.out.print(tokenStatus + " " + bar + "\r");

		List <Integer> token_to_remove = new LinkedList <Integer> ();
		for(int i = findIndex(node_start_pos); i < tokens.size(); i++) {
			Token token = tokens.get(i);
			final String string = token.getText();

			final int token_start_pos = token.getStartIndex();
			final int token_end_pos = token_start_pos + string.length();

			final int start_line_number = token.getLine();
			final int start_column_number = token.getCharPositionInLine();

			final int end_line_number = unit.getLineNumber(token_end_pos);
			final int end_column_number = unit.getColumnNumber(token_end_pos);


			final int nodetype_id = token.getType() + PostgreSQLStorer.tokenBase;

			final int file_id = this.currentFileId;
			if (node_start_pos <= token_start_pos && token_end_pos <= node_end_pos) {  // if token is inside of node interval
				if (token.getType() != -1 ) {
					database.saveTokenInfo(token_start_pos, string.length(),
							start_line_number, start_column_number,
							end_line_number, end_column_number,
							nodetype_id, string, file_id, currentFileRaw, parentId);
					token_to_remove.add(i);
				}
				if (token_start_pos > currentFileRaw.length() || nodetype_id == 99) {
					System.err.println("Should not happend: " + token.toString());
				}
			} else if (token_start_pos > node_end_pos){
				break;
			}
		}

		// remove from tokens
		for (int i = 0; i < token_to_remove.size(); i++) {
			tokens.remove(token_to_remove.get(i) - i);
		}
	}

//	public void saveTokenInfo(CompilationUnit unit, int start_pos, // to figure out
//			Token token, int parentId) {
//		/*
//		 * start_pos, length, line_number, nodetype_id, file_id, string, raw, parent_astnode_id
//		 */
//		final int line_number = unit.getLineNumber(start_pos);    // token's start_pos not node's start_pos
//		final int column_number = unit.getColumnNumber(start_pos);
//		final int length = token.getLength();
//		final int nodetype_id = token.getId();
//		final String string = token.getToken();
//
//		database.saveTokenInfo(start_pos, length, line_number, column_number, nodetype_id, string, currentFileId, currentFileRaw, parentId);
//	}

	public int getAstnodeId(ASTNode node) {
		if (node == null) { return -1; }

		final int start_pos = node.getStartPosition();
		final int length = node.getLength();
		final int nodetype = node.getNodeType();
		final int result = database.queryAstNodeId(start_pos, length, nodetype, currentFileId);
		if (!(node instanceof CompilationUnit) && result == -1) {
			throw new IllegalStateException("should not happen");
		}
		return result;
	}

	private static int progressCount = 0;
	public void showProgress(String sourceFilePath) {
		OmniController.progressCount ++ ;
		System.out.println(String.format("(%d/%d) %s", OmniController.progressCount, projectSize, sourceFilePath));
	}

	public void clearProjectAstNodeInfo() {
		database.clearProjectAstnode(projectId);
	}

	public String getCurrentFileRaw() {
		return currentFileRaw;
	}

	public OmniController setLibPath(String libPath) {
		this.libPath = libPath;
		return this;
	}
}