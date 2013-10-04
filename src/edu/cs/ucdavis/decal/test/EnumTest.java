package edu.cs.ucdavis.decal.test;

import org.junit.Test;

import edu.cs.ucdavis.decal.Token;

public class EnumTest {

	@Test
	public void testEnum() {
		for (Token token: Token.values()) {
			System.out.println(token.getId() + " " + token.toString() + " " + token.getToken());
		}
	}
}