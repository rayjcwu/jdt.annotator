package edu.cs.ucdavis.decal;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.antlr.v4.runtime.Token;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;

public class OmniController extends BaseController {
	private int currentProjectId;
	private int currentFileId;
	private String currentFileContent;

	private Map <CompilationUnit, String> compilaionUnitFileNameMap;
	private Collection <String> crossRefKeys;
	private List <Token> tokens;

	public OmniController() {
		super();

		this.currentProjectId = -1;
		this.currentFileId = -1;
		this.currentFileContent = null;

		this.compilaionUnitFileNameMap = new HashMap<CompilationUnit, String>();
		this.crossRefKeys = new HashSet<String>();
		this.tokens = null;
	}

	public OmniController addCrossRefKey(String crossRefKey) {
		crossRefKeys.add(crossRefKey);
		return this;
	}

	public Collection <String> getCrossRefKeys() {
		return crossRefKeys;
	}

	public OmniController addCompilationUnitFileName(CompilationUnit unit, String fileName) {
		compilaionUnitFileNameMap.put(unit, fileName);
		return this;
	}

	public String getCompilationUnitFileName(CompilationUnit unit) {
		return compilaionUnitFileNameMap.get(unit);
	}

	void retriveProjectId(String projectName, String sourcePath) {
		int id = database.retrieveProjectId(projectName, sourcePath);
		if (id == -1) {
			throw new IllegalStateException("retrieve project id error");
		}
		currentProjectId = id;
	}

	public void retriveCurrentFileNameId(String sourceFilePath) {
		int id = database.retrieveFileId(sourceFilePath, currentProjectId);
		if (id == -1) {
			throw new IllegalStateException("retrieve file id error");
		}
		try {
			this.currentFileContent = FileUtils.readFileToString(new File(sourceFilePath));
			this.tokens = Util.prepareTokens(this.currentFileContent);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Read file content error", e);
		}
		currentFileId = id;
	}

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

	// main flow
	public void run() {
		clearProjectAstNodeInfo();

		System.out.println("Collecting ASTs...");
		collectAst();

		System.out.println("Annotating .java files...");
		annotateAst();

		System.out.println("Resolving cross reference...");
		resolveCrossReference();
	}


	void clearProjectAstNodeInfo() {
		retriveProjectId(this.projectName, this.sourcePath);
		database.clearProjectAstnode(currentProjectId);
	}

	void collectAst() {
		String[] sourceFilePaths = Util.collectFilePaths(this.sourcePath, new String[] {"java"});
		ASTParser parser = Util.getParser(this.libPath);
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
	}

	void annotateAst() {
		for (Map.Entry<CompilationUnit, String> cuEntry: this.compilaionUnitFileNameMap.entrySet()) {
			String sourceFilePath = cuEntry.getValue();
			CompilationUnit ast = cuEntry.getKey();

			this.showProgress(sourceFilePath);
			this.retriveCurrentFileNameId(sourceFilePath);
			this.getVisitor().setCurrentCompilationUnit(ast);
			ast.accept(this.getVisitor()); // save ast node information happend in here

			this.saveTokenInfo(ast);
		}
	}

	void resolveCrossReference() {
		int bindingSize = crossRefKeys.size();
		int i = 1;
		boolean resolved = false;
		for (String bindingKey: crossRefKeys) {
			System.out.print(String.format("(%d/%d) resolve cross reference", i, bindingSize));
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

	private int getAstnodeId(LookupVisitor lookup) {
		final int node_start_pos = lookup.getStartPos();
		final int nodetype = lookup.getNodetype();
		final int node_length = lookup.getLength();
		final int parentId = database.queryAstNodeId(node_start_pos, node_length, nodetype, this.currentFileId);
		return parentId;
	}

	private void saveForeignAstNode(ASTNode node, String bindingKey) {
		int start_pos = node.getStartPosition();
		int length = node.getLength();
		int nodetype_id = node.getNodeType();
		database.saveForeignAstNode(start_pos, length, nodetype_id, bindingKey, currentFileId);
	}

	public void saveAstNodeInfo(ASTNode node, CompilationUnit unit) {

		final String string = node.toString();  // code generated from AST node, not original source code
		final int nodetype_id = node.getNodeType();
		// offset starts from 0
		final int start_pos = node.getStartPosition();
		final int length = node.getLength();

		// both line/column number start from 1
		final int start_line_number = unit.getLineNumber(start_pos);
		final int start_column_number = unit.getColumnNumber(start_pos) + 1;

		final int end_line_number = unit.getLineNumber(start_pos + length - 1);
		final int end_column_number = unit.getColumnNumber(start_pos + length - 1) + 1;

		String crossRefKey = "";
		if (node instanceof Name) {
			Name n = (Name)node;
			if (n.resolveBinding() != null) {
				crossRefKey = n.resolveBinding().getKey();
				crossRefKeys.add(crossRefKey);
			}
		}
		final int parentId = getAstnodeId(node.getParent());
		if (start_pos == -1) {
			throw new IllegalStateException("should not happen");
		}
		database.saveAstNodeInfo(start_pos, length,
				start_line_number, start_column_number,
				end_line_number, end_column_number,
				nodetype_id, crossRefKey, string, currentFileId, currentFileContent, parentId);
	}

	public void saveTokenInfo(CompilationUnit unit) {

		LookupVisitor lookup = new LookupVisitor();
		final int totalTokens = tokens.size();
		for (int i = 0; i < tokens.size(); i++) {
			String tokenStatus = String.format("%d/%d", i+1, totalTokens);
			String bar = String.format("%2.2f%%", ((float)(i+1)/totalTokens)*100);
			System.out.print(tokenStatus + " " + bar + "\r");

			Token token = tokens.get(i);
			lookup.reset();
			lookup.setToken(token);
			unit.accept(lookup);  // look up token in current source code

			final int parentId = getAstnodeId(lookup);
			final String string = token.getText();

			final int token_start_pos = token.getStartIndex();
			final int token_end_pos = token.getStopIndex(); // inclusive end pos

			final int start_line_number = token.getLine();
			final int start_column_number = token.getCharPositionInLine() + 1;

			final int end_line_number = unit.getLineNumber(token_end_pos);
			final int end_column_number = unit.getColumnNumber(token_end_pos) + 1;

			final int nodetype_id = token.getType() + PostgreSQLStorer.tokenBase;

			final int file_id = this.currentFileId;

			database.saveTokenInfo(token_start_pos, token_end_pos - token_start_pos + 1,
					start_line_number, start_column_number,
					end_line_number, end_column_number,
					nodetype_id, string, file_id, currentFileContent, parentId);
		}
	}

	private static int progressCount = 0;
	public void showProgress(String sourceFilePath) {
		final int projectSize = compilaionUnitFileNameMap.size();
		OmniController.progressCount ++ ;
		System.out.println(String.format("(%d/%d) %s", OmniController.progressCount, projectSize, sourceFilePath));
	}

}