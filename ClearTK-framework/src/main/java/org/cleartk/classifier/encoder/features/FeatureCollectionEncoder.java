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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.normalizer.NOPNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.feature.FeatureCollection;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * @author Philipp Wetzler
*/

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
			Feature f1 = new Feature(Feature.createName(feature.getName(), f.getName()), f.getValue());
			fves.addAll(subEncoder.encode(f1));
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
