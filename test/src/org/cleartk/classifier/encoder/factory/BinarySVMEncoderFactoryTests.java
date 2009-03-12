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
package org.cleartk.classifier.encoder.factory;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.UimaContext;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.junit.Test;
import org.uutuc.factory.UimaContextFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class BinarySVMEncoderFactoryTests {

	
	@Test
	public void testFeatureVectorFeatureEncoder() throws Exception {
		List<FeatureVector.Entry> entries;
		
		UimaContext context = UimaContextFactory.createUimaContext();
		EncoderFactory factory = new BinarySVMEncoderFactory();
		FeaturesEncoder<?> encoder = factory.createFeaturesEncoder(context);

		// add a numeric valued feature
		entries = this.getVector(encoder, new Feature("A", 1));
		assertEquals(1, entries.size());
		assertEquals(1, entries.get(0).index);
		assertEquals(1.0, entries.get(0).value, 0.01d);
		
		// use a boolean value with an existing feature
		entries = this.getVector(encoder, new Feature("A", false));
		assertEquals(0, entries.size());

		// add a string valued feature
		entries = this.getVector(encoder, new Feature("B", "spam"));
		assertEquals(1, entries.size());
		assertEquals(2, entries.get(0).index);
		assertEquals(1.0, entries.get(0).value, 0.01d);

		// add a string valued feature with the same name but a different value
		entries = this.getVector(encoder, new Feature("B", "eggs"));
		assertEquals(1, entries.size());
		assertEquals(3, entries.get(0).index);
		assertEquals(1.0, entries.get(0).value, 0.01d);
		
		// check that the A feature still works
		entries = this.getVector(encoder, new Feature("A", 0.5));
		assertEquals(1, entries.size());
		assertEquals(1, entries.get(0).index);
		assertEquals(0.5, entries.get(0).value, 0.01d);
		
		// check that the B_spam feature still works
		entries = this.getVector(encoder, new Feature("B_spam"));
		assertEquals(1, entries.size());
		assertEquals(2, entries.get(0).index);
		assertEquals(1.0, entries.get(0).value, 0.01d);

		// check that the B_eggs feature still works
		entries = this.getVector(encoder, new Feature("B", "eggs"));
		assertEquals(1, entries.size());
		assertEquals(3, entries.get(0).index);
		assertEquals(1.0, entries.get(0).value, 0.01d);
		
		// create a new B feature
		entries = this.getVector(encoder, new Feature("B", true));
		assertEquals(1, entries.size());
		assertEquals(4, entries.get(0).index);
		assertEquals(1, entries.get(0).value, 0.01d);
		
		// freeze the encoder and try to create a new value
		encoder.allowNewFeatures(false);
		entries = this.getVector(encoder, new Feature("C", true));
		assertEquals(0, entries.size());
		
	}
	
	@Test
	public void testNormalizeFeatures() throws Exception {
		UimaContext context = UimaContextFactory.createUimaContext(SVMEncoderFactory.PARAM_NORMALIZE_VECTORS, true);
		FeaturesEncoder<?> encoder = new BinarySVMEncoderFactory().createFeaturesEncoder(context);
		FeatureVector vector = (FeatureVector)encoder.encodeAll(Arrays.asList(new Feature[]{
				new Feature("A", 3),
				new Feature("B", 4)
		}));
		Assert.assertEquals(3./5., vector.get(1), 1e-10);
		Assert.assertEquals(4./5., vector.get(2), 1e-10);
		Assert.assertEquals(0, vector.get(3), 1e-10);

		vector = (FeatureVector)encoder.encodeAll(Arrays.asList(new Feature[]{
				new Feature("B", 4),
				new Feature("C", 2),
				new Feature("D", 6),
				new Feature("E", 2),
				new Feature("F", 2)
		}));
		Assert.assertEquals(0, vector.get(1), 0);
		Assert.assertEquals(4./8., vector.get(2), 0);
		Assert.assertEquals(2./8., vector.get(3), 0);
		Assert.assertEquals(6./8., vector.get(4), 0);
		Assert.assertEquals(2./8., vector.get(5), 0);
		Assert.assertEquals(2./8., vector.get(6), 0);
		Assert.assertEquals(0., vector.get(7), 0);
	}
	
	
	private List<FeatureVector.Entry> getVector(FeaturesEncoder<?> encoder, Feature feature) {
		List<FeatureVector.Entry> entries = new ArrayList<FeatureVector.Entry>();
		FeatureVector vector = (FeatureVector)encoder.encodeAll(
				Collections.singletonList(feature));
		for (FeatureVector.Entry entry: vector) {
			entries.add(entry);
		}
		return entries;
	}

}
