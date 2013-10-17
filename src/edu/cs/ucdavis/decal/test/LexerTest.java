package edu.cs.ucdavis.decal.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.JavaLexer;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.core.util.PublicScanner;
import org.junit.Test;



public class LexerTest {

	@Test
	public void testJdtLexer() {
		IScanner scanner = new PublicScanner();
		String source = "public class A { int a; List <String> b = new ArrayList<String>(); }";
		scanner.setSource(source.toCharArray());

		Map <Integer, String> map = new HashMap<Integer, String>();
		try {
			for (Field field: ITerminalSymbols.class.getDeclaredFields()) {
				if (field.getType().equals(int.class)) {
					int id = field.getInt(null);
					String string = field.getName().substring(9).toUpperCase();
					map.put(id, string);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		final int EOF_ID = 158;
		int token;

		while (true) {
			try {
				token = scanner.getNextToken();
				if (token == EOF_ID) { break; }

				System.out.println(scanner.getCurrentTokenSource());

				System.out.println(String.format("%s %d -> %d", map.get(token), scanner.getCurrentTokenStartPosition(), scanner.getCurrentTokenEndPosition()));
			} catch (InvalidInputException e) {
				break;
			}
		}

	}

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