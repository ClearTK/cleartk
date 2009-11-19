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
package org.cleartk.classifier.feature.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.simple.NGramExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.type.test.Sentence;
import org.cleartk.type.test.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.JCasFactory;
import org.uutuc.factory.TokenFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */

public class NGramExtractorTest {

	@Test
	public void test() throws UIMAException, CleartkException {
		JCas jCas = JCasFactory.createJCas("org.cleartk.TestTypeSystem");
		TokenFactory.createTokens(jCas,
				"She sells seashells by the sea shore", Token.class, Sentence.class, 
				null, 
				"PRP VBZ NNS IN DT NN NN",
				null, "org.cleartk.type.test.Token:pos", null);
		DocumentAnnotation document = AnnotationRetrieval.getDocument(jCas);
		
		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		TypePathExtractor posExtractor = new TypePathExtractor(Token.class, "pos");
		NGramExtractor extractor;
		List<Feature> features;
		
		extractor = new NGramExtractor(2, Token.class, textExtractor);
		Assert.assertEquals("|", extractor.getValueSeparator());
		features = extractor.extract(jCas, document);
		Assert.assertEquals(6, features.size());
		this.checkFeatures(
				features, "Ngram(Token,null,null)",
				"She|sells", "sells|seashells", "seashells|by",
				"by|the", "the|sea", "sea|shore");
		
		extractor = new NGramExtractor(3, Token.class, posExtractor);
		extractor.setValueSeparator("@");
		Assert.assertEquals("@", extractor.getValueSeparator());
		features = extractor.extract(jCas, document);
		Assert.assertEquals(5, features.size());
		this.checkFeatures(
				features, "Ngram(Token,TypePath(Pos),TypePath(Pos),TypePath(Pos))",
				"PRP@VBZ@NNS", "VBZ@NNS@IN", "NNS@IN@DT", "IN@DT@NN", "DT@NN@NN");
		
	}
	
	private void checkFeatures(
			List<Feature> features,
			String expectedName,
			Object ... expectedValues) {
		List<Object> actualValues = new ArrayList<Object>();
		for (Feature feature: features) {
			Assert.assertEquals(expectedName, feature.getName());
			actualValues.add(feature.getValue());
		}
		Assert.assertEquals(Arrays.asList(expectedValues), actualValues);
	}
}
