package edu.cs.ucdavis.decal;

import java.util.LinkedHashMap;
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
	int label;

	Long nextVal;	// to store this information in visitor and pass it around, not meant to be used in visitor itself

	public LabelAstVisitor() {
		nodeLabel = new LinkedHashMap<ASTNode, Integer>(); // use linked hashmap to preserve order of insertion
		label = 0;
		nextVal = 0L;
	}

	public void reset() {
		nodeLabel = new LinkedHashMap<ASTNode, Integer>();
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
		label++;
		return true;
	}

	public Map<ASTNode, Integer> getNodeLabel() {
		return nodeLabel;
	}
}