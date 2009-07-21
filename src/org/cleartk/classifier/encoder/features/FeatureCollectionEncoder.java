package org.cleartk.classifier.encoder.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeatureEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.features.normalizer.NOPNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.feature.FeatureCollection;

public class FeatureCollectionEncoder implements FeatureEncoder<NameNumber> {

	private static final long serialVersionUID = -7840242678514710238L;
	
	public FeatureCollectionEncoder(String identifier, FeatureEncoder<NameNumber> subEncoder, NameNumberNormalizer normalizer) {
		this.identifier = identifier;
		this.normalizer = normalizer;
		this.subEncoder = subEncoder;
	}
	
	public FeatureCollectionEncoder(FeatureEncoder<NameNumber> subEncoder, NameNumberNormalizer normalizer) {
		this(null, subEncoder, normalizer);
	}
	
	public FeatureCollectionEncoder(String name, FeatureEncoder<NameNumber> subEncoder) {
		this(name, subEncoder, new NOPNormalizer());
	}

	public FeatureCollectionEncoder(FeatureEncoder<NameNumber> subEncoder) {
		this(null, subEncoder, new NOPNormalizer());
	}

	public List<NameNumber> encode(Feature feature) throws IllegalArgumentException {
		FeatureCollection fc = (FeatureCollection) feature.getValue();
		List<NameNumber> fves = new ArrayList<NameNumber>();
		
		if( identifier != null && ! identifier.equals(fc.getIdentifier()) )
			return Collections.emptyList();
		
		for( Feature f : fc.getFeatures() ) {
			f.setName(Feature.createName(feature.getName(), f.getName()));
			fves.addAll(subEncoder.encode(f));
		}
		
		normalizer.normalize(fves);
		
		return fves;
	}

	public boolean encodes(Feature feature) {
		if( feature.getValue() instanceof FeatureCollection ) {
			FeatureCollection f = (FeatureCollection) feature.getValue();
			
			if( identifier == null || identifier.equals(f.getIdentifier()) )
				return true;
			else
				return false;
		}
		
		return false;
	}
	
	private String identifier;
	private NameNumberNormalizer normalizer = new NOPNormalizer();	
	private FeatureEncoder<NameNumber> subEncoder = null;
}
