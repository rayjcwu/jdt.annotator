package edu.cs.ucdavis.decal.test;

import java.lang.reflect.Field;

import org.junit.Assert;

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

	private int findIndex(int start_pos, int[] array) {
		int max = array.length - 1;
		int min = 0;

		int mid = (max + min)/2;
		while (max > min) {
			mid = (max + min)/2;
			if (array[mid] > start_pos) {
				max = mid;
			} else if (array[mid] < start_pos) {
				min = mid + 1;
			} else {
				return mid;
			}
		}

		// not found
		while (mid >= 0) {
			if (array[mid] < start_pos){
				break;
			}
			mid --;
		}
		return mid;
	}

	@Test
	public void testBinarySearch() {
		int []a = new int[]{4, 6, 7,  9, 14, 20};

		Assert.assertTrue(findIndex(0, a) <= 0);
		Assert.assertTrue(findIndex(1, a) <= 0);
		Assert.assertTrue(findIndex(2, a) <= 0);
		Assert.assertTrue(findIndex(3, a) <= 0);

		Assert.assertEquals(0, findIndex(4, a));

		Assert.assertTrue(findIndex(5, a) <= 1);

		Assert.assertEquals(1, findIndex(6, a));
		Assert.assertEquals(2, findIndex(7, a));

		Assert.assertTrue(findIndex(8, a) <= 3);

		Assert.assertEquals(3, findIndex(9, a));

		Assert.assertTrue(findIndex(10, a) <= 4);
		Assert.assertTrue(findIndex(11, a) <= 4);
		Assert.assertTrue(findIndex(12, a) <= 4);
		Assert.assertTrue(findIndex(13, a) <= 4);

		Assert.assertEquals(4, findIndex(14, a));
	}
}
