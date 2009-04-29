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

package org.cleartk.classifier.viterbi;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.util.FileUtils;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.SequentialInstanceConsumer;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.example.pos.ExamplePOSAnnotationHandler;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.TestsUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.util.HideOutput;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 */

public class ViterbiDataWriterTest {
	
	private String outputDirectory = "test/data/viterbi";
	
//	@After
//	public void tearDown() {
//		TearDownUtil.removeDirectory(new File(outputDirectory));
//	}
	
	@Test
	public void testConsumeAll() throws Exception {

		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(SequentialDataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(),
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER, ExamplePOSAnnotationHandler.class.getName(),
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, ViterbiDataWriterFactory.class.getName(),
				ViterbiDataWriter.PARAM_DELEGATED_DATAWRITER_FACTORY_CLASS, DefaultMaxentDataWriterFactory.class.getName(),
				ViterbiDataWriter.PARAM_OUTCOME_FEATURE_EXTRACTORS, new String[] {"org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor"});

		JCas jCas = engine.newJCas();
		String text = "Do I really have to come up with some creative text, or can I just write anything?";
		TokenFactory.createTokens(jCas, text, Token.class, Sentence.class,
				"Do I really have to come up with some creative text , or can I just write anything ?",
				"D I R H T C U W S C T , O C I J W A ?", null, "org.cleartk.type.Token:pos", null);

		engine.process(jCas);
		engine.collectionProcessComplete();

		String expectedManifest = 
			       "Manifest-Version: 1.0\n"
				+ "classifierBuilderClass: org.cleartk.classifier.viterbi.ViterbiClassifi\n"  
				+" erBuilder";
				
		File manifestFile = new File(outputDirectory, "MANIFEST.MF");
		String actualManifest = FileUtils.file2String(manifestFile);
		Assert.assertEquals(expectedManifest, actualManifest.replaceAll("\r", "").trim());

		File delegatedOutputDirectory = new File(outputDirectory, ViterbiDataWriter.DELEGATED_MODEL_DIRECTORY_NAME);
		String[] trainingData = FileUtil.loadListOfStrings(new File(delegatedOutputDirectory, "training-data.maxent"));
		testFeatures(trainingData[1], "PreviousOutcome_L1_D");
		testFeatures(trainingData[2], "PreviousOutcome_L1_I", "PreviousOutcome_L2_D", "PreviousOutcomes_L1_2gram_L2R_I_D");
		testFeatures(trainingData[3], "PreviousOutcome_L1_R", "PreviousOutcome_L2_I", "PreviousOutcome_L3_D", "PreviousOutcomes_L1_2gram_L2R_R_I", "PreviousOutcomes_L1_3gram_L2R_R_I_D");
		testFeatures(trainingData[4], "PreviousOutcome_L1_H", "PreviousOutcome_L2_R", "PreviousOutcome_L3_I", "PreviousOutcomes_L1_2gram_L2R_H_R", "PreviousOutcomes_L1_3gram_L2R_H_R_I");
		
		HideOutput hider = new HideOutput();
		Train.main(new String[] {outputDirectory+"/", "10", "1"});
		hider.restoreOutput();
		
		engine = AnalysisEngineFactory.createAnalysisEngine(SequentialClassifierAnnotator.class, 
				TestsUtil.getTypeSystemDescription(),
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER, ExamplePOSAnnotationHandler.class.getName(),
				SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, "model.jar").getPath());
		
		engine.process(jCas);
		engine.collectionProcessComplete();
		
	}
	
	private void testFeatures(String trainingDataLine, String...expectedFeatures) {
		Set<String> features = new HashSet<String>();
		features.addAll(Arrays.asList(trainingDataLine.split(" ")));
		for(String expectedFeature : expectedFeatures) {
			assertTrue(features.contains(expectedFeature));
		}
	}
	
}
