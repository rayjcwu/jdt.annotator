package edu.cs.ucdavis.decal;

import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;

class ZTASTVisitor extends ASTVisitor {
	
	Stack <ASTNode> stack = new Stack<ASTNode>();
	int indent = 0;

	private void println(String s) {
		/*
		for (int i = 0; i < indent; i++) {
			System.out.print(" ");
		}
		*/		
		System.out.println(s);
	}
	
	private void print(String s) {
		/*
		for (int i = 0; i < indent; i++) {
			System.out.print(" ");
		}
		*/
		System.out.print(s);
	}
	
	@Override
	public boolean visit(CompilationUnit node) {
		println(node.toString());
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ReturnStatement node) {
		//println(getNodeInfo(node));   // Auto-generated
		print("return<=>KW_return ");
		return super.visit(node);
	}
	
	@Override
	public void endVisit(ReturnStatement node) {
		println(";<=>SEP_;");
	}
	
	@Override
	public boolean visit(Block node) {
		println("{<=>SEP_{ ");
		indent += 2;
		return super.visit(node);
	}
	
	@Override
	public void endVisit(Block node) {
		indent -= 2;
		println("}<=>SEP_} ");
		super.endVisit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (node.isInterface()) {
			print("interface<=>KW_interface ");
			print(node.getName().getIdentifier()+"<=>Name_interface ");
		} else {
			print("class<=>KW_class ");
			print(node.getName().getIdentifier()+"<=>Name_class ");
		}
		println("{<=>SEP_{");
		indent += 2;
		
		stack.push(node);
		
		return super.visit(node);
	}
	
	@Override
	public void endVisit(TypeDeclaration node) {
		indent -= 2;
		println("}<=>SEP_}");
		stack.pop();
	}
	
	@Override
	public boolean visit(SimpleName node) {
		if (!(stack.peek() instanceof TypeDeclaration))
			print(node + "<=>Name ");
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(Modifier node) {
		if (node.isAbstract()) {
			print("abstract<=>KW_abstract ");
		} else if (node.isAnnotation()) {
			print(node + "<=>KW_annotation ");
		} else if (node.isFinal()) {
			print("final<=>KW_final ");
		} else if (node.isPrivate()) {
			print("private<=>KW_private ");
		} else if (node.isProtected()) {
			print("protected<=>KW_protected ");
		} else if (node.isPublic()) {
			print("public<=>KW_public ");
		} else if (node.isStatic()) {
			print("static<=>KW_static ");
		} else if (node.isSynchronized()) {
			print("synchronized<=>KW_synchronized ");
		} else if (node.isVolatile()) {
			print("volatile<=>KW_volatile ");
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(SimpleType node) {
		print(node.getName() + "<=>KW_type ");
		return false;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		stack.push(node);
		return super.visit(node);
	}
	
	@Override
	public void endVisit(MethodDeclaration node) {
		stack.pop();
	}
	
	@Override
	public boolean visit(StringLiteral node) {
		print(node.getLiteralValue() + "<=>STRLIT ");
		return super.visit(node);
	}
	
}