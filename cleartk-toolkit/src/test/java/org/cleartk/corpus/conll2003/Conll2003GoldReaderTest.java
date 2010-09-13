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
package org.cleartk.corpus.conll2003;

import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ToolkitTestBase;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.type.Chunk;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */
public class Conll2003GoldReaderTest extends ToolkitTestBase{
	
	@Test
	public void testFakeTrainDocs() throws UIMAException, IOException 
	{
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				Conll2003GoldReader.class, typeSystemDescription,
				Conll2003GoldReader.PARAM_DATA_FILE_NAME, "test/data/corpus/conll2003/eng.train",
				Conll2003GoldReader.PARAM_LOAD_NAMED_ENTITIES, true);
		
		Iterator<JCas> iterator = new JCasIterable(reader).iterator();
		
		JCas jCas = iterator.next(); 			
		
		FSIndex<Annotation> sentenceIndex = jCas.getAnnotationIndex(Sentence.type);
		
		Assert.assertEquals(1, sentenceIndex.size());
		Sentence firstSentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		Assert.assertEquals("ee rrrr ggg ccc tt bbbb BBBBBBB llll . ", firstSentence.getCoveredText());
		
		Assert.assertEquals(9, AnnotationRetrieval.getAnnotations(jCas, firstSentence, Token.class).size());

		Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		Assert.assertEquals("A", token.getPos());
		
		Chunk chunk = AnnotationRetrieval.get(jCas, Chunk.class, 0);
		Assert.assertEquals("ee", chunk.getCoveredText());
		Assert.assertEquals("A", chunk.getChunkType());
		
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 1);
		Assert.assertEquals("rrrr", chunk.getCoveredText());
		Assert.assertEquals("B", chunk.getChunkType());
			
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 2);
		Assert.assertEquals("ggg ccc", chunk.getCoveredText());
		Assert.assertEquals("N", chunk.getChunkType());
		
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 3);
		Assert.assertEquals("tt bbbb", chunk.getCoveredText());
		Assert.assertEquals("T", chunk.getChunkType());
		
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 4);
		Assert.assertEquals("BBBBBBB llll", chunk.getCoveredText());
		Assert.assertEquals("BB", chunk.getChunkType());
			
		NamedEntityMention nem = AnnotationRetrieval.get(jCas, NamedEntityMention.class, 0);
		NamedEntity ne = nem.getMentionedEntity();
		Assert.assertEquals("ee", nem.getCoveredText());
		Assert.assertEquals("NAM", nem.getMentionType());
		Assert.assertEquals("OOO", ne.getEntityType());
		
		nem = AnnotationRetrieval.get(jCas, NamedEntityMention.class, 1);
		ne = nem.getMentionedEntity();
		Assert.assertEquals("ggg", nem.getCoveredText());
		Assert.assertEquals("MM", ne.getEntityType());

		nem = AnnotationRetrieval.get(jCas, NamedEntityMention.class, 2);
		ne = nem.getMentionedEntity();
		Assert.assertEquals("BBBBBBB", nem.getCoveredText());
		Assert.assertEquals("MM", ne.getEntityType());

		
		jCas = iterator.next(); 			
		
		sentenceIndex = jCas.getAnnotationIndex(Sentence.type);
		

		Assert.assertEquals(1, sentenceIndex.size());
		firstSentence = AnnotationRetrieval.get(jCas, Sentence.class, 0);
		Assert.assertEquals("CCCCC ssss tttt rrrrr fff TTTTTT ttttt . ", firstSentence.getCoveredText());
		
		Assert.assertEquals(8, AnnotationRetrieval.getAnnotations(jCas, firstSentence, Token.class).size());

		token = AnnotationRetrieval.get(jCas, Token.class, 0);
		Assert.assertEquals("PPP", token.getPos());
		
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 0);
		Assert.assertEquals("CCCCC", chunk.getCoveredText());
		Assert.assertEquals("PP", chunk.getChunkType());
		
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 1);
		Assert.assertEquals("ssss", chunk.getCoveredText());
		Assert.assertEquals("VV", chunk.getChunkType());
			
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 2);
		Assert.assertEquals("tttt", chunk.getCoveredText());
		Assert.assertEquals("NN", chunk.getChunkType());
		
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 3);
		Assert.assertEquals("rrrrr", chunk.getCoveredText());
		Assert.assertEquals("AAAA", chunk.getChunkType());
		
		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 4);
		Assert.assertEquals("fff", chunk.getCoveredText());
		Assert.assertEquals("PP", chunk.getChunkType());

		chunk = AnnotationRetrieval.get(jCas, Chunk.class, 5);
		Assert.assertEquals("TTTTTT ttttt", chunk.getCoveredText());
		Assert.assertEquals("NN", chunk.getChunkType());

		nem = AnnotationRetrieval.get(jCas, NamedEntityMention.class, 0);
		ne = nem.getMentionedEntity();
		Assert.assertEquals("CCCCC", nem.getCoveredText());
		Assert.assertEquals("NAM", nem.getMentionType());
		Assert.assertEquals("LL", ne.getEntityType());
		
		nem = AnnotationRetrieval.get(jCas, NamedEntityMention.class, 1);
		ne = nem.getMentionedEntity();
		Assert.assertEquals("TTTTTT", nem.getCoveredText());
		Assert.assertEquals("LL", ne.getEntityType());

	}

}
