/** 
  * Copyright (c) 2010, Regents of the University of Colorado 
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

package org.cleartk.example.treebank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.CleartkComponents;
import org.cleartk.ViewNames;
import org.cleartk.example.AbstractOptions;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.XWriter;
import org.kohsuke.args4j.Option;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.util.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * This class takes in treebank files and parses the the PTB-style format and
 * populates a the CAS with constituent trees and then writes out XMI files. 
 * The CAS will contain the raw treebank data in a view 
 * You
 * may find it useful to view the generated XMI files in the CAS Visual
 * Debugger.
 * 
 * @author Philip Ogren
 * 
 */
public class TreebankParsingExample {

	public static class Options extends AbstractOptions {
		@Option(name = "-td", aliases = "--treebankDirectory", usage = "specify the directory containing treebank files", required = true)
		public String treebankDirectory;

		@Option(name = "-o", aliases = "--outputDirectory", usage = "specify the directory to write the XMI files to", required = true)
		public String outputDirectory;

		@Option(name = "-suf", aliases = "--treebankFileSuffixes", usage = "specify file suffixes of the treebank files in the treebank directory", multiValued = true)
		public List<String> treebankFileSuffixes = new ArrayList<String>();
	}

	public static void main(String[] args) throws UIMAException, IOException {
		Options options = new Options();
		options.parseOptions(args);

		String[] suffixes = options.treebankFileSuffixes.toArray(new String[options.treebankFileSuffixes.size()]);
		
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION, 
				FilesCollectionReader.PARAM_ROOT_FILE, options.treebankDirectory,
				FilesCollectionReader.PARAM_VIEW_NAME, ViewNames.TREEBANK, 
				FilesCollectionReader.PARAM_SUFFIXES, suffixes);

		AnalysisEngine viewCreator = AnalysisEngineFactory.createPrimitive(ViewCreatorAnnotator.class, CleartkComponents.TYPE_SYSTEM_DESCRIPTION,
				ViewCreatorAnnotator.PARAM_VIEW_NAME, ViewNames.GOLD_VIEW);

		AnalysisEngineDescription treebankParserDescription = AnalysisEngineFactory.createPrimitiveDescription(TreebankGoldAnnotator.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION);
		AnalysisEngine treebankParser = AnalysisEngineFactory.createAnalysisEngine(treebankParserDescription, ViewNames.GOLD_VIEW);

		AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(XWriter.class, CleartkComponents.TYPE_SYSTEM_DESCRIPTION,
				XWriter.PARAM_OUTPUT_DIRECTORY_NAME, options.outputDirectory);

		SimplePipeline.runPipeline(reader, viewCreator, treebankParser, xWriter);

	}

}
