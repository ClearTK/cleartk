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

import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.syntax.opennlp.OpenNLPTreebankParser;
import org.cleartk.temporal.timeml.TimeMLWriter;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class VerbClauseTemporalAnnotate {

	private static void error(String message) throws Exception {
		Logger logger = UimaContextFactory.createUimaContext().getLogger();
		logger.log(Level.SEVERE, String.format("%s\nusage: " +
				"VerbClauseTemporalAnnotate input-file-or-dir [output-dir]", message));
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		// check arguments
		if (args.length != 1 && args.length != 2) {
			error("wrong number of arguments");
		} else if (!new File(args[0]).exists()) {
			error("file or directory not found: " + args[0]);
		}
		
		// parse arguments
		String inputFileOrDir = args[0];
		File outputDir;
		if (args.length == 2) {
			outputDir = new File(args[1]);
		} else {
			outputDir = new File(".");
		}
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		
		// run the components on the selected documents
		SimplePipeline.runPipeline(
				FilesCollectionReader.getCollectionReader(inputFileOrDir),
				OpenNLPSentenceSegmenter.getDescription(),
				TokenAnnotator.getDescription(), 
				OpenNLPPOSTagger.getDescription(),
				DefaultSnowballStemmer.getDescription("English"),
				OpenNLPTreebankParser.getDescription(),
				VerbClauseTemporalAnnotator.getAnnotatorDescription(),
				TimeMLWriter.getDescription(outputDir.getPath()));
	}
}
