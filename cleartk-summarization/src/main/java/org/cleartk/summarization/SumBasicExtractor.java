package org.cleartk.summarization;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.transform.TrainableExtractor_ImplBase;

public class SumBasicExtractor extends TrainableExtractor_ImplBase<Double> implements SimpleFeatureExtractor {
	
	private boolean isTrained;

	public SumBasicExtractor(String name, SimpleFeatureExtractor tokenCountsExtractor) {
		super(name);
	}

	public SumBasicExtractor(String name, SimpleFeatureExtractor tokenCountsExtractor, Set<String> seenWords) {
		super(name);
	}
	
	@Override
	public void train(Iterable<Instance<Double>> instances) {
		
	}

	@Override
	public void save(URI uri) throws IOException {
		// save out a file containing word counts
		
	}

	@Override
	public void load(URI uri) throws IOException {
		// load a file containing word counts
		
	}

	@Override
	public Instance<Double> transform(Instance<Double> instance) {
	
		return null;
	}
	
	@Override
	public List<Feature> extract(JCas view, Annotation focusAnnotation)
			throws CleartkExtractorException {	
		
		List<Feature> features = new ArrayList<Feature>();
		if (this.isTrained) {
			
		} else {
			// convert word features into a sentence score
		}
		return features;
		
	}
	
}
