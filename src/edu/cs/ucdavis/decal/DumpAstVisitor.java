package edu.cs.ucdavis.decal;

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
		controller.saveAstNodeInfo(node, currentUnit);
	}

	/*
	private ASTNode getDeclaringNode(IBinding binding) {
		if (binding == null) {
			return null;
		}

		ASTNode node = null;

		if (currentUnit != null) {
			node = currentUnit.findDeclaringNode(binding);
		}

		if (node == null) {
			if (units != null) {
			for (CompilationUnit unit: units) {
				node = unit.findDeclaringNode(binding.getKey());
				if (node != null) {
					break;
				}
			}
			} else {
				System.err.println("units is empty");
			}
		}
		return node;
	}
	*/

	@Override
	public boolean preVisit2(ASTNode node) {
		saveAstNodeInfo(node);
		return true;
	}
}