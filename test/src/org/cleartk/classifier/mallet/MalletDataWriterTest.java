package org.cleartk.classifier.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
import org.cleartk.classifier.encoder.factory.NameNumberEncoderFactory;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.mallet.factory.ClassifierTrainerFactory;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.opennlp.MaxentDataWriter;
import org.cleartk.classifier.opennlp.MaxentDataWriterTest.TestHandler5;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.TearDownUtil;

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
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createAnalysisEngine(DataWriterAnnotator.class,
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
			Train.main(new String[] { outputDirectory, classifierName });
		}
		hider.restoreOutput();
		
		IllegalArgumentException iae = null;
		try {
			Train.main(new String[] { outputDirectory, "AutoTrophic" });
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
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createAnalysisEngine(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler1.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				NameNumberEncoderFactory.PARAM_COMPRESS, true);

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
		assertEquals("name_2PO\t3", lines[i++]);
		assertEquals("precision\t2", lines[i++]);
		assertEquals("distance\t1", lines[i++]);
		assertEquals("pos_NN\t0", lines[i++]);
		assertEquals("A_B_AB\t5", lines[i++]);
		assertEquals("p's\t4", lines[i++]);

		HideOutput hider = new HideOutput();
		for(String classifierName : ClassifierTrainerFactory.NAMES) {
			Train.main(new String[] { outputDirectory, classifierName });
		}
		hider.restoreOutput();
	}

	/**
	 * This test is identical to test2 except that the feature lookup file is sorted by NameNumberFeaturesEncoder.
	 * @throws Exception
	 */

	@Test
	public void test3() throws Exception {
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createAnalysisEngine(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler1.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				NameNumberEncoderFactory.PARAM_COMPRESS, true, NameNumberEncoderFactory.PARAM_SORT_NAME_LOOKUP, true);

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
			Train.main(new String[] { outputDirectory, classifierName });
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

		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createAnalysisEngine(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler4.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				NameNumberEncoderFactory.PARAM_COMPRESS, true, NameNumberEncoderFactory.PARAM_SORT_NAME_LOOKUP, true);

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
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createAnalysisEngine(DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(), InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler5.class
						.getName(), DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName(),
				NameNumberEncoderFactory.PARAM_COMPRESS, true);

		JCas jCas = TestsUtil.getJCas();
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();

		String[] lines = FileUtil.loadListOfStrings(new File(outputDirectory, MalletDataWriter.TRAINING_DATA_FILE_NAME));
		assertEquals("0:1.0 1:1.0 2:1.0 a", lines[0]);
	}

}
