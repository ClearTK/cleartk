package org.cleartk.example.documentclassification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

public class Evaluator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			JCas goldView = jCas.getView(GoldAnnotator.GOLD_VIEW_NAME);
			JCas predictionView = jCas.getView(AnnotationHandler.PREDICTION_VIEW_NAME);

			String gold = goldView.getSofaDataString();
			String prediction = predictionView.getSofaDataString();

			classes.add(gold);
			classes.add(prediction);

			int value = get(gold, prediction);
			set(gold, prediction, value+1);
		}
		catch (CASException e) {
			throw new AnalysisEngineProcessException();
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		try {
			super.collectionProcessComplete();

			System.out.print("        ");
			for( String cp : classes ) {
				System.out.format("%7.7s ", cp);
			}
			System.out.println();

			for( String cg : classes ) {
				System.out.format("%7.7s ", cg);
				for( String cp : classes ) {
					System.out.format("%7d ", get(cg, cp));
				}
				System.out.println();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private int get(String gold, String prediction) {
		if( confusionMatrix.containsKey(gold) ) {
			Map<String, Integer> m = confusionMatrix.get(gold);
			if( m.containsKey(prediction) )
				return m.get(prediction);
			else
				return 0;
		} else {
			return 0;
		}
	}

	private void set(String gold, String prediction, int value) {
		if( confusionMatrix.containsKey(gold) ) {
			Map<String, Integer> m = confusionMatrix.get(gold);
			m.put(prediction, value);
		} else {
			Map<String, Integer> m = new HashMap<String, Integer>();
			m.put(prediction, value);
			confusionMatrix.put(gold, m);
		}
	}

	private Set<String> classes = new TreeSet<String>();
	private Map<String, Map<String, Integer>> confusionMatrix = new HashMap<String, Map<String, Integer>>();

}
