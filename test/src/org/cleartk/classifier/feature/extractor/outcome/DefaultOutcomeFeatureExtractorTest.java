 /** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.classifier.feature.extractor.outcome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.junit.Test;
import org.uutuc.factory.UimaContextFactory;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 */

public class DefaultOutcomeFeatureExtractorTest {

	@Test
	public void testInitialize() throws ResourceInitializationException {
		OutcomeFeatureExtractor ofe;

		UimaContext context = UimaContextFactory.createUimaContext();
		ResourceInitializationException rie = null;
		try {
			ofe = new DefaultOutcomeFeatureExtractor();
			ofe.initialize(context);
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNull(rie);

		context = UimaContextFactory.createUimaContext(DefaultOutcomeFeatureExtractor.PARAM_MOST_RECENT_OUTCOME,
				new Integer(3), DefaultOutcomeFeatureExtractor.PARAM_LEAST_RECENT_OUTCOME, new Integer(1));
		rie = null;
		try {
			ofe = new DefaultOutcomeFeatureExtractor();
			ofe.initialize(context);
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNotNull(rie);

		context = UimaContextFactory.createUimaContext(DefaultOutcomeFeatureExtractor.PARAM_MOST_RECENT_OUTCOME,
				new Integer(0));
		rie = null;
		try {
			ofe = new DefaultOutcomeFeatureExtractor();
			ofe.initialize(context);
		}
		catch (ResourceInitializationException e) {
			rie = e;
		}
		assertNotNull(rie);
	}

	@Test
	public void testExtractFeatures() throws ResourceInitializationException {
		UimaContext context = UimaContextFactory.createUimaContext();
		OutcomeFeatureExtractor ofe = new DefaultOutcomeFeatureExtractor();
		ofe.initialize(context);
		
		List<Feature> features = ofe.extractFeatures(Arrays.asList(new Object[] {"A", "B", "C", "D"}));
		assertEquals(5, features.size());
		Set<String> featureNames = new HashSet<String>(); 
		for(Feature feature : features) {
			featureNames.add(feature.getName()+"_"+feature.getValue());
		}
		assertTrue(featureNames.contains("PreviousOutcome_L1_D"));
		assertTrue(featureNames.contains("PreviousOutcome_L2_C"));
		assertTrue(featureNames.contains("PreviousOutcome_L3_B"));
		assertTrue(featureNames.contains("PreviousOutcomes_L1_2gram_L2R_D_C"));
		assertTrue(featureNames.contains("PreviousOutcomes_L1_3gram_L2R_D_C_B"));
		
		features = ofe.extractFeatures(Arrays.asList(new Object[] {"A", "B"}));
		assertEquals(3, features.size());
		featureNames.clear(); 
		for(Feature feature : features) {
			featureNames.add(feature.getName()+"_"+feature.getValue());
		}
		assertTrue(featureNames.contains("PreviousOutcome_L1_B"));
		assertTrue(featureNames.contains("PreviousOutcome_L2_A"));
		assertTrue(featureNames.contains("PreviousOutcomes_L1_2gram_L2R_B_A"));
		
		features = ofe.extractFeatures(Arrays.asList(new Object[0] ));
		assertEquals(0, features.size());

		
		context = UimaContextFactory.createUimaContext(DefaultOutcomeFeatureExtractor.PARAM_MOST_RECENT_OUTCOME, 2,
				DefaultOutcomeFeatureExtractor.PARAM_LEAST_RECENT_OUTCOME, 3,
				DefaultOutcomeFeatureExtractor.PARAM_USE_4GRAM, true,
				DefaultOutcomeFeatureExtractor.PARAM_USE_BIGRAM, false);
		ofe = new DefaultOutcomeFeatureExtractor();
		ofe.initialize(context);
		
		features = ofe.extractFeatures(Arrays.asList(new Object[] {"A", "B", "C", "D"}));
		assertEquals(4, features.size());
		featureNames.clear(); 
		for(Feature feature : features) {
			featureNames.add(feature.getName()+"_"+feature.getValue());
		}

		assertTrue(featureNames.contains("PreviousOutcome_L2_C"));
		assertTrue(featureNames.contains("PreviousOutcome_L3_B"));
		assertTrue(featureNames.contains("PreviousOutcomes_L1_3gram_L2R_D_C_B"));
		assertTrue(featureNames.contains("PreviousOutcomes_L1_4gram_L2R_D_C_B_A"));

		
		features = ofe.extractFeatures(Arrays.asList(new Object[] {1, 3, "CAT", 5.5f}));
		assertEquals(4, features.size());
		featureNames.clear(); 
		for(Feature feature : features) {
			featureNames.add(feature.getName()+"_"+feature.getValue());
		}

		assertTrue(featureNames.contains("PreviousOutcome_L2_CAT"));
		assertTrue(featureNames.contains("PreviousOutcome_L3_3"));
		assertTrue(featureNames.contains("PreviousOutcomes_L1_3gram_L2R_5.5_CAT_3"));
		assertTrue(featureNames.contains("PreviousOutcomes_L1_4gram_L2R_5.5_CAT_3_1"));
	}
	
}
