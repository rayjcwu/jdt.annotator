package edu.cs.ucdavis.decal.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FileUtilsTest {
	
	@Test
	public void fileUtilTest() {
		String sourcePath = "C:\\repos\\xtext-cql";
		
		Collection <File> files = FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true);
		String [] filePaths = new String[files.size()];
		int i = 0;
		for (File file: files) {
			filePaths[i] = file.toString();
			i++;
		}
		
		
		for (String s: filePaths) {
			System.out.println(s);
		}
			
		/*
		for (File file: (Collection<File>) FileUtils.listFiles(new File(sourcePath), new String[] {"java"}, true)) {
			String fileContent = null;
			
			try {
				fileContent = FileUtils.readFileToString(file);
			} catch (IOException e) {
				
			}
			
			System.out.println(fileContent);
		}
		*/
	}
}
