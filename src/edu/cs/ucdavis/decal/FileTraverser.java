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
	
}
