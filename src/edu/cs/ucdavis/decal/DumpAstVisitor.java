package edu.cs.ucdavis.decal;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class DumpAstVisitor extends ASTVisitor {

	CompilationUnit currentUnit;
	OmniController controller;

	private int currentAstnodeId;

	public DumpAstVisitor() {
		currentUnit = null;
		controller = null;
	}

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setVisitor(this);
	}

	public DumpAstVisitor setController(OmniController controller) {
		this.controller = controller;
		return this;
	}

	public DumpAstVisitor setCurrentCompilationUnit(CompilationUnit unit) {
		currentUnit = unit;
		return this;
	}

	private void saveAstNodeInfo(ASTNode node) {
		controller.saveAstNodeInfo(node, currentUnit);
	}

	private void saveTokenInfo(ASTNode node) {
		controller.saveTokenInfo(node, currentUnit);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		saveAstNodeInfo(node);
		return true;
	}

	@Override
	public void postVisit(ASTNode node) {
		saveTokenInfo(node);
	}
}