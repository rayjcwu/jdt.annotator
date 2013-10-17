package edu.cs.ucdavis.decal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class DumpAstVisitor extends ASTVisitor {

	CompilationUnit currentUnit;
	OmniController controller;

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
		if (node.getStartPosition() == -1 && node.getLength() == 0) {
			Logger logger = Logger.getAnonymousLogger();
			logger.log(Level.SEVERE, "Node doesn't have information: " + node.toString());
		} else {
			controller.saveAstNodeInfo(node, currentUnit);
		}
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		saveAstNodeInfo(node);
		return true;
	}
}