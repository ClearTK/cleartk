package org.cleartk.example.documentclassification;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.extractor.CountsExtractor;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.type.Token;

public class AnnotationHandler implements org.cleartk.classifier.AnnotationHandler<String> {

	public static final String PREDICTION_VIEW_NAME = "ExampleDocumentClassificationPredictionView";

	public void initialize(UimaContext context) throws ResourceInitializationException {
		SimpleFeatureExtractor subExtractor = new TypePathExtractor(Token.class, "stem", false, false, true);
		extractor = new CountsExtractor(Token.class, subExtractor);
	}

	public void process(JCas jCas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
		try {
			DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();

			Instance<String> instance = new Instance<String>();
			instance.addAll(extractor.extract(jCas, doc));

			if( consumer.expectsOutcomes() ) {
				JCas goldView = jCas.getView(GoldAnnotator.GOLD_VIEW_NAME);
				instance.setOutcome(goldView.getSofaDataString());
			}

			String result = consumer.consume(instance);
			
			if( result != null ) {
				JCas predictionView = jCas.createView(PREDICTION_VIEW_NAME);
				predictionView.setSofaDataString(result, "text/plain");
			}
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private CountsExtractor extractor;

}
