package edu.cs.ucdavis.decal;

import java.util.logging.Logger;

// data fields and getters/setters
public class BaseController {

	protected DumpAstVisitor visitor;
	protected AnnotationASTRequestor requestor;
	protected PostgreSQLStorer database;
	protected Logger logger;
	protected String projectName;
	protected String sourcePath;
	protected String libPath;

	public BaseController() {
		this.visitor = null;
		this.requestor = null;
		this.database = null;
		this.logger = null;
		this.sourcePath = null;
		this.projectName = null;
	}

	public DumpAstVisitor getVisitor() {
		return visitor;
	}

	public void setVisitor(DumpAstVisitor visitor) {
		this.visitor = visitor;
	}

	public AnnotationASTRequestor getRequestor() {
		return requestor;
	}

	public void setRequestor(AnnotationASTRequestor requestor) {
		this.requestor = requestor;
	}

	public PostgreSQLStorer getDatabase() {
		return database;
	}

	public void setDatabase(PostgreSQLStorer database) {
		this.database = database;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getLibPath() {
		return libPath;
	}

	public void setLibPath(String libPath) {
		this.libPath = libPath;
	}

}