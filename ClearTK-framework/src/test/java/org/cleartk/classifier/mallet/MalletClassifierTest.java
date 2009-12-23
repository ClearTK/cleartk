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
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;
import java.util.jar.JarFile;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory_ImplBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.Train;
import org.cleartk.util.JCasUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.TearDownUtil;

import cc.mallet.types.FeatureVector;
/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

* 
* @author Philip Ogren
*/

public class MalletClassifierTest {
	private Random random;
	private String outputDirectory = "test/data/mallet/mallet-classifier";
	
	@Before
	public void setUp() {
		random = new Random(System.currentTimeMillis());
	}

	@After
	public void tearDown() throws Exception {
		File outputDirectory = new File(this.outputDirectory);
		TearDownUtil.removeDirectory(outputDirectory);
		Assert.assertFalse(outputDirectory.exists());
	}

	private static Instance<String> generateInstance(Random random){
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

	
	public static class TestAnnotator extends CleartkAnnotator<String>{
		Random random = new Random(System.currentTimeMillis());

		public void process(JCas cas) throws AnalysisEngineProcessException {
			try {
				this.processSimple(cas);
			} catch (CleartkException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
		public void processSimple(JCas cas) throws AnalysisEngineProcessException, CleartkException {
			if (this.isTraining()) {
				for(int i=0; i<1000; i++) {
					this.dataWriter.write(generateInstance(random));
				}
			} else {
				Instance<String> testInstance = new Instance<String>();
				testInstance.add(new Feature("hello", random.nextInt(1000)+1000));
				String outcome = this.classifier.classify(testInstance.getFeatures());
				assertEquals("A", outcome);

				testInstance = new Instance<String>();
				testInstance.add(new Feature("hello", 95));
				outcome = this.classifier.classify(testInstance.getFeatures());
				assertEquals("B", outcome);
			}
		}
	}
	
	@Test
	public void runTest1() throws Exception {
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
				TestAnnotator.class, JCasUtil.getTypeSystemDescription(),
				DataWriterFactory_ImplBase.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, DefaultMalletDataWriterFactory.class.getName());

		JCas jCas = JCasUtil.getJCas();
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();

		BufferedReader reader = new BufferedReader(new FileReader(new File(outputDirectory, MalletDataWriter.TRAINING_DATA_FILE_NAME)));
		reader.readLine();
		reader.close();

		IllegalArgumentException exception = null;
		try {
			Train.main(outputDirectory, "asdf");
		} catch(IllegalArgumentException iae) {
			exception = iae;
		}
		assertNotNull(exception);
		
		exception = null;
		try {
			Train.main(outputDirectory, "MaxEnt", "10", "asdf");
		} catch(IllegalArgumentException iae) {
			exception = iae;
		}
		assertNotNull(exception);
		
		HideOutput hider = new HideOutput();
		Train.main(new String[] {outputDirectory, "C45"});
		hider.restoreOutput();

		JarFile modelFile = new JarFile(new File(outputDirectory, "model.jar"));
		MalletClassifier classifier = new MalletClassifier(modelFile);
		modelFile.close();
		
		Instance<String> testInstance = new Instance<String>();
		testInstance.add(new Feature("hello", random.nextInt(1000)+1000));
		String outcome = classifier.classify(testInstance.getFeatures());
		assertEquals("A", outcome);

		testInstance = new Instance<String>();
		testInstance.add(new Feature("hello", 95));
		outcome = classifier.classify(testInstance.getFeatures());
		assertEquals("B", outcome);
		
		cc.mallet.types.Instance malletInstance = classifier.toInstance(testInstance.getFeatures());
		FeatureVector fv = (FeatureVector) malletInstance.getData();
		assertEquals(95.0, fv.value("hello"), 0.001);
		
		AnalysisEngine classifierAnnotator = AnalysisEngineFactory.createPrimitive(
				TestAnnotator.class, JCasUtil.getTypeSystemDescription(),
				CleartkAnnotator.PARAM_CLASSIFIER_JAR_PATH, outputDirectory+"/model.jar");
		jCas.reset();
		classifierAnnotator.process(jCas);
		classifierAnnotator.collectionProcessComplete();

	}

}
