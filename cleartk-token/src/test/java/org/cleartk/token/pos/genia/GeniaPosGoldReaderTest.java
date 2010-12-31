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
package org.cleartk.token.pos.genia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.TokenTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.jdom.JDOMException;
import org.junit.Test;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 */

public class GeniaPosGoldReaderTest extends TokenTestBase{

	@Test
	public void testReader() throws CASException, UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(GeniaPosGoldReader.class, typeSystemDescription, GeniaPosGoldReader.PARAM_GENIA_CORPUS_FILE,
				"src/test/resources/token/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, true,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, true, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, true);

		JCas jcas = new JCasIterable(reader).next();
		Token token = JCasUtil.selectByIndex(jcas, Token.class, 0);
		assertEquals("IL-2", token.getCoveredText());
		assertEquals("NN", token.getPos());

		Sentence sentence = JCasUtil.selectByIndex(jcas, Sentence.class, 0);
		assertEquals("IL-2 gene expression and NF-kappa B activation through CD28 requires reactive oxygen production by 5-lipoxygenase.", sentence.getCoveredText());

		token = JCasUtil.selectByIndex(jcas, Token.class, 9);
		assertEquals("requires", token.getCoveredText());
		assertEquals("VBZ", token.getPos());

		reader = CollectionReaderFactory.createCollectionReader(GeniaPosGoldReader.class, typeSystemDescription, GeniaPosGoldReader.PARAM_GENIA_CORPUS_FILE,
				"src/test/resources/token/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, false,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, false, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, false);

		jcas = new JCasIterable(reader).next();
		assertEquals(1, reader.getProgress()[0].getCompleted());
		
		token = JCasUtil.selectByIndex(jcas, Token.class, 0);
		assertNull(token);

		sentence = JCasUtil.selectByIndex(jcas, Sentence.class, 0);
		assertNull(null);
		
		assertTrue(jcas.getDocumentText().startsWith("IL-2 gene expression and NF-kappa B activation through CD28 requires reactive oxygen production by 5-lipoxygenase."));

		IOException ioe = null;
		try {
			reader = CollectionReaderFactory.createCollectionReader(GeniaPosGoldReader.class, typeSystemDescription, GeniaPosGoldReader.PARAM_GENIA_CORPUS_FILE,
				"src/test/resources/token/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, false,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, false, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, false,
				GeniaPosGoldReader.PARAM_ARTICLE_IDS_LIST_FILE, "asdf");
		}catch (ResourceInitializationException rie) {
			ioe = (IOException) rie.getCause();
		}
		assertNotNull(ioe);

		JDOMException jde = null;
		try {
			reader = CollectionReaderFactory.createCollectionReader(GeniaPosGoldReader.class, typeSystemDescription, GeniaPosGoldReader.PARAM_GENIA_CORPUS_FILE,
				"src/test/resources/token/genia/article_ids.txt");
		}catch (ResourceInitializationException rie) {
			jde = (JDOMException) rie.getCause();
		}
		assertNotNull(jde);

		reader = CollectionReaderFactory.createCollectionReader(GeniaPosGoldReader.class, typeSystemDescription, GeniaPosGoldReader.PARAM_GENIA_CORPUS_FILE,
				"src/test/resources/token/genia/GENIAcorpus3.02.articleA.pos.xml", GeniaPosGoldReader.PARAM_LOAD_TOKENS, false,
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, false, GeniaPosGoldReader.PARAM_LOAD_POS_TAGS, false,
				GeniaPosGoldReader.PARAM_ARTICLE_IDS_LIST_FILE, "src/test/resources/token/genia/article_ids.txt");
		jcas = new JCasIterable(reader).next();
		assertEquals(1, reader.getProgress()[0].getCompleted());
		assertFalse(reader.hasNext());
		
		CollectionException ce = null;
		try {
			reader.getNext(jcas.getCas());
			
		}catch (CollectionException collectionException) {
			ce = collectionException;
		}
		assertNotNull(ce);
		
		reader.close();
	}

}
