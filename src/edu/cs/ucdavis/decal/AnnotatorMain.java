package edu.cs.ucdavis.decal;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AnnotatorMain {
	public static void main(String [] argv) {
		Logger logger = Logger.getLogger("annotator");

		if (argv.length != 1) {
			logger.log(Level.SEVERE, "need an argument for src/");
		} else {
			try {
				OmniController controller = new OmniController(argv[0]);
				controller.setLogger(logger);
				(new PostgreSQLStorer("jdbc:postgresql://127.0.0.1:5432/annotation")).register(controller);
				(new DumpAstVisitor()).register(controller);
				(new AnnotationASTRequestor()).register(controller);
				
				controller.setProjectName("demo");
				
				controller.run();
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString());
				e.printStackTrace();
			}
		}
	}
}