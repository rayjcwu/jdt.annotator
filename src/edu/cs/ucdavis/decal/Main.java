package edu.cs.ucdavis.decal;

import org.jboss.logging.Logger;

public class Main {
	public static void main(String [] argv) {
		// HibernateClient hc = new HibernateClient();
		// hc.run();		
		Logger logger = Logger.getLogger("annotator");
		
		if (argv.length != 1) {
			logger.error("need an argument for src/");
		} else {
			try {
				
				FileTraverser ft = new FileTraverser(argv[0]);
				ft.runZT();
				
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
	}
}