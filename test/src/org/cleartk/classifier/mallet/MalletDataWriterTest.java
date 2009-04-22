package org.cleartk.classifier.mallet;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.Train;
import org.cleartk.util.TestsUtil;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.util.HideOutput;

public class MalletDataWriterTest {

	
	public class TestHandler implements AnnotationHandler<String>{

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
			List<Feature> features = Arrays.asList(new Feature("pos","NN"), new Feature("distance", 3.0), new Feature("precision", 1.234));
			Instance<String> instance = new Instance<String>("A", features);
			consumer.consume(instance);

			features = Arrays.asList(new Feature("name","2PO"), new Feature("p's", 2));
			instance = new Instance<String>("B", features);
			consumer.consume(instance);

			features = Arrays.asList(new Feature("A_B","AB"));
			instance = new Instance<String>("A", features);
			consumer.consume(instance);

			instance = new Instance<String>("Z");
			consumer.consume(instance);

		}
		
	}

	private String outputDirectory1 = "test/data/mallet/mallet-data-writer";
	
	@Test
	public void test1() throws Exception {
		
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createAnalysisEngine(
				DataWriterAnnotator.class,
				TestsUtil.getTypeSystemDescription(),
				InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler.class.getName(),
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY,  outputDirectory1,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletDataWriterFactory.class.getName()
				);
	
		JCas jCas = TestsUtil.getJCas();
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();
		
		HideOutput hider = new HideOutput();
		Train.main(new String[] {outputDirectory1, "C45"});
		hider.restoreOutput();
	
	}
	
}
