package edu.cs.ucdavis.decal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class FileTraverser {
	String sourcePath;
	
	public FileTraverser(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	
	public void run() {
		// setting parser parameters
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		// parser.setSource(sourceCode.toString().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		parser.setEnvironment(null, null, null, true);
		parser.setUnitName("test");
		parser.setResolveBindings(true);
		
		Collection <CompilationUnit> units = new ArrayList<CompilationUnit>();
		Collection <Pair <File, CompilationUnit> > filesAndUnits = new ArrayList<Pair <File, CompilationUnit>> ();
		
		for (File f: (Collection <File>) FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true)) {
			try {
				String fileContent = FileUtils.readFileToString(f);
				parser.setSource(fileContent.toCharArray());
				CompilationUnit unit = (CompilationUnit) parser.createAST(null);
				units.add(unit);
				filesAndUnits.add(Pair.of(f, unit));
			} catch (IOException e) {
				System.out.println(f.getName() + " reading failed");
			}
		}
		
		DumpAstVisitor visitor = new DumpAstVisitor();
		visitor.setCurrentCompilationUnits(units);
		
		for (Pair<File, CompilationUnit> p: filesAndUnits) {
			System.out.println("\n============================= " + p.getLeft().getName());
			p.getRight().accept(visitor);
		}
	}
	
	public void run2() {
				
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
	

		Collection <CompilationUnit> units = new ArrayList<CompilationUnit>();
		
		for(String sourceFilePath: sourceFilePaths) {
			
		}
		
		//FileASTRequestor requestor = new AnnotationASTRequestor();
		//parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
  		//CompilationUnit cu = (CompilationUnit) parser.createAST(null);
  		//ASTVisitor visitor = new AnnotationAstVisitor(cu);
 		//cu.accept(visitor);
	}
}
