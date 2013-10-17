package edu.cs.ucdavis.decal;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class AnnotationASTRequestor extends FileASTRequestor {

	private OmniController controller;

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		controller.showProgress(sourceFilePath);
		controller.retriveCurrentFileNameId(sourceFilePath);
		controller.addCompilationUnitFileName(ast, sourceFilePath);
		controller.getVisitor().setCurrentCompilationUnit(ast);
		ast.accept(controller.getVisitor()); // save ast node information happend in here

		controller.storeTokenInfo(ast);
	}

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setRequestor(this);
	}
}