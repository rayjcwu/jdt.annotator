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

public class Util {

	// concatenate two array
	public static String[] concat(String[] A, String[] B) {
		int aLen = A.length;
		int bLen = B.length;
		String[] C = new String[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}

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

	public static String[] getClasspath() {
		String classpath = System.getenv("CLASSPATH");
		return (classpath == null) ? new String[0] : classpath.split(":");
	}

	public static String guessProjectName(String argv) {
		String []args = argv.split("/");
		return args[args.length - 1];
	}


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
		parser.setUnitName("test");  // seems you should set unit name for whatever you want
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return parser;
	}

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
