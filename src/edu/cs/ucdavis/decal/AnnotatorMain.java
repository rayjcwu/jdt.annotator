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
	public static void main(String[] argv) {
		Logger logger = Logger.getLogger("annotator");

		Options options = new Options();

		options.addOption("H", "host", true, "database server host ip (default: \"localhost\")");
		options.addOption("P", "port", true, "database server port (default: \"5432\")");
		options.addOption("d", "dbname", true, "database name to connect to (default: \"entity\")");
		options.addOption("U", "username", true, "(optional) username, must specify password as well");
		options.addOption("W", "password", true, "(optional) password, must specify username as well");
		options.addOption("r", "reset", false, "reset all annotated astnode information in database");

		options.addOption("s", "src", true, "absolute root path of source code files");
		options.addOption("l", "lib", true, "absolute root path of libraries (.jar)");
		options.addOption("p", "project", true, "project name (default: folder name containing source code)");

		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;

		String host = null;
		String port = null;
		String dbname = null;
		String username = null;
		String password = null;

		String projectName = null;
		String srcPath = null;
		String libPath = null;

		OmniController controller = null;
		try {
			cmd = parser.parse(options, argv);
			srcPath = cmd.getOptionValue("s");
			libPath = cmd.getOptionValue("l");
			projectName = cmd.getOptionValue("p");

			host = cmd.getOptionValue("H", "127.0.0.1");
			port = cmd.getOptionValue("P", "5432");
			dbname = cmd.getOptionValue("d", "entity");
			String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbname);
			username = cmd.getOptionValue("U");
			password = cmd.getOptionValue("W");

			if (cmd.hasOption("r")) {
				if (cmd.hasOption("U") && cmd.hasOption("W")) {
					(new PostgreSQLStorer(jdbcUrl, username, password)).resetDatabase();
				} else {
					(new PostgreSQLStorer(jdbcUrl)).resetDatabase();
				}
				System.out.println("reset done.");
			} else if (srcPath == null) {
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
				(new AnnotationASTRequestor()).register(controller);

				if (projectName == null || projectName.equals("")) {
					projectName = Util.guessProjectName(srcPath);
				}
				controller.setProjectName(projectName);
				controller.run();
				System.out.println("annotating finished.");
			}
		} catch (ParseException e) {
			formatter.printHelp("jdt.annotator --src <path> [options]", "= a source code annotator =", options, "");
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString());
			e.printStackTrace();
		} finally {
			if (controller != null)
				controller.getDatabase().close();
		}
	}
}