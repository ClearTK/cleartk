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


import org.cleartk.classifier.Feature;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.SparseFeatureVector;
import org.cleartk.util.StringIndex;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class FeatureVectorFeaturesEncoder extends FeaturesEncoder_ImplBase<FeatureVector, NameNumber> {
	
	private static final long serialVersionUID = 6714456694285732480L;

	@Override
	public FeatureVector encodeAll(Iterable<Feature> features) {
		SparseFeatureVector fv = new SparseFeatureVector();
		
		for( Feature feature : features ) {
			for( NameNumber nameValue : this.encode(feature) ) {
				String name = nameValue.name;
				Number number = nameValue.number;

				if(number.doubleValue() == 0.0 ) {
					continue;
				}
				if( stringIndex.contains(name) ) {
					int i = stringIndex.find(name);
					double v = fv.get(i) + number.doubleValue();
					fv.set(i, v);
				} else if( expandIndex ) {
					stringIndex.insert(name);
					int i = stringIndex.find(name);
					double v = fv.get(i) + number.doubleValue();
					fv.set(i, v);
				}
			}
		}

		return fv;
	}
	
	@Override
	public void allowNewFeatures(boolean flag) {
		expandIndex = flag;
	}

	private boolean expandIndex = true;
	private StringIndex stringIndex = new StringIndex(1);
	
}
