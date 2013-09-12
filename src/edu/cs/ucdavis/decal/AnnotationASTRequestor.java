package edu.cs.ucdavis.decal;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class AnnotationASTRequestor extends FileASTRequestor {

	DumpAstVisitor visitor;
	
	public AnnotationASTRequestor() {
		visitor = new DumpAstVisitor();
	}
	
	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		super.acceptAST(sourceFilePath, ast);
		System.out.println("\n================ " + sourceFilePath);
	//	visitor.setCompilationUnit(ast);
		ast.accept(visitor);
	}

	/*
	@Override
	public void acceptBinding(String bindingKey, IBinding binding) {
		// TODO Auto-generated method stub
		super.acceptBinding(bindingKey, binding);
	}
	*/

}
