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

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.CleartkComponents;
import org.cleartk.ViewNames;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.svmlight.DefaultOVASVMlightDataWriterFactory;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.snowball.DefaultSnowballStemmer;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.junit.After;
import org.junit.Test;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.SimplePipeline;
import org.uutuc.util.TearDownUtil;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class NonSequentialExamplePOSAnnotationHandlerTest {

	private String baseDirectory = "test/data/example/pos/nonsequential";
	
	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(new File(baseDirectory));
	}
	
	@Test
	public void testLibsvm() throws Exception {
		System.out.println(" running test org.cleartk.example.pos.NonSequentialExamplePOSAnnotationHandlerTest.testLibsvm()");

		String outputDirectory = baseDirectory+"/libsvm";
		AnalysisEngineDescription dataWriter = 	CleartkComponents.createDataWriterAnnotator(NonSequentialExamplePOSAnnotationHandler.class,
				DefaultMultiClassLIBSVMDataWriterFactory.class, outputDirectory);
		testClassifier(dataWriter, outputDirectory, "-t", "0");

		String firstLine = FileUtil.loadListOfStrings(new File(outputDirectory + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
		boolean badTags = firstLine.equals("2008/NN Sichuan/NN earthquake/NN From/NN Wikipedia/NN ,/NN the/NN free/NN encyclopedia/NN");
		assertFalse(badTags);
		assertEquals("2008/NN Sichuan/NN earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN", firstLine);
	}

	@Test
	public void testMaxent() throws Exception {
		System.out.println(" running test org.cleartk.example.pos.NonSequentialExamplePOSAnnotationHandlerTest.testMaxent()");

		String outputDirectory = baseDirectory+"/maxent";
		AnalysisEngineDescription dataWriter = 	CleartkComponents.createDataWriterAnnotator(NonSequentialExamplePOSAnnotationHandler.class,
				DefaultMaxentDataWriterFactory.class, outputDirectory);
		testClassifier(dataWriter, outputDirectory);

		String firstLine = FileUtil.loadListOfStrings(new File(outputDirectory + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
		boolean badTags = firstLine.equals("2008/NN Sichuan/NN earthquake/NN From/NN Wikipedia/NN ,/NN the/NN free/NN encyclopedia/NN");
		assertFalse(badTags);
		
		assertEquals("2008/PRP Sichuan/MD earthquake/NNS From/IN Wikipedia/NNS ,/, the/DT free/NN encyclopedia/NN", firstLine);

	}
	
	@Test
	public void testSVMLIGHT() throws Exception {
		System.out.println(" running test org.cleartk.example.pos.NonSequentialExamplePOSAnnotationHandlerTest.testSVMLIGHT()");

		String outputDirectory = baseDirectory+"/svmlight";
		AnalysisEngineDescription dataWriter = 	CleartkComponents.createDataWriterAnnotator(NonSequentialExamplePOSAnnotationHandler.class,
				DefaultOVASVMlightDataWriterFactory.class, outputDirectory);
		testClassifier(dataWriter, outputDirectory);

		String firstLine = FileUtil.loadListOfStrings(new File(outputDirectory + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
		boolean badTags = firstLine.equals("2008/NN Sichuan/NN earthquake/NN From/NN Wikipedia/NN ,/NN the/NN free/NN encyclopedia/NN");
		assertFalse(badTags);
		
		assertEquals("2008/CD Sichuan/NNP earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/NN", firstLine);

		
		
	}


	private void testClassifier(AnalysisEngineDescription dataWriter, String outputDirectory, String... trainingArgs) throws Exception {
		TypeSystemDescription typeSystemDescription = CleartkComponents.TYPE_SYSTEM_DESCRIPTION;
		
		System.out.print("creating training data...");
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createCollectionReader(
						FilesCollectionReader.class, typeSystemDescription,
						FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, "test/data/docs/treebank/11597317.tree",
						FilesCollectionReader.PARAM_VIEW_NAME, ViewNames.TREEBANK),
				CleartkComponents.createPrimitiveDescription(TreebankGoldAnnotator.class, TreebankGoldAnnotator.PARAM_POST_TREES, false),
				CleartkComponents.createPrimitiveDescription(DefaultSnowballStemmer.class, SnowballStemmer.PARAM_STEMMER_NAME, "English"),
				dataWriter);
		System.out.println("done");
				
		String[] args;
		if(trainingArgs != null && trainingArgs.length > 0) {
			args = new String[trainingArgs.length + 1];
			args[0] = outputDirectory;
			System.arraycopy(trainingArgs, 0, args, 1, trainingArgs.length);
		} else {
			args = new String[] { outputDirectory };
		}

		System.out.print("training model...");		
		HideOutput hider = new HideOutput();
		org.cleartk.classifier.Train.main(args);
		hider.restoreOutput();
		System.out.println("done");

		AnalysisEngineDescription taggerDescription = CleartkComponents.createClassifierAnnotator(NonSequentialExamplePOSAnnotationHandler.class, outputDirectory + "/model.jar");

		System.out.print("tagging data...");
		SimplePipeline.runPipeline(
				CleartkComponents.createFilesCollectionReader("example/data/2008_Sichuan_earthquake.txt"),
				CleartkComponents.createOpenNLPSentenceSegmenter(),
				CleartkComponents.createPrimitiveDescription(TokenAnnotator.class), 
				CleartkComponents.createPrimitiveDescription(DefaultSnowballStemmer.class, SnowballStemmer.PARAM_STEMMER_NAME, "English"),
				taggerDescription,
				CleartkComponents.createPrimitiveDescription(ExamplePOSPlainTextWriter.class, ExamplePOSPlainTextWriter.PARAM_OUTPUT_DIRECTORY, outputDirectory));
		
		System.out.println("done");

	}
}
