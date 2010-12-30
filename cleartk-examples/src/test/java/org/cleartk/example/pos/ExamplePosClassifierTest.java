/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.example.pos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.classifier.CleartkComponents;
import org.cleartk.classifier.jar.JarDataWriterFactory;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.classifier.mallet.DefaultMalletDataWriterFactory;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.svmlight.DefaultOVASVMlightDataWriterFactory;
import org.cleartk.classifier.viterbi.ViterbiClassifier;
import org.cleartk.example.ExamplesTestBase;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.examples.pos.ExamplePOSAnnotator;
import org.cleartk.examples.pos.ExamplePOSPlainTextWriter;
import org.cleartk.syntax.constituent.TreebankGoldAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.ViewNames;
import org.cleartk.util.cr.FilesCollectionReader;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class ExamplePosClassifierTest extends ExamplesTestBase{

	private static final String RUN_LONG_TESTS_PROP = "cleartk.longtests";
	private static final boolean RUN_LONG_TESTS = System.getProperty(RUN_LONG_TESTS_PROP) != null;
	private static final String LONG_TEST_FORMAT = String.format(
			"Skipping test because training takes ~%%s. To run this test, supply -D%s at the " +
			"command line.", RUN_LONG_TESTS_PROP);
	private static final Logger LOGGER = Logger.getLogger(ExamplePosClassifierTest.class.getName());
	
	
	@Test
	public void testLibsvm() throws Exception {
		String outDirectoryName = outputDirectoryName+"/libsvm";
		
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultMultiClassLIBSVMDataWriterFactory.class,
				outDirectoryName,
				
				JarDataWriterFactory.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM, false);

		testClassifier(dataWriter, outDirectoryName, 1, "-t", "0"); //MultiClassLIBSVMClassifier.score is not implemented so we cannot have a stack size greater than 1.
		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
		boolean badTags = firstLine.equals("2008/NN Sichuan/NN earthquake/NN From/NN Wikipedia/NN ,/NN the/NN free/NN encyclopedia/NN");
		assertFalse(badTags);
		assertEquals("2008/NN Sichuan/NN earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN", firstLine);
		
	}

	@Test
	public void testMalletCRF() throws Exception {
		if(!RUN_LONG_TESTS) {
			LOGGER.info(String.format(LONG_TEST_FORMAT, "2 minutes"));
			return;
		}

		
		String outDirectoryName = outputDirectoryName+"/malletcrf"; 
		AnalysisEngineDescription dataWriter = CleartkComponents.createCleartkSequentialAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultMalletCRFDataWriterFactory.class,
				outDirectoryName,
				(List<Class<?>>) null);
		testClassifier(dataWriter, outDirectoryName, -1); //viterbi stack size is meaningless here so pass in an invalid value to make sure it is ignored.

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
		assertEquals("2008/NN Sichuan/JJ earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN", firstLine);
		
	}

	@Test
	public void testMalletCRF2() throws Exception {
		if(!RUN_LONG_TESTS) {
			LOGGER.info(String.format(LONG_TEST_FORMAT, "2 minutes"));
			return;
		}

		String outDirectoryName = outputDirectoryName+"/malletcrf-compressed"; 
		AnalysisEngineDescription dataWriter = CleartkComponents.createCleartkSequentialAnnotator(
					ExamplePOSAnnotator.class, 
					ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
					DefaultMalletCRFDataWriterFactory.class, outDirectoryName, (List<Class<?>>) null);
		AnalysisEngineFactory.setConfigurationParameters(dataWriter, DefaultMalletCRFDataWriterFactory.PARAM_COMPRESS, true);
		testClassifier(dataWriter, outDirectoryName, -1); //viterbi stack size is meaningless here so pass in an invalid value to make sure it is ignored.
		
		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
		assertEquals("2008/IN Sichuan/JJ earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN", firstLine);
	}

	@Test
	public void testMaxent() throws Exception {
		String outDirectoryName = outputDirectoryName+"/maxent"; 
		
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultMaxentDataWriterFactory.class,
				outDirectoryName);
		testClassifier(dataWriter, outDirectoryName, 10);

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0];
		assertEquals("2008/CD Sichuan/JJ earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN", firstLine);
	}

	@Test
	public void testMaxent2() throws Exception {
		String outDirectoryName = outputDirectoryName+"/maxent2"; 
		
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultMaxentDataWriterFactory.class,
				outDirectoryName,
				DefaultMaxentDataWriterFactory.PARAM_COMPRESS, true);
		testClassifier(dataWriter, outDirectoryName, 10);

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0];
		assertEquals("2008/CD Sichuan/JJ earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN", firstLine);
	}

	@Test
	public void testMalletMaxent() throws Exception {
		String outDirectoryName = outputDirectoryName+"/mallet-maxent"; 
		
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultMalletDataWriterFactory.class,
				outDirectoryName);
		testClassifier(dataWriter, outDirectoryName, 10, "MaxEnt");

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0];
		assertEquals("2008/DT Sichuan/JJ earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN", firstLine);
	}

	@Test
	public void testMalletNaiveBayes() throws Exception {
		String outDirectoryName = outputDirectoryName+"/mallet-naive-bayes"; 
		
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultMalletDataWriterFactory.class,
				outDirectoryName);
		testClassifier(dataWriter, outDirectoryName, 10, "NaiveBayes");

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0];
		assertEquals("2008/DT Sichuan/JJ earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN", firstLine);
	}

	@Test
	public void testMalletNaiveBayes2() throws Exception {
		String outDirectoryName = outputDirectoryName+"/mallet-naive-bayes"; 
		
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultMalletDataWriterFactory.class,
				outDirectoryName, 
				DefaultMalletDataWriterFactory.PARAM_COMPRESS, true
				);
		testClassifier(dataWriter, outDirectoryName, 10, "NaiveBayes");

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0];
		assertEquals("2008/DT Sichuan/JJ earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN", firstLine);
	}

	@Test
	public void testMalletC45() throws Exception {
		if(!RUN_LONG_TESTS) {
			LOGGER.info(String.format(LONG_TEST_FORMAT, "20 minutes"));
			return;
		}
		String outDirectoryName = outputDirectoryName+"/mallet-c45"; 
		
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, ExampleComponents.TYPE_SYSTEM_DESCRIPTION,  DefaultMalletDataWriterFactory.class,
				outDirectoryName);
		testClassifier(dataWriter, outDirectoryName, 10, "C45");

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0];
		assertEquals("2008/CD Sichuan/JJ earthquake/NN From/NN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN", firstLine);
	}

	@Test
	public void testSVMLIGHT() throws Exception {
		String outDirectoryName = outputDirectoryName+"/svmlight";
		AnalysisEngineDescription dataWriter = CleartkComponents.createViterbiAnnotator(
				ExamplePOSAnnotator.class, 
				ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
				DefaultOVASVMlightDataWriterFactory.class,
				outDirectoryName,
				DefaultOVASVMlightDataWriterFactory.PARAM_CUTOFF, 1);

		testClassifier(dataWriter, outDirectoryName, 1);

		String firstLine = FileUtil.loadListOfStrings(new File(outDirectoryName + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
		boolean badTags = firstLine.equals("2008/NN Sichuan/NN earthquake/NN From/NN Wikipedia/NN ,/NN the/NN free/NN encyclopedia/NN");
		assertFalse(badTags);
		
		assertEquals("2008/CD Sichuan/JJ earthquake/NNS From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN", firstLine);
	}

	
	
	
	private void testClassifier(AnalysisEngineDescription dataWriter, String outDirectoryName, int stackSize, String... trainingArgs) throws Exception {

		SimplePipeline.runPipeline(
				FilesCollectionReader.getCollectionReaderWithView(ExampleComponents.TYPE_SYSTEM_DESCRIPTION, "src/test/resources/data/treebank/11597317.tree", ViewNames.TREEBANK),
				TreebankGoldAnnotator.getDescriptionPOSTagsOnly(),
				DefaultSnowballStemmer.getDescription("English"), 
				dataWriter);
		
		String[] args;
		if(trainingArgs != null && trainingArgs.length > 0) {
			args = new String[trainingArgs.length + 1];
			args[0] = outDirectoryName;
			System.arraycopy(trainingArgs, 0, args, 1, trainingArgs.length);
		} else {
			args = new String[] { outDirectoryName };
		}
		
		HideOutput hider = new HideOutput();
		try {
			org.cleartk.classifier.jar.Train.main(args);
		} finally {
			hider.restoreOutput();
		}

		
		AnalysisEngineDescription taggerDescription = ExamplePOSAnnotator.getClassifierDescription(outDirectoryName + "/model.jar");
		AnalysisEngineFactory.setConfigurationParameters(taggerDescription, ViterbiClassifier.PARAM_STACK_SIZE, stackSize);
		
		SimplePipeline.runPipeline(
				FilesCollectionReader.getCollectionReader(ExampleComponents.TYPE_SYSTEM_DESCRIPTION, "src/test/resources/data/2008_Sichuan_earthquake.txt"),
				ExampleComponents.getSentenceSegmenter(),
				TokenAnnotator.getDescription(),
				DefaultSnowballStemmer.getDescription("English"),
				taggerDescription, 
				AnalysisEngineFactory.createPrimitiveDescription(ExamplePOSPlainTextWriter.class, ExampleComponents.TYPE_SYSTEM_DESCRIPTION, ExamplePOSPlainTextWriter.PARAM_OUTPUT_DIRECTORY_NAME, outDirectoryName));
	}

}
