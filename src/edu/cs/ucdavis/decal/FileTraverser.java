package edu.cs.ucdavis.decal;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class FileTraverser {
	String sourcePath;
	
	public FileTraverser(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	
	private ASTParser parserInit() {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);		
		parser.setEnvironment(null, null, null, true);
		parser.setUnitName("test");
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		return parser;
	}
//	
//	public void run2() {
//		// setting parser parameters
//		ASTParser parser = ASTParser.newParser(AST.JLS4);
//		// parser.setSource(sourceCode.toString().toCharArray());
//		parser.setKind(ASTParser.K_COMPILATION_UNIT);
//
//		parser.setEnvironment(null, null, null, true);
//		parser.setUnitName("test");
//		parser.setResolveBindings(true);
//		parser.setBindingsRecovery(true);
//		
//		Collection <CompilationUnit> units = new ArrayList<CompilationUnit>();
//		Collection <Pair <File, CompilationUnit> > filesAndUnits = new ArrayList<Pair <File, CompilationUnit>> ();
//		
//		for (File f: (Collection <File>) FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true)) {
//			try {
//				String fileContent = FileUtils.readFileToString(f);
//				parser.setSource(fileContent.toCharArray());
//				CompilationUnit unit = (CompilationUnit) parser.createAST(null);
//				units.add(unit);
//				filesAndUnits.add(Pair.of(f, unit));
//			} catch (IOException e) {
//				System.out.println(f.getName() + " reading failed");
//			}
//		}
//		
//		DumpAstVisitor visitor = new DumpAstVisitor();
//		visitor.setCurrentCompilationUnits(units);
//		
//		for (Pair<File, CompilationUnit> p: filesAndUnits) {
//			System.out.println("\n============================= " + p.getLeft().getName());
//			p.getRight().accept(visitor);
//		}
//	}
//	
	public void run() {
		// collect String[] 
		Collection <File> filePaths = (Collection <File>) FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true);
		String [] sourceFilePaths = new String[filePaths.size()];
		
		int i = 0;
		for (File f: filePaths) {
			sourceFilePaths[i] = f.toString();
			i++;
		}
	
		ASTParser parser = parserInit();
		

		DumpAstVisitor visitor = new DumpAstVisitor();
		visitor.setBindingKeys(new HashSet<String>());
		visitor.setCompilationUnits(new HashSet<CompilationUnit>());
		visitor.setResolve(false);
		
		AnnotationASTRequestor requestor = new AnnotationASTRequestor();
		requestor.setVisitor(visitor);
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
		
		// check visitor
		System.out.println("print binding keys");
		/*
		for (String bindingKey: visitor.getBindingKeys()) {
			System.out.println(bindingKey);
		}
		
		System.out.println("print compilation unit");
		for (CompilationUnit cu: visitor.getCompilatoinUnits()) {
			System.out.println(cu);
		}
		*/
		/*
		visitor.setResolve(true);
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
		*/
		
		for (String bindingKey: visitor.getBindingKeys()) {
			System.out.println(bindingKey);
			for (CompilationUnit cu: visitor.getCompilatoinUnits()) {
				ASTNode node = cu.findDeclaringNode(bindingKey);
				if (node != null) {
					System.out.println(node);
					break;
				}
			}
		}
	}
	
//	public void run3() {
//		// collect String[] 
//		Collection <File> filePaths = (Collection <File>) FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true);
//		String [] sourceFilePaths = new String[filePaths.size()];
//		
//		Collection <CompilationUnit> units = new ArrayList<CompilationUnit>();
//		
//		// create parser
//		ASTParser parser = parserInit();
//
//		int i = 0;
//		for (File f : filePaths) {
//			try {
//				String fileContent = FileUtils.readFileToString(f);
//				parser.setSource(fileContent.toCharArray());
//
//				sourceFilePaths[i] = f.toString();
//				i++;
//				CompilationUnit unit = (CompilationUnit) parser.createAST(null);
//				units.add(unit);
//			} catch (IOException e) {
//				System.err.println("can't read files");
//			}
//		}
//
//		FileASTRequestor requestor = new AnnotationASTRequestor(units);
//		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
//  		//
//  		//ASTVisitor visitor = new AnnotationAstVisitor(cu);
// 		//cu.accept(visitor);
//	}
	
	
}
