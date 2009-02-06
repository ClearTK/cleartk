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
package org.cleartk.corpus.genia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.genia.GeniaPosGoldReader;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.TestsUtil;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 */

public class GeniaPosGoldReaderTests {

	@Test
	public void testReader() throws CASException, UIMAException, IOException {
		CollectionReader reader = TestsUtil.getCollectionReader(GeniaPosGoldReader.class, TestsUtil
				.getTypeSystem("desc/TypeSystem.xml"), GeniaPosGoldReader.PARAM_GENIA_CORPUS,
				"test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, true,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, true, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, true);

		JCas jCas = new TestsUtil.JCasIterable(reader).next();
		Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		assertEquals("IL-2", token.getCoveredText());
		assertEquals("NN", token.getPos());

		Sentence sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertEquals("IL-2 gene expression and NF-kappa B activation through CD28 requires reactive oxygen production by 5-lipoxygenase.", sentence.getCoveredText());

		token = AnnotationRetrieval.get(jCas, Token.class, 9);
		assertEquals("requires", token.getCoveredText());
		assertEquals("VBZ", token.getPos());

		reader = TestsUtil.getCollectionReader(GeniaPosGoldReader.class, TestsUtil
				.getTypeSystem("desc/TypeSystem.xml"), GeniaPosGoldReader.PARAM_GENIA_CORPUS,
				"test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, false,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, false, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, false);

		jCas = new TestsUtil.JCasIterable(reader).next();
		assertEquals(1, reader.getProgress()[0].getCompleted());
		
		token = AnnotationRetrieval.get(jCas, Token.class, 0);
		assertNull(token);

		sentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		assertNull(null);
		
		assertTrue(jCas.getDocumentText().startsWith("IL-2 gene expression and NF-kappa B activation through CD28 requires reactive oxygen production by 5-lipoxygenase."));

		IOException ioe = null;
		try {
			reader = TestsUtil.getCollectionReader(GeniaPosGoldReader.class, TestsUtil
				.getTypeSystem("desc/TypeSystem.xml"), GeniaPosGoldReader.PARAM_GENIA_CORPUS,
				"test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, false,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, false, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, false,
				GeniaPosGoldReader.PARAM_ARTICLE_IDS_LIST, "asdf");
		}catch (ResourceInitializationException rie) {
			ioe = (IOException) rie.getCause();
		}
		assertNotNull(ioe);

		JDOMException jde = null;
		try {
			reader = TestsUtil.getCollectionReader(GeniaPosGoldReader.class, TestsUtil
				.getTypeSystem("desc/TypeSystem.xml"), GeniaPosGoldReader.PARAM_GENIA_CORPUS,
				"test/data/corpus/genia/article_ids.txt");
		}catch (ResourceInitializationException rie) {
			jde = (JDOMException) rie.getCause();
		}
		assertNotNull(jde);

		reader = TestsUtil.getCollectionReader(GeniaPosGoldReader.class, TestsUtil
				.getTypeSystem("desc/TypeSystem.xml"), GeniaPosGoldReader.PARAM_GENIA_CORPUS,
				"test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, false,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, false, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, false,
				GeniaPosGoldReader.PARAM_ARTICLE_IDS_LIST, "test/data/corpus/genia/article_ids.txt");
		jCas = new TestsUtil.JCasIterable(reader).next();
		assertEquals(1, reader.getProgress()[0].getCompleted());
		assertFalse(reader.hasNext());
		
		CollectionException ce = null;
		try {
			reader.getNext(jCas.getCas());
			
		}catch (CollectionException collectionException) {
			ce = collectionException;
		}
		assertNotNull(ce);
		
		reader.close();
	}

	@Test
	public void testAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine;
		try {
			engine = TestsUtil.getAnalysisEngine("org.cleartk.corpus.genia.GeniaPosGoldReader");
			Assert.fail("expected exception with output directory not specified");
		}
		catch (ResourceInitializationException e) {
		}

		engine = TestsUtil.getAnalysisEngine("org.cleartk.corpus.genia.GeniaPosGoldReader",
				GeniaPosGoldReader.PARAM_GENIA_CORPUS, "test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml");

		String geniaCorpus = (String) engine.getConfigParameterValue(GeniaPosGoldReader.PARAM_GENIA_CORPUS);
		Assert.assertEquals("test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml", geniaCorpus);

		engine.collectionProcessComplete();

	}

}
