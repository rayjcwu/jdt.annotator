package edu.cs.ucdavis.decal;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class OmniController {
	private DumpAstVisitor visitor;
	private AnnotationASTRequestor requestor;
	private IDatabaseStorer database;
	private Logger logger;
	
	private String projectName;
	private String currentFileName;
	
	private String sourcePath;
	
	public OmniController(String sourcePath) {
		this.visitor = null;
		this.requestor = null;
		this.database = null;
		this.logger = null;
		this.projectName = "fake_project";
		this.currentFileName = "";
		this.sourcePath = sourcePath;
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
		String[] sourceFilePaths = collectFilePaths();
		ASTParser parser = getParser();

		init();		
		
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
		
		// check visitor
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
	
	public void saveAstNode(ASTNode node) {
		// TODO: save node information to database
	}
	
	// getter/setter
	public OmniController setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
		return this;
	}
	
	public String getCurrentFileName() {
		return currentFileName;
	}
	
	public OmniController setProjectName(String projectName) {
		this.projectName = projectName;
		return this;
	}
	
	public String getProjectName() {
		return projectName;
	}

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

	public void retriveCurrentFileNameId(String sourceFilePath) {
		
		
	}	
}