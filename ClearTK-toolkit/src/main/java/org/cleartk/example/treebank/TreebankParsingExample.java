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
