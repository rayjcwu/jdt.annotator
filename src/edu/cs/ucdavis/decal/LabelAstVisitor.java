package edu.cs.ucdavis.decal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class LabelAstVisitor extends ASTVisitor {
	Map <ASTNode, Integer> nodeLabel;
	List <ASTNode> nodeList;
	int label;

	Long nextVal;

	public LabelAstVisitor() {
		nodeLabel = new HashMap<ASTNode, Integer>();
		nodeList = new LinkedList<ASTNode>();
		label = 0;
		nextVal = 0L;
	}

	public void reset() {
		nodeLabel = new HashMap<ASTNode, Integer>();
		nodeList = new LinkedList<ASTNode>();
		label = 0;
		nextVal = 0L;
	}

	public void setNextVal(Long nextVal) {
		this.nextVal = nextVal;
	}

	public Long getNextVal() {
		return nextVal;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		nodeLabel.put(node, label);
		nodeList.add(node);
		label++;
		return true;
	}

	public Map<ASTNode, Integer> getNodeLabel() {
		return nodeLabel;
	}

	public List<ASTNode> getNodeList() {
		return nodeList;
	}
}