package edu.cs.ucdavis.decal.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.antlr.JavaLexer;
import org.junit.Test;

public class LexerTest {

	@Test
	public void testLexer() {
		try {
			for (Field field: JavaLexer.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					int id = field.getInt(null);
					String string = field.getName();
					String token = JavaLexer.tokenNames[id];
					if (token.startsWith("'")) {
						token = token.substring(1, token.length() - 1);
					}
					System.out.println(String.format("%d %s %s", id, string, token));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRemove() {
		List <String> ints = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			ints.add(" " + i);
		}

		List <Integer> remove_ints = new ArrayList<Integer>();
		remove_ints.add(1);
		remove_ints.add(4);
		remove_ints.add(8);

		int removed = 0;
		for (Integer i: remove_ints) {
			ints.remove(i - removed);
			removed++;
		}

		System.out.println(ints);
	}
}