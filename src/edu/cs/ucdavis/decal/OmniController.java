package edu.cs.ucdavis.decal;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;

public class OmniController {
	private DumpAstVisitor visitor;
	private AnnotationASTRequestor requestor;
	private IDatabaseStorer database;
	private Logger logger;
	
	private String projectName;
	private int projectId;
	private int currentFileId;
	
	private String sourcePath;
	
	public OmniController(String sourcePath) {
		this.visitor = null;
		this.requestor = null;
		this.database = null;
		this.logger = null;
		this.sourcePath = sourcePath;
		
		this.projectId = -1;
		this.currentFileId = -1;
	}
	
	private ASTParser getParser() {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);		
		parser.setEnvironment(null, null, null, true);
		parser.setUnitName("test");  // seems you should set unit name for whatever you want
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return parser;
	}
	
	private void init() {
		// init database
		if (database instanceof PostgreSQLStorer) {
			PostgreSQLStorer db = (PostgreSQLStorer)database;
			if (!db.isReady()) {
				db.init();
			}
		}
		// init visitor
	}
	
	public void run() {
		init();
		
		String[] sourceFilePaths = collectFilePaths();
		ASTParser parser = getParser();
		
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
		
		// check visitor
		/*
		System.out.println("print binding keys");
		
		for (String bindingKey: visitor.getBindingKeys()) {
			System.out.println(bindingKey);
			for (CompilationUnit cu: visitor.getCompilatoinUnits()) {
				ASTNode node = cu.findDeclaringNode(bindingKey);
				if (node != null) {
					System.out.println(node);
					break;
				}
			}
		}
		*/		
	}

	@SuppressWarnings("unchecked")
	private String[] collectFilePaths() {
		Collection <File> filePaths = (Collection <File>) FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true);
		String [] sourceFilePaths = new String[filePaths.size()];
		int i = 0;
		for (File f: filePaths) {
			sourceFilePaths[i] = f.toString();
			i++;
		}
		return sourceFilePaths;
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

	public IDatabaseStorer getDatabase() {
		return database;
	}

	public OmniController setDatabase(IDatabaseStorer database) {
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
		currentFileId = id;
	}
	
	public void saveAstNodeInfo(ASTNode node, CompilationUnit unit) {
		/*
		String string = node.toString();

		node.getLocationInParent(),
		
		node.subtreeBytes()
		*/
		String string = node.toString();
		int nodetype_id = node.getNodeType();
		int start_pos = node.getStartPosition();
		int length = node.getLength();
		int line_number = unit.getLineNumber(node.getStartPosition());
		
		String binding_key = "";
		if (node instanceof Name) {
			Name n = (Name)node;
			binding_key = n.resolveBinding().getKey();
		}
 
		database.saveAstNodeInfo(start_pos, length, line_number, nodetype_id, binding_key, string, currentFileId);
	}
}