package edu.cs.ucdavis.decal;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class AnnotationASTRequestor extends FileASTRequestor {

	DumpAstVisitor visitor;

	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		super.acceptAST(sourceFilePath, ast);
		System.out.println("\n================ " + sourceFilePath);
		ast.accept(visitor);
	}

	public AnnotationASTRequestor setVisitor(DumpAstVisitor visitor) {
		this.visitor = visitor;
		return this;
	}
	
	public DumpAstVisitor getVisitor() {
		return visitor;
	}
}
