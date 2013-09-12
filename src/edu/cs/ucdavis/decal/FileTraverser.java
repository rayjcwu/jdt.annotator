package edu.cs.ucdavis.decal;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class FileTraverser {
	String sourcePath;
	
	public FileTraverser(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	
	public void run() {
				
		// collect String[] 
		Collection <File> filePaths = (Collection <File>) FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true);
		String [] sourceFilePaths = new String[filePaths.size()];
		
		int i = 0;
		for (File f: filePaths) {
			sourceFilePaths[i] = f.toString();
			i++;
		}
		
		parse(sourceFilePaths);
	}

	public void parse(String[] sourceFilePaths) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		//parser.setSource(sourceCode.toString().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		//String JDT_PATH = "/Users/jcwu/project/ecj/JDT_DOM/lib";
//		String [] classpathEntries = {
//				JDT_PATH + "/org.eclipse.core.contenttype-3.4.200.v20130326-1255.jar",
//				JDT_PATH + "/org.eclipse.core.jobs-3.5.300.v20130429-1813.jar",
//				JDT_PATH + "/org.eclipse.core.resources-3.8.100.v20130521-2026.jar",
//				JDT_PATH + "/org.eclipse.core.runtime-3.9.0.v20130326-1255.jar",
//				JDT_PATH + "/org.eclipse.equinox.common-3.6.200.v20130402-1505.jar",
//				JDT_PATH + "/org.eclipse.equinox.preferences-3.5.100.v20130422-1538.jar",
//				JDT_PATH + "/org.eclipse.jdt.core-3.9.0.v20130604-1421.jar",
//				JDT_PATH + "/org.eclipse.osgi-3.9.0.v20130529-1710.jar"
//				
//		};
//		String [] sourcepathEntries = {};
		//parser.setEnvironment(classpathEntries, sourcepathEntries, null, false);
		
		parser.setEnvironment(null, null, null, true);
		parser.setUnitName("test");
		parser.setResolveBindings(true);
		
		FileASTRequestor requestor = new AnnotationASTRequestor();
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
  		//CompilationUnit cu = (CompilationUnit) parser.createAST(null);
  		//ASTVisitor visitor = new AnnotationAstVisitor(cu);
 		//cu.accept(visitor);
	}
}
