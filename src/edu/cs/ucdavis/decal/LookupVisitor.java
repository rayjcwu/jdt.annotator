package edu.cs.ucdavis.decal;

import org.antlr.v4.runtime.Token;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class LookupVisitor extends ASTVisitor {

	// tightest node containing current token so far
	private ASTNode lastSeenNode;
	private Token token;

	public LookupVisitor() {
		lastSeenNode = null;
		token = null;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public void reset() {
		lastSeenNode = null;
		token = null;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		final int token_start_pos = token.getStartIndex();

		if (node.getStartPosition() <= token_start_pos && token_start_pos < node.getStartPosition() + node.getLength()) {
			lastSeenNode = node;
			return true;
		} else {
			return false;
		}
	}

	public ASTNode getLastSeenNode() {
		return lastSeenNode;
	}

}
