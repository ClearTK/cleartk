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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.FrameworkTestBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.annotationpair.DistanceExtractor;
import org.cleartk.type.test.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * Unit tests for org.cleartk.readers.DirectoryCollectionReader.
 * 
 * @author Philip Ogren
 */

public class DistanceExtractorTest extends FrameworkTestBase {

	@Test
	public void test1() throws Exception {
		tokenBuilder.buildTokens(jCas, "A simple sentence to test the distance of tokens from each other.", 
				"A simple sentence to test the distance of tokens from each other .");
		
		DistanceExtractor extractor = new DistanceExtractor(null, Token.class);
		
		Token token1 = AnnotationRetrieval.get(jCas, Token.class, 0);
		Token token2 = AnnotationRetrieval.get(jCas, Token.class, 1);
		Token token3 = AnnotationRetrieval.get(jCas, Token.class, 2);
		Token token4 = AnnotationRetrieval.get(jCas, Token.class, 3);
		Token token5 = AnnotationRetrieval.get(jCas, Token.class, 4);
		
		List<Feature> features = extractor.extract(jCas, token1, token2);
		assertEquals(0, features.get(0).getValue());

		features = extractor.extract(jCas, token1, token3);
		assertEquals(1, features.get(0).getValue());
		features = extractor.extract(jCas, token3, token1);
		assertEquals(1, features.get(0).getValue());

		features = extractor.extract(jCas, token1, token5);
		assertEquals(3, features.get(0).getValue());
		features = extractor.extract(jCas, token5, token1);
		assertEquals(3, features.get(0).getValue());

		features = extractor.extract(jCas, token4, token5);
		assertEquals(0, features.get(0).getValue());
		features = extractor.extract(jCas, token5, token4);
		assertEquals(0, features.get(0).getValue());

		features = extractor.extract(jCas, token5, token5);
		assertEquals(0, features.get(0).getValue());
		
		features = extractor.extract(jCas, new Annotation(jCas, 0,3), token1);
		assertEquals(0, features.get(0).getValue());
		features = extractor.extract(jCas, new Annotation(jCas, 0,3), token2);
		assertEquals(0, features.get(0).getValue());
		features = extractor.extract(jCas, new Annotation(jCas, 0,3), token3);
		assertEquals(0, features.get(0).getValue());
		features = extractor.extract(jCas, new Annotation(jCas, 0,3), token4);
		assertEquals(1, features.get(0).getValue());
		features = extractor.extract(jCas, new Annotation(jCas, 0,3), token5);
		assertEquals(2, features.get(0).getValue());
		
		Annotation annotation = new Annotation(jCas, 64, 65);
		assertEquals(".", annotation.getCoveredText());
		features = extractor.extract(jCas, annotation, token1);
		assertEquals(11, features.get(0).getValue());

	}
}
