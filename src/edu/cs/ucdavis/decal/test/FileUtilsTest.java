package edu.cs.ucdavis.decal.test;

import java.io.File;
import java.util.Collection;

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
	}
}
