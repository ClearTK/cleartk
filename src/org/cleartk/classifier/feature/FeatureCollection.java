package org.cleartk.classifier.feature;

import java.util.Collection;

import org.cleartk.classifier.Feature;

public class FeatureCollection {
	
	public FeatureCollection(String identifier, Collection<Feature> features) {
		this.identifier = identifier;
		this.features = features;
	}
	
	public FeatureCollection(Collection<Feature> features) {
		this(null, features);
	}
	
	public Collection<Feature> getFeatures() {
		return features;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}

	private Collection<Feature> features;
	private String identifier;

}
