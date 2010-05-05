package org.cleartk.example;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public abstract class AbstractOptions {

	public void parseOptions(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			e.printStackTrace();
			parser.printUsage(System.err);
			System.exit(1);
		}
	}

}
