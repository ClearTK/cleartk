/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.temporal;

import java.io.File;
import java.io.IOException;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.ViewNames;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.svmlight.DefaultOVASVMlightDataWriterFactory;
import org.cleartk.corpus.timeml.PlainTextTLINKGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLGoldAnnotator;
import org.cleartk.corpus.timeml.TreebankAligningAnnotator;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.UIMAUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.factory.UimaContextFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class VerbClauseTemporalTrain {
	
	private static void error(String message) throws Exception {
		Logger logger = UimaContextFactory.createUimaContext().getLogger();
		logger.log(Level.SEVERE, String.format("%s\nusage: " +
				"VerbClauseTemporalMain timebank-dir treebank-dir output-dir",
				message));
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		// check arguments
		if (args.length != 3) {
			error("wrong number of arguments");
		} else if (!new File(args[0]).exists()) {
			error("TimeBank directory not found: " + args[0]);
		} else if (!new File(args[1]).exists()) {
			error("TreeBank directory not found: " + args[1]);
		}
		
		
		String timeBankDir = args[0];
		String treeBankDir = args[1];
		String outputDir = args[2];
		
		// clean up the mismatches between TimeBank and TreeBank
		File cleanedTimeBankDir = getCleanedTimeBankDir(timeBankDir);
		timeBankDir = cleanedTimeBankDir.getPath();
		
		// use the common ClearTk type system
		TypeSystemDescription typeSystem =
			TypeSystemDescriptionFactory.createTypeSystemDescription(
					"org.cleartk.TypeSystem");

		// run the components that write out the training data
		UIMAUtil.runUIMAPipeline(
				CollectionReaderFactory.createCollectionReader(
						FilesCollectionReader.class, typeSystem,
						FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, timeBankDir,
						FilesCollectionReader.PARAM_PATTERNS, new String[]{"wsj"},
						FilesCollectionReader.PARAM_VIEW_NAME, ViewNames.TIMEML),
				AnalysisEngineFactory.createAnalysisEngine(
						TimeMLGoldAnnotator.class, typeSystem,
						TimeMLGoldAnnotator.PARAM_LOAD_TLINKS, false),
				AnalysisEngineFactory.createAnalysisEngine(
						PlainTextTLINKGoldAnnotator.class, typeSystem,
						PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL,
						"http://www.stanford.edu/~bethard/data/timebank-verb-clause.txt"),
				AnalysisEngineFactory.createAnalysisEngine(
						TreebankAligningAnnotator.class, typeSystem,
						TreebankAligningAnnotator.PARAM_TREEBANK_DIRECTORY, treeBankDir),
				AnalysisEngineFactory.createAnalysisEngine(
						SnowballStemmer.class, typeSystem,
						SnowballStemmer.PARAM_STEMMER_NAME, "English"),
				AnalysisEngineFactory.createAnalysisEngine(
						DataWriterAnnotator.class, typeSystem,
						InstanceConsumer.PARAM_ANNOTATION_HANDLER,
						VerbClauseTemporalHandler.class.getName(),
						DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS,
						DefaultOVASVMlightDataWriterFactory.class.getName(),
						DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDir));
		
		// remove the temporary directory containing the cleaned up TimeBank
		FileUtils.deleteRecursive(cleanedTimeBankDir);

		// train the model
		Train.main(new String[]{outputDir});
		
	}
	
	public static File getCleanedTimeBankDir(String timeBankDir) throws IOException {
		File tempDir = File.createTempFile("TimeBank", "Cleaned");
		tempDir.delete();
		tempDir.mkdir();
		for (File file: new File(timeBankDir).listFiles()) {
			String name = file.getName();
			if (file.isHidden() || name.startsWith(".")) {
				continue;
			}
			
			// get the file text
			String text = FileUtils.file2String(file);

			// all ampersands are messed up in TimeBank
			text = text.replaceAll("\\bamp\\b", "&amp;");
			text = text.replaceAll("SampP", "S&amp;P");
			
			// all "---" missing in TreeBank
			text = text.replaceAll("---", "");
			
			// fix individual file errors
			text = fixTextByFileName(name, text);

			// write the file to the temp directory
			FileUtils.saveString2File(text, new File(tempDir, file.getName()));
		}
		return tempDir;
		
	}
	
	public static String fixTextByFileName(String name, String text) {
		// duplicate "the" in TimeBank
		if (name.equals("wsj_0032.tml")) {
			text = text.replace(
					"the <TIMEX3 tid=\"t18\"",
					"<TIMEX3 tid=\"t18\"");
		}

		// missing "DD"s in TimeBank
		else if (name.equals("wsj_0159.tml")) {
			text = text.replace(
					"Acquisition has <EVENT eid=\"e11\"",
					"DD Acquisition has <EVENT eid=\"e11\"");
			text = text.replace(
					"Acquisition <EVENT eid=\"e20\"",
					"DD Acquisition <EVENT eid=\"e20\"");
		}

		// missing "BRUCE R. BENT" in TreeBank
		else if (name.equals("wsj_0266.tml")) {
			text = text.replace("BRUCE R. BENT", "");
		}
		
		// missing 30. in TreeBank
		else if (name.equals("wsj_0344.tml")) {
			text = text.replace(" 30</TIMEX3>.", "</TIMEX3>");
		}
		
		// reversed "off roughly" in TimeBank
		else if (name.equals("wsj_0376.tml")) {
			text = text.replace("roughly off", "off roughly");
		}
		
		// missing @... lines in TreeBank
		else if (name.equals("wsj_0586.tml")) {
			text = text.replaceAll("(?m)@((?!</HL>).)*?$", "");
		}
		
		// missing @CORPORATES and @EUROBONDS in TreeBank
		else if (name.equals("wsj_0612.tml")) {
			text = text.replace(
					"@ <ENAMEX TYPE=\"ORGANIZATION\">CORPORATES",
					"<ENAMEX TYPE=\"ORGANIZATION\">");
			text = text.replace(
					"@ <ENAMEX TYPE=\"ORGANIZATION\">EUROBONDS",
					"<ENAMEX TYPE=\"ORGANIZATION\">");
		}
		
		// missing "1988." in TreeBank
		else if (name.equals("wsj_0667.tml")) {
			text = text.replace("1988</TIMEX3>.", "</TIMEX3>");
		}
		
		// missing "--" in TimeBank and missing "19.29." in TreeBank
		else if (name.equals("wsj_0675.tml")) {
			text = text.replace("Markets</ENAMEX>", "Markets</ENAMEX> --");
			text = text.replace("19.29</CARDINAL>.", "</CARDINAL>");
		}
		
		// reversed "definitely not" in TimeBank
		else if (name.equals("wsj_0781.tml")) {
			text = text.replace("not definitely", "definitely not");
		}
		
		// really messed up text in TimeBank
		else if (name.equals("wsj_1003.tml")) {
			text = text.replace(
					"a shhha55 cents a share,   ents a share, but  ssa share",
					"a share");
			text = text.replace(
					"steel business, <EVENT eid=\"e109\"",
					"Armco, hampered by lower volume in its specialty steel " +
					"business, <EVENT eid=\"e109\"");
		}
		
		return text;
	}
}
