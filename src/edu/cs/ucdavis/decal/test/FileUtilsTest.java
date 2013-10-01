package edu.cs.ucdavis.decal.test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FileUtilsTest {
	
	@Test
	public void fileUtilTest() {
		//String sourcePath = "/Users/jcwu/Dropbox/code/project/annotation/fake_project/demo/";
		String sourcePath = "/Users/jcwu/repos/vert.x/vertx-platform";
		
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
	
	@Test
	public void getAbsolutePath() {
		//String sourcePath = "~/project/annotation/fake_project/demo/";
		String sourcePath = "../";
		File f = new File(sourcePath);

		try {
			System.out.println(f.getAbsolutePath());
			System.out.println(f.getCanonicalPath());
			System.out.println(f.getName());
			System.out.println(f.getPath());
		} catch (IOException e) {
			
		}
	}
	
	@Test
	public void getRelativeFilePath() {
		String sourcePath = "/Users/jcwu/repos/vert.x/vertx-platform/";
		File f = new File(sourcePath);
		try {
			String pathToReplace = f.getCanonicalPath();
			
			Collection<File> filePaths = FileUtils.listFiles(f, new String[] {"java"}, true);
			for (File path: filePaths) {
				System.out.println(path.toString().replace(pathToReplace, ""));
			}
		} catch (IOException e) {
			;
		}
		
	}
}