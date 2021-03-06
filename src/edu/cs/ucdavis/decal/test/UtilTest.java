package edu.cs.ucdavis.decal.test;

import org.junit.Test;
import org.junit.Assert;

import edu.cs.ucdavis.decal.Util;

public class UtilTest {

	@Test
	public void testClasspath() {
		String classpath = System.getenv("CLASSPATH");  // will return null in Eclipse....
		System.out.println(classpath);
	}

	@Test
	public void testSplit() {
		String classpath = ".:/usr/local/lib/ST-4.0.7.jar:/usr/local/Cellar/antlr/4.1/antlr-4.1-complete.jar:";
		String [] paths = classpath.split(":");
		for (int i = 0; i < paths.length; i++) {
			System.out.println(i + " " + paths[i]);
		}
	}

	@Test
	public void testConcat() {
		String [] a = new String[] {"1", "2", "3"};
		String [] b = new String[] {"4", "5"};

		String [] c = concat(a, b);

		for (String s: c) {
			System.out.println(s);
		}
	}

	@Test
	public void testGuessProjectName() {

		Assert.assertEquals("guess project name", "de-3.mo", Util.guessProjectName("/Users/jcwu/project/annotation/fake_project/de-3.mo"));
		Assert.assertEquals("guess project name 2", "demo", Util.guessProjectName("/Users/jcwu/project/annotation/fake_project/demo/"));
	}


	private String[] concat(String[] A, String[] B) {
		int aLen = A.length;
		int bLen = B.length;
		String[] C = new String[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}
}