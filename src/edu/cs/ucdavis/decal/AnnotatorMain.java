package edu.cs.ucdavis.decal;

import java.util.Properties;

import org.jboss.logging.Logger;

public class AnnotatorMain {
	public static void main(String [] argv) {
		Logger logger = Logger.getLogger("annotator");
		Properties prop = new Properties();
		
		if (argv.length != 1) {
			logger.error("need an argument for src/");
		} else {
			try {
				PostgreSQLStorer db = new PostgreSQLStorer("jdbc:postgresql://127.0.0.1:5432/annotation");
				db.init();
				
				
				/*
				FileTraverser ft = new FileTraverser(argv[0]);
				ft.run();
				*/
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
	}
}