package edu.cs.ucdavis.decal;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

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
}
