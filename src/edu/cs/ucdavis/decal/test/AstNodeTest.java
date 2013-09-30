package edu.cs.ucdavis.decal.test;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Test;

public class AstNodeTest {
	@Test
	public void testAstNode() {
		try {
			for (Field field : ASTNode.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					System.out.println(field.getInt(null) + " "
							+ field.getName());

				}
			}
		} catch (IllegalAccessException e) {
			System.err.println("should not happend");
		}
	}

}
