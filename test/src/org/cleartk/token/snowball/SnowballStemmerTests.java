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
package org.cleartk.token.snowball;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.type.Document;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.TestsUtil;
import org.junit.Assert;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class SnowballStemmerTests {

	@Test
	public void testBadStemmerName() throws UIMAException {
		try {
			TestsUtil.getAnalysisEngine(
					SnowballStemmer.class,
					TestsUtil.getTypeSystem(Document.class, Token.class, Sentence.class),
					SnowballStemmer.PARAM_STEMMER_NAME, "FooBar");
			Assert.fail("Expected exception for bad stemmer name");
		} catch (ResourceInitializationException e) {}
	}
	
	@Test
	public void testSimple() throws UIMAException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				SnowballStemmer.class,
				TestsUtil.getTypeSystem(Document.class, Token.class, Sentence.class),
				SnowballStemmer.PARAM_STEMMER_NAME, "English");
		JCas jCas = engine.newJCas();
		TestsUtil.createTokens(jCas,
				"The brown foxes jumped quickly over the lazy dog.",
				"The brown foxes jumped quickly over the lazy dog .",
				null, null);
		engine.process(jCas);
		
		List<String> expected = Arrays.asList(
				"the brown fox jump quick over the lazi dog .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token: AnnotationRetrieval.getAnnotations(jCas, Token.class)) {
			actual.add(token.getStem());
		}
		Assert.assertEquals(expected, actual);
		
	}

	@Test
	public void testUppercase() throws UIMAException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				SnowballStemmer.class,
				TestsUtil.getTypeSystem(Document.class, Token.class, Sentence.class),
				SnowballStemmer.PARAM_STEMMER_NAME, "English");
		JCas jCas = engine.newJCas();
		TestsUtil.createTokens(jCas,
				"The brown foxes JumPEd QUICKLy over the lazY dog.",
				"The brown foxes JumPEd QUICKLy over the lazY dog .",
				null, null);
		engine.process(jCas);
		
		List<String> expected = Arrays.asList(
				"the brown fox jump quick over the lazi dog .".split(" "));
		List<String> actual = new ArrayList<String>();
		for (Token token: AnnotationRetrieval.getAnnotations(jCas, Token.class)) {
			actual.add(token.getStem());
		}
		Assert.assertEquals(expected, actual);
		
	}

	@Test
	public void testDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				"desc/token/snowball/SnowballStemmer.xml");
		
		Object stemmerName = engine.getConfigParameterValue(
				SnowballStemmer.PARAM_STEMMER_NAME);
		Assert.assertEquals("English", stemmerName);
		
		engine.collectionProcessComplete();
	}
}
