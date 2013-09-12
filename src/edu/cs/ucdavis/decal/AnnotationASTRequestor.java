package edu.cs.ucdavis.decal;

import java.util.Collection;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class AnnotationASTRequestor extends FileASTRequestor {

	DumpAstVisitor visitor;
		
	public AnnotationASTRequestor() {
		this(null);
	}
	
	public AnnotationASTRequestor(Collection<CompilationUnit> units) {
		visitor = new DumpAstVisitor();
		visitor.setCurrentCompilationUnits(units);
	}
	
	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		super.acceptAST(sourceFilePath, ast);
		System.out.println("\n================ " + sourceFilePath);
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
