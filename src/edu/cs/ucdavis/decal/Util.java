package edu.cs.ucdavis.decal;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.antlr.JavaLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

/**
 * Utility methods
 * @author jcwu
 *
 */
public class Util {

	/**
	 * concatenate two string arrays
	 * @param A
	 * @param B
	 * @return Will return an empty array if A or B is null
	 */
	public static String[] concat(String[] A, String[] B) {
		if (A == null || B == null) {
			return new String[0];
		}
		int aLen = A.length;
		int bLen = B.length;
		String[] C = new String[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}

	/**
	 * Return all files with specified file types in path folder and its subfolders
	 * @param path Absolute path of
	 * @param filetypes Collect all java files by passing string array ["java"]
	 * @return String array of all qualified files
	 */
	@SuppressWarnings("unchecked")
	public static String[] collectFilePaths(String path, String[] filetypes) {
		Collection <File> filePaths = (Collection <File>) FileUtils.listFiles(new File(path), filetypes, true);
		String [] sourceFilePaths = new String[filePaths.size()];
		int i = 0;
		for (File f: filePaths) {
			sourceFilePaths[i] = f.toString();
			i++;
		}
		return sourceFilePaths;
	}

	/**
	 * Return environment variable CLASSPATH
	 * @return string array of CLASSPATH environment variable
	 */
	private static String[] getClasspath() {
		String classpath = System.getenv("CLASSPATH");
		return (classpath == null) ? new String[0] : classpath.split(":");
	}

	/**
	 * Return potential project name from source path
	 * @param argv
	 * @return
	 */
	public static String guessProjectName(String argv) {
		String []args = argv.split("/");
		return args[args.length - 1];
	}

	/**
	 * Retrieve a JDT ASTPaser. Pass libPath if you want to figure out
	 * type/method information from libraries outside of your project.
	 * Encoding of source files depend on system.
	 * @param libPath null or empty string for no libPath. Default will use CLASSPATH variable.
	 * @return
	 */
	public static ASTParser getParser(String libPath) {

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		String []classpath = Util.getClasspath();
		if (libPath == null || libPath.equals("")) {
			parser.setEnvironment(classpath, null, null, true);
		} else {
			String []libs = Util.collectFilePaths(libPath, new String[] {"jar"});
			String []combinePath = Util.concat(libs, classpath);
			parser.setEnvironment(combinePath, null, null, true);
		}
		parser.setUnitName("test");  // need to set this but never use
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return parser;
	}

	/**
	 * Use ANTLR to lex a java file. Will ignore identitifer and
	 * null/boolean/string/character literals since JDT has identical ASTNode for them.
	 * Skip EOF as well since we don't need it.
	 * @param fileContent java source code
	 * @return
	 */
	public static List <Token> prepareTokens(String fileContent) {
		ANTLRInputStream input = new ANTLRInputStream(fileContent);
		JavaLexer lexel = new JavaLexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexel);
		tokenStream.fill();
		List <Token> trimedTokens = new LinkedList<Token>();
		for (Token token: tokenStream.getTokens()) {
			if (!(token.getType() == JavaLexer.IDENTIFIER ||     // same as SimpleName
				  token.getType() == JavaLexer.NULL_LITERAL ||   // have corresponding literal ast nodes
				  token.getType() == JavaLexer.BOOLEAN_LITERAL ||
				  token.getType() == JavaLexer.STRING_LITERAL ||
				  token.getType() == JavaLexer.CHARACTER_LITERAL ||
				  token.getType() == JavaLexer.EOF)) {           // simply ignore
				trimedTokens.add(token);
			}
		}
		return trimedTokens;
	}
}
