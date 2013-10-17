package edu.cs.ucdavis.decal;

import org.antlr.v4.runtime.Token;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class LookupVisitor extends ASTVisitor {

	// tightest node containing current token so far
	private int startPos;
	private int length;
	private int nodetype;

	private Token token;

	public LookupVisitor() {
		startPos = -1;
		length 	  = -1;
		nodetype  = -1;
		token = null;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public void reset() {
		startPos = -1;
		length = -1;
		nodetype = -1;
		token = null;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		final int token_start_pos = token.getStartIndex();

		if (node.getStartPosition() <= token_start_pos && token_start_pos < node.getStartPosition() + node.getLength()) {
			startPos = node.getStartPosition();
			length = node.getLength();
			nodetype = node.getNodeType();
			return true;
		} else {
			return false;
		}
	}

	public int getStartPos() {
		return startPos;
	}

	public int getLength() {
		return length;
	}

	public int getNodetype() {
		return nodetype;
	}


}
