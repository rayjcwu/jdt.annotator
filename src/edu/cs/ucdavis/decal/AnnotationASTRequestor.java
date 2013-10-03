package edu.cs.ucdavis.decal;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class AnnotationASTRequestor extends FileASTRequestor {

	private OmniController controller;

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		// System.out.println("\n================ " + sourceFilePath);
		//controller.setCurrentFileName(sourceFilePath);
		controller.retriveCurrentFileNameId(sourceFilePath);
		controller.addCompilationUnitFileName(ast, sourceFilePath);
		controller.getVisitor().setCurrentCompilationUnit(ast);
		ast.accept(controller.getVisitor());
	}

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setRequestor(this);
	}
}