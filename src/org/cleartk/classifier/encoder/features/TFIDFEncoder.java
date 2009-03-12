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
package org.cleartk.classifier.encoder.features;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeatureEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.features.normalizer.EuclidianNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.feature.Counts;


/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * Encodes a Counts feature into TF-IDF values.
 * 
 * A normalizer can be given, which will be applied to the list of
 * encoded features. If no normalizer is given, it defaults to a
 * euclidian normalizer.
 * 
 * If a name is supplied this encoder will only dispatch on features
 * of that name.
 * 
 * @author Philipp Wetzler
 */
public class TFIDFEncoder implements FeatureEncoder<NameNumber> {

	private static final long serialVersionUID = -5280514188425612793L;
	
	public TFIDFEncoder(String name, File idfFile, NameNumberNormalizer normalizer) throws IOException {
		this.name = name;
		this.normalizer = normalizer;
		
		ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(idfFile)));
		inverseDocumentFrequencies = readObject(input);
		input.close();
		
		if (! inverseDocumentFrequencies.containsKey(null)) {
			String message = "IDF map must contain a null key for unseen terms";
			throw new IllegalArgumentException(message);
		}
	}

	public TFIDFEncoder(String name, File idfFile) throws IOException {
		this(name, idfFile, new EuclidianNormalizer());
	}

	public TFIDFEncoder(File idfFile, NameNumberNormalizer normalizer) throws IOException {
		this(null, idfFile, normalizer);
	}

	public TFIDFEncoder(File idfFile) throws IOException {
		this(null, idfFile, new EuclidianNormalizer());
	}
	
	public List<NameNumber> encode(Feature feature) {
		List<NameNumber> fves = new ArrayList<NameNumber>();
		Counts counts = (Counts) feature.getValue();
		String prefix = Feature.createName(feature.getName(), "TFIDF", counts.getFeatureName());
		double total = counts.getTotalCount();

		for( Object value : counts.getValues() ) {
			String name = Feature.createName(prefix, value.toString());
			
			double frequency = counts.getCount(value) / total;
			String key = value.toString();
			if( ! inverseDocumentFrequencies.containsKey(key) )
				key = null;
			double idf = inverseDocumentFrequencies.get(key);
			
			NameNumber fve = new NameNumber(name, frequency * idf);
			fves.add(fve);
		}

		normalizer.normalize(fves);

		return fves;
	}

	public boolean encodes(Feature feature) {
		if( name != null && ! name.equals(feature.getName()) )
			return false;

		return feature.getValue() instanceof Counts;
	}

	@SuppressWarnings("unchecked")
	private static Map<String,Double> readObject(ObjectInput input) throws IOException {
		try {
			return (Map<String, Double>) input.readObject();
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private String name;
	private NameNumberNormalizer normalizer;
	private Map<String,Double> inverseDocumentFrequencies;

}
