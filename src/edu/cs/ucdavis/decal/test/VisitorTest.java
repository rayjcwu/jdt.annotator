package edu.cs.ucdavis.decal.test;

import org.junit.Assert;
import org.junit.Test;

import edu.cs.ucdavis.decal.DumpAstVisitor;
import edu.cs.ucdavis.decal.Token;

public class VisitorTest {
	@Test
	public void testStartPos() {
		DumpAstVisitor visitor = new DumpAstVisitor();

		int pos = visitor.getTokenStartPos("/**  asdfkl lkj */\n @Deprecatted\npackage demo.example", 7, Token.PACKAGE, 10);
		Assert.assertEquals("test", 40, pos);

		pos = visitor.getTokenStartPos("package demo.example", 7, Token.PACKAGE, 0);
		Assert.assertEquals("test", 7, pos);

		pos = visitor.getTokenStartPos(" package demo.example", 7, Token.PACKAGE, 0);
		Assert.assertEquals("test", 8, pos);

		pos = visitor.getTokenStartPos(" package demo.example", 7, Token.PACKAGE, 8);
		Assert.assertEquals("test", -1, pos);

		pos = visitor.getTokenStartPosBackward("public static void class Main (string argv[]) {}", 10, Token.CLASS, 0);
		Assert.assertEquals("test", 29, pos);

		pos = visitor.getTokenStartPosBackward("public static void class Main (string argv[]) {}", 10, Token.INTERFACE, 0);
		Assert.assertEquals("test", -1, pos);


		pos = visitor.getTokenStartPosBeforeBackwardStartPos("public static void class Main (string argv[]) {}", 10, Token.CLASS, 35);
		Assert.assertEquals("test", 29, pos);
	}
}
