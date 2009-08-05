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

package org.cleartk.classifier.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.CleartkException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.InstanceFactory;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.mallet.factory.ClassifierTrainerFactory;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.TearDownUtil;
/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */

public class MalletDataWriterTest {

	private String outputDirectory = "test/data/mallet/mallet-data-writer";

	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(new File(outputDirectory));
	}

	public class TestHandler1 implements AnnotationHandler<String> {

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException, CleartkException {
			List<Feature> features = Arrays.asList(new Feature("pos", "NN"), new Feature("distance", 3.0), new Feature(
					"precision", 1.234));
			Instance<String> instance = new Instance<String>("A", features);
			consumer.consume(instance);

			features = Arrays.asList(new Feature("name", "2PO"), new Feature("p's", 2));
			instance = new Instance<String>("B", features);
			consumer.consume(instance);

			instance = new Instance<String>("Z");
			consumer.consume(instance);

			features = Arrays.asList(new Feature("A_B", "AB"));
			instance = new Instance<String>("A", features);
			consumer.consume(instance);
		}
	}

	@Test
	public void test1() throws Exception {
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler1.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName());

		JCas jCas = TestsUtil.getJCas();
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();

		String[] lines = FileUtil
				.loadListOfStrings(new File(outputDirectory, MalletDataWriter.TRAINING_DATA_FILE_NAME));
		assertEquals("pos_NN:1.0 distance:3.0 precision:1.234 A", lines[0]);
		assertEquals("name_2PO:1.0 p's:2 B", lines[1]);
		assertEquals("null:0 Z", lines[2]);
		assertEquals("A_B_AB:1.0 A", lines[3]);

		//simply train four different models where each one writes over the previous
		HideOutput hider = new HideOutput();
		for(String classifierName : ClassifierTrainerFactory.NAMES) {
			Train.main(outputDirectory, classifierName);
		}
		hider.restoreOutput();
		
		IllegalArgumentException iae = null;
		try {
			Train.main(outputDirectory, "AutoTrophic");
		} catch(IllegalArgumentException e) {
			iae = e;
		}
		assertNotNull(iae);
		hider.restoreOutput();
	}

	/**
	 * This test is identical to test1 except that the features are compressed by NameNumberFeaturesEncoder.
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception {
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler1.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				DefaultMalletDataWriterFactory.PARAM_COMPRESS, true);

		JCas jCas = TestsUtil.getJCas();
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();

		String[] lines = FileUtil
				.loadListOfStrings(new File(outputDirectory, MalletDataWriter.TRAINING_DATA_FILE_NAME));
		assertEquals("0:1.0 1:3.0 2:1.234 A", lines[0]);
		assertEquals("3:1.0 4:2 B", lines[1]);
		assertEquals("null:0 Z", lines[2]);
		assertEquals("5:1.0 A", lines[3]);

		lines = FileUtil.loadListOfStrings(new File(outputDirectory, NameNumberFeaturesEncoder.LOOKUP_FILE_NAME));
		Set<String> lineSet = new HashSet<String>();
		for( int i=0; i<lines.length; i++)
			lineSet.add(lines[i]);
		assertEquals("6", lines[0]);
		assertTrue(lineSet.contains("6"));
		assertTrue(lineSet.contains("name_2PO\t3"));
		assertTrue(lineSet.contains("precision\t2"));
		assertTrue(lineSet.contains("distance\t1"));
		assertTrue(lineSet.contains("pos_NN\t0"));
		assertTrue(lineSet.contains("A_B_AB\t5"));
		assertTrue(lineSet.contains("p's\t4"));
		assertEquals(7, lineSet.size());

		HideOutput hider = new HideOutput();
		for(String classifierName : ClassifierTrainerFactory.NAMES) {
			Train.main(outputDirectory, classifierName);
		}
		hider.restoreOutput();
	}

	/**
	 * This test is identical to test2 except that the feature lookup file is sorted by NameNumberFeaturesEncoder.
	 * @throws Exception
	 */

	@Test
	public void test3() throws Exception {
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler1.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				DefaultMalletDataWriterFactory.PARAM_COMPRESS, true, DefaultMalletDataWriterFactory.PARAM_SORT_NAME_LOOKUP, true);

		JCas jCas = TestsUtil.getJCas();
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();

		String[] lines = FileUtil
				.loadListOfStrings(new File(outputDirectory, MalletDataWriter.TRAINING_DATA_FILE_NAME));
		assertEquals("0:1.0 1:3.0 2:1.234 A", lines[0]);
		assertEquals("3:1.0 4:2 B", lines[1]);
		assertEquals("null:0 Z", lines[2]);
		assertEquals("5:1.0 A", lines[3]);

		lines = FileUtil.loadListOfStrings(new File(outputDirectory, NameNumberFeaturesEncoder.LOOKUP_FILE_NAME));
		int i = 0;
		assertEquals("6", lines[i++]);
		assertEquals("A_B_AB	5", lines[i++]);
		assertEquals("distance	1", lines[i++]);
		assertEquals("name_2PO	3", lines[i++]);
		assertEquals("p's	4", lines[i++]);
		assertEquals("pos_NN	0", lines[i++]);
		assertEquals("precision	2", lines[i++]);

		HideOutput hider = new HideOutput();
		for(String classifierName : ClassifierTrainerFactory.NAMES) {
			Train.main(outputDirectory, classifierName);
		}
		hider.restoreOutput();
	}

	public class TestHandler4 implements AnnotationHandler<String> {

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException, CleartkException {
			List<Feature> features = Arrays.asList(new Feature("pos", "NN"), new Feature("distance", 3.0), new Feature(
					"precision", 1.234));
			Instance<String> instance = new Instance<String>(features);
			consumer.consume(instance);
		}

	}

	/**
	 * Here we test that an exception is thrown if an instance with no outcome
	 * @throws Exception
	 */
	@Test
	public void test4() throws Exception {
		String outputDirectory = "test/data/mallet/mallet-data-writer";

		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler4.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				DefaultMalletDataWriterFactory.PARAM_COMPRESS, true, DefaultMalletDataWriterFactory.PARAM_SORT_NAME_LOOKUP, true);

		JCas jCas = TestsUtil.getJCas();
		AnalysisEngineProcessException aepe = null;
		try {
			dataWriterAnnotator.process(jCas);
		}
		catch (AnalysisEngineProcessException e) {
			aepe = e;
		}
		assertNotNull(aepe);
	}

	public class TestHandler5 implements AnnotationHandler<String> {

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException, CleartkException {
			Instance<String> instance = InstanceFactory.createInstance("a", "b c d");
			consumer.consume(instance);
		}
	}

	/**
	 * This test is identical to test1 except that the features are compressed by NameNumberFeaturesEncoder.
	 * @throws Exception
	 */
	@Test
	public void test5() throws Exception {
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler5.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				DefaultMalletDataWriterFactory.PARAM_COMPRESS, true);

		JCas jCas = TestsUtil.getJCas();
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();

		String[] lines = FileUtil.loadListOfStrings(new File(outputDirectory, MalletDataWriter.TRAINING_DATA_FILE_NAME));
		assertEquals("0:1.0 1:1.0 2:1.0 a", lines[0]);
	}

}
