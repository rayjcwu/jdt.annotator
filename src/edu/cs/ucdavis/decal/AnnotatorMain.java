package edu.cs.ucdavis.decal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class AnnotatorMain {
	/**
	 * example argv
	   --jdbc jdbc:postgresql://127.0.0.1:5432/annotation
	   --project demo
	   --src /Users/jcwu/Dropbox/code/project/annotation/fake_project/
	 */
	public static void main(String[] argv) {
		Logger logger = Logger.getLogger("annotator");

		Options options = new Options();
		options.addOption("s", "src", true, "absolute root path of files");
		options.addOption("p", "project", true, "project name");
		options.addOption("d", "jdbc", true, "jdbc url, currently only support postgresql (jdbc:postgresql://ip:port/schema)");
		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;

		String jdbc_url = "";
		String project_name = "";
		String src_path = "";
		try {
			cmd = parser.parse(options, argv);
			src_path = cmd.getOptionValue("s");
			project_name = cmd.getOptionValue("p", src_path);
			jdbc_url = cmd.getOptionValue("d");
			if (src_path == null || project_name == null || jdbc_url == null) { throw new ParseException(argv.toString()); }

			OmniController controller = new OmniController(src_path);
			controller.setLogger(logger);
			(new PostgreSQLStorer(jdbc_url)).resetDatabase().register(controller);
			(new DumpAstVisitor()).register(controller);
			(new AnnotationASTRequestor()).register(controller);
			controller.setProjectName(project_name);
			controller.run();

			System.out.println("finished");
		} catch (ParseException e) {
			formatter.printHelp("annotator.jar", options);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString());
			e.printStackTrace();
		}
	}
}