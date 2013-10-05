package edu.cs.ucdavis.decal.test;

import java.lang.reflect.Field;

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
}