package edu.cs.ucdavis.decal;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

/**
 * Collect all ASTs and filenames
 * @author jcwu
 *
 */
public class AnnotationASTRequestor extends FileASTRequestor {

	private OmniController controller;

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		controller.addCompilationUnitFileName(ast, sourceFilePath);
	}

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setRequestor(this);
	}
}