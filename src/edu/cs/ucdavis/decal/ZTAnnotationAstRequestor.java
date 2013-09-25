package edu.cs.ucdavis.decal;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class ZTAnnotationAstRequestor extends FileASTRequestor {

	ZTASTVisitor visitor;
	
	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		ast.accept(visitor);		
	}
	
	public void setVisitor(ZTASTVisitor visitor) {
		this.visitor = visitor;
	}
}
