package edu.cs.ucdavis.decal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * This visitor will save each AST node with an unique index number.
 * Index starts with 0. To map index to correct serial index in database,
 * you need a delta value nextVal retrieved from database
 * @author jcwu
 *
 */
public class LabelAstVisitor extends ASTVisitor {
	Map <ASTNode, Integer> nodeLabel;
	List <ASTNode> nodeList;  // in order to visit each node in order TODO: could use LinkedHashMap instead
	int label;

	Long nextVal;	// to store this information in visitor and pass it around, not meant to be used in visitor itself

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
		if (node.getStartPosition() == -1) {
			return false;
		}
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