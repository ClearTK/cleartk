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
package org.cleartk.classifier.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.classifier.ClassifierFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.encoder.factory.ContextValueEncoderFactory;
import org.cleartk.util.EmptyAnnotator;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.HideOutput;

import cc.mallet.types.FeatureVector;
/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

* 
* @author Philip Ogren
*/

public class RunMalletTests {
	Random random;
	
	@Before
	public void setUp() {
		random = new Random(System.currentTimeMillis());
	}
	
	@Test
	public void runTest1() throws Exception {
		String outputDirectory = "test/data/mallet/run-test-1"; 
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(EmptyAnnotator.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"), 
				MalletDataWriter.PARAM_ANNOTATION_HANDLER,
				"org.cleartk.example.ExamplePOSAnnotationHandler",
				MalletDataWriter.PARAM_OUTPUT_DIRECTORY,
				outputDirectory);
		
		MalletDataWriter dataWriter = new MalletDataWriter();
		dataWriter.initialize(engine.getUimaContext());
		
		
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		
		
		for(int i=0; i<1000; i++)
			instances.add(generateInstance());
			
		dataWriter.consumeAll(instances);
		dataWriter.collectionProcessComplete();
		engine.collectionProcessComplete();
		
		BufferedReader reader = new BufferedReader(new FileReader(outputDirectory+"/training-data.mallet"));
		reader.readLine();
		reader.close();

		IllegalArgumentException exception = null;
		try {
			Train.main(new String[] {outputDirectory, "asdf"});
		} catch(IllegalArgumentException iae) {
			exception = iae;
		}
		assertNotNull(exception);
		
		exception = null;
		try {
			Train.main(new String[] {outputDirectory, "MaxEnt", "10", "asdf"});
		} catch(IllegalArgumentException iae) {
			exception = iae;
		}
		assertNotNull(exception);
		
		HideOutput hider = new HideOutput();
		Train.main(new String[] {outputDirectory, "C45"});
		hider.restoreOutput();

		MalletClassifier classifier = (MalletClassifier) ClassifierFactory.readFromJar(outputDirectory+"/model.jar");
		assertFalse(classifier.isSequential());
		
		List<List<Feature>> testFeatures = new ArrayList<List<Feature>>();
		
		//they aren't the same because MaxEnt can't discriminate on a single feature
		Instance<String> testInstance = new Instance<String>();
		testInstance.add(new Feature("hello", random.nextInt(1000)+1000));
		testFeatures.add(testInstance.getFeatures());
		String outcome = classifier.classify(testInstance.getFeatures());
		assertEquals("A", outcome);

		testInstance = new Instance<String>();
		testInstance.add(new Feature("hello", 95));
		testInstance.add(new Feature("goodbye", 95));
		testFeatures.add(testInstance.getFeatures());
		outcome = classifier.classify(testInstance.getFeatures());
		assertEquals("B", outcome);
		
		cc.mallet.types.Instance malletInstance = classifier.toInstance(testInstance.getFeatures());
		FeatureVector fv = (FeatureVector) malletInstance.getData();
		assertEquals(95.0, fv.value("hello"), 0.001);

		List<String> outcomes = classifier.classifySequence(testFeatures);
		assertEquals(2, outcomes.size());
		assertEquals("A", outcomes.get(0));
		assertEquals("B", outcomes.get(1));
	}

	private Instance<String> generateInstance(){
		Instance<String> instance = new Instance<String>();
		
		int outcome = random.nextInt(2);
		if(outcome == 0) {
			instance.setOutcome("A");
			instance.add(new Feature("hello", random.nextInt(1000)+1000));
		} else {
			instance.setOutcome("B");
			instance.add(new Feature("hello", random.nextInt(100)));
		}
		return instance;
	}

	@Test
	public void runTest2() throws Exception {
		String outputDirectory = "test/data/mallet/run-test-2"; 
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(EmptyAnnotator.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"), 
				MalletDataWriter.PARAM_ANNOTATION_HANDLER,
				"org.cleartk.example.ExamplePOSAnnotationHandler",
				MalletDataWriter.PARAM_OUTPUT_DIRECTORY,
				outputDirectory,
				ContextValueEncoderFactory.PARAM_COMPRESS,
				true);
		
		MalletDataWriter dataWriter = new MalletDataWriter();
		dataWriter.initialize(engine.getUimaContext());
		
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		
		
		for(int i=0; i<1000; i++)
			instances.add(generateInstance());
			
		dataWriter.consumeAll(instances);
		dataWriter.collectionProcessComplete();
		engine.collectionProcessComplete();

		BufferedReader reader = new BufferedReader(new FileReader(outputDirectory+"/training-data.mallet"));
		reader.readLine();
		reader.close();

		HideOutput hider = new HideOutput();
		Train.main(new String[] {outputDirectory, "C45"});
		hider.restoreOutput();
		
		MalletClassifier classifier = (MalletClassifier) ClassifierFactory.readFromJar(outputDirectory+"/model.jar");
		assertFalse(classifier.isSequential());
		
		List<List<Feature>> testFeatures = new ArrayList<List<Feature>>();
		
		//they aren't the same because MaxEnt can't discriminate on a single feature
		Instance<String> testInstance = new Instance<String>();
		testInstance.add(new Feature("hello", random.nextInt(1000)+1000));
		testFeatures.add(testInstance.getFeatures());
		String outcome = classifier.classify(testInstance.getFeatures());
		assertEquals("A", outcome);

		testInstance = new Instance<String>();
		testInstance.add(new Feature("hello", 95));
		testInstance.add(new Feature("goodbye", 95));
		testFeatures.add(testInstance.getFeatures());
		outcome = classifier.classify(testInstance.getFeatures());
		assertEquals("B", outcome);
		
		cc.mallet.types.Instance malletInstance = classifier.toInstance(testInstance.getFeatures());
		FeatureVector fv = (FeatureVector) malletInstance.getData();
		assertEquals(95.0, fv.value("0"), 0.001);

		List<String> outcomes = classifier.classifySequence(testFeatures);
		assertEquals(2, outcomes.size());
		assertEquals("A", outcomes.get(0));
		assertEquals("B", outcomes.get(1));

	}
}
