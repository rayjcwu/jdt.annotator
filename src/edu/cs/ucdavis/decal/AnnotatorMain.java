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
		options.addOption("s", "src", true, "absolute root path of source code files");
		options.addOption("l", "lib", true, "absolute root path of libraries (.jar)");
		options.addOption("p", "project", true, "project name");
		options.addOption("d", "jdbc", true, "jdbc url, currently only support postgresql (jdbc:postgresql://ip:port/database) (postgresql default port: 5432)");
		options.addOption("r", "reset", false, "reset all annotated astnode information in database [need to specify --jdbc]");
		options.addOption("U", "username", true, "(optional) username, must specify password as well");
		options.addOption("W", "password", true, "(optional) password, must specify username as well");

		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;

		String jdbcUrl = "";
		String projectName = "";
		String srcPath = "";
		String libPath = "";
		String username = "";
		String password = "";

		OmniController controller = null;
		try {
			cmd = parser.parse(options, argv);
			srcPath = cmd.getOptionValue("s");
			libPath = cmd.getOptionValue("l");
			projectName = cmd.getOptionValue("p");
			jdbcUrl = cmd.getOptionValue("d");
			username = cmd.getOptionValue("U");
			password = cmd.getOptionValue("W");

			if (cmd.hasOption("r") && jdbcUrl != null) {
				if (cmd.hasOption("U") && cmd.hasOption("W")) {
					(new PostgreSQLStorer(jdbcUrl, username, password)).resetDatabase();
				} else {
					(new PostgreSQLStorer(jdbcUrl)).resetDatabase();
				}
				System.out.println("reset done.");
			} else if (srcPath == null || jdbcUrl == null) {
				throw new ParseException(argv.toString());
			} else {
				controller = new OmniController();
				controller.setSourcePath(srcPath);
				controller.setLibPath(libPath);
				controller.setLogger(logger);
				if (cmd.hasOption("U") && cmd.hasOption("W")) {
					controller.setDatabase(new PostgreSQLStorer(jdbcUrl, username, password));
				} else {
					controller.setDatabase(new PostgreSQLStorer(jdbcUrl));
				}
				(new DumpAstVisitor()).register(controller);
				(new AnnotationASTRequestor()).register(controller);

				if (projectName == null || projectName.equals("")) {
					projectName = Util.guessProjectName(srcPath);
				}
				controller.setProjectName(projectName);
				controller.clearProjectAstNodeInfo();
				controller.run();
				System.out.println("annotating finished.");
			}
		} catch (ParseException e) {
			formatter.printHelp("annotator.jar", options);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString());
			e.printStackTrace();
		} finally {
			if (controller != null)
				controller.getDatabase().close();
		}
	}
}