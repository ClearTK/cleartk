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
package org.cleartk.token.tokenizer.chunk;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.chunker.ChunkLabeler_ImplBase;
import org.cleartk.chunker.Chunker;
import org.cleartk.chunker.DefaultChunkLabeler;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.chunk.type.Subtoken;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.junit.Test;
import org.uimafit.component.JCasAnnotatorAdapter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.AnnotationFactory;
import org.uimafit.util.JCasUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class ChunkTokenizerLabelerTest extends ToolkitTestBase{


	/**
	 * these tests represent what would typically happen when the InstanceConsumer is ClassifierAnnotator
	 * @throws UIMAException 
	 */
	@Test
	public void testClassifierAnnotator() throws UIMAException {
		  AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				  JCasAnnotatorAdapter.class,
				    typeSystemDescription,
				    ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				    ChunkerAnnotator.PARAM_LABELED_ANNOTATION_CLASS_NAME, "org.cleartk.token.chunk.type.Subtoken"
		  			);
		  
		  DefaultChunkLabeler defaultChunkLabeler = new DefaultChunkLabeler();
		  defaultChunkLabeler.initialize(engine.getUimaContext());
		  
		  
		  
		  jCas.reset();
		  jCas.setDocumentText("Technological progress is like an axe in the hands of a pathological criminal."); //Albert Einstein
		  Sentence sentence = new Sentence(jCas, 0, 78);
		  sentence.addToIndexes();
		  
		  Subtoken subtoken1 = AnnotationFactory.createAnnotation(jCas, 0, 13, Subtoken.class);  //Technological
		  Subtoken subtoken2 = AnnotationFactory.createAnnotation(jCas, 14, 22, Subtoken.class); //progress
		  Subtoken subtoken3 = AnnotationFactory.createAnnotation(jCas, 23, 25, Subtoken.class); //is
		  Subtoken subtoken4 = AnnotationFactory.createAnnotation(jCas, 26, 30, Subtoken.class); //like
		  Subtoken subtoken5 = AnnotationFactory.createAnnotation(jCas, 31, 33, Subtoken.class); //an
		  Subtoken subtoken6 = AnnotationFactory.createAnnotation(jCas, 34, 37, Subtoken.class); //axe
		  Subtoken subtoken7 = AnnotationFactory.createAnnotation(jCas, 38, 40, Subtoken.class); //in
		  Subtoken subtoken8 = AnnotationFactory.createAnnotation(jCas, 41, 44, Subtoken.class); //the
		  Subtoken subtoken9 = AnnotationFactory.createAnnotation(jCas, 45, 50, Subtoken.class); //hands
		  Subtoken subtoken10 = AnnotationFactory.createAnnotation(jCas, 51, 53, Subtoken.class); //of
		  Subtoken subtoken11 = AnnotationFactory.createAnnotation(jCas, 54, 55, Subtoken.class); //a
		  Subtoken subtoken12 = AnnotationFactory.createAnnotation(jCas, 56, 68, Subtoken.class); //pathological
		  Subtoken subtoken13 = AnnotationFactory.createAnnotation(jCas, 69, 77, Subtoken.class); //criminal
		  Subtoken subtoken14 = AnnotationFactory.createAnnotation(jCas, 77, 78, Subtoken.class); //.
		  defaultChunkLabeler.chunks2Labels(jCas, sentence);
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken1));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken2));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken3));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken4));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken5));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken6));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken7));
		  defaultChunkLabeler.setLabel(subtoken1, "B-Token"); //begin Technological
		  defaultChunkLabeler.setLabel(subtoken2, ChunkLabeler_ImplBase.OUTSIDE_LABEL);
		  defaultChunkLabeler.setLabel(subtoken3, "I-Token");//begin is
		  defaultChunkLabeler.setLabel(subtoken4, "B-Token");//begin like
		  defaultChunkLabeler.setLabel(subtoken5, "B-Token");//begin an axe in
		  defaultChunkLabeler.setLabel(subtoken6, "I-Token");
		  defaultChunkLabeler.setLabel(subtoken7, "I-Token");
		  defaultChunkLabeler.setLabel(subtoken8, ChunkLabeler_ImplBase.OUTSIDE_LABEL);
		  defaultChunkLabeler.setLabel(subtoken9, "I-Token");//begin hands of
		  defaultChunkLabeler.setLabel(subtoken10, "I-Token");
		  defaultChunkLabeler.setLabel(subtoken11, "B-Token");//begin a
		  defaultChunkLabeler.setLabel(subtoken12, "B-Token");//begin pathological
		  defaultChunkLabeler.setLabel(subtoken13, ChunkLabeler_ImplBase.OUTSIDE_LABEL);
		  defaultChunkLabeler.setLabel(subtoken14, "I-Token");
		  defaultChunkLabeler.labels2Chunks(jCas, sentence);
		  Token token = JCasUtil.selectByIndex(jCas, Token.class, 0);
		  assertEquals("Technological", token.getCoveredText());
		  token = JCasUtil.selectByIndex(jCas, Token.class, 1);
		  assertEquals("is", token.getCoveredText());
		  token = JCasUtil.selectByIndex(jCas, Token.class, 2);
		  assertEquals("like", token.getCoveredText());
		  token = JCasUtil.selectByIndex(jCas, Token.class, 3);
		  assertEquals("an axe in", token.getCoveredText());
		  token = JCasUtil.selectByIndex(jCas, Token.class, 4);
		  assertEquals("hands of", token.getCoveredText());
		  token = JCasUtil.selectByIndex(jCas, Token.class, 5);
		  assertEquals("a", token.getCoveredText());
		  token = JCasUtil.selectByIndex(jCas, Token.class, 6);
		  assertEquals("pathological", token.getCoveredText());
		  token = JCasUtil.selectByIndex(jCas, Token.class, 7);
		  assertEquals(".", token.getCoveredText());
		  
		  
	}

	/**
	 * these tests represent what would typically happen when the InstanceConsumer is a DataWriter
	 * @throws UIMAException 
	 */
	
	@Test
	public void testDataWriter() throws UIMAException {
		  AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				  JCasAnnotatorAdapter.class,
				    typeSystemDescription,
				    ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token",
				    ChunkerAnnotator.PARAM_LABELED_ANNOTATION_CLASS_NAME, "org.cleartk.token.chunk.type.Subtoken"
		  			);
		  
		  DefaultChunkLabeler defaultChunkLabeler = new DefaultChunkLabeler();
		  defaultChunkLabeler.initialize(engine.getUimaContext());
		  
		  
		  
		  jCas.reset();
		  jCas.setDocumentText("Technological progress is like an axe in the hands of a pathological criminal."); //Albert Einstein
		  Sentence sentence = new Sentence(jCas, 0, 78);
		  sentence.addToIndexes();

		  Subtoken subtoken1 = AnnotationFactory.createAnnotation(jCas, 0, 13, Subtoken.class);  //Technological
		  Subtoken subtoken2 = AnnotationFactory.createAnnotation(jCas, 14, 22, Subtoken.class); //progress
		  Subtoken subtoken3 = AnnotationFactory.createAnnotation(jCas, 23, 25, Subtoken.class); //is
		  Subtoken subtoken4 = AnnotationFactory.createAnnotation(jCas, 26, 30, Subtoken.class); //like
		  Subtoken subtoken5 = AnnotationFactory.createAnnotation(jCas, 31, 33, Subtoken.class); //an
		  Subtoken subtoken6 = AnnotationFactory.createAnnotation(jCas, 34, 37, Subtoken.class); //axe
		  Subtoken subtoken7 = AnnotationFactory.createAnnotation(jCas, 38, 40, Subtoken.class); //in
		  Subtoken subtoken8 = AnnotationFactory.createAnnotation(jCas, 41, 44, Subtoken.class); //the
		  Subtoken subtoken9 = AnnotationFactory.createAnnotation(jCas, 45, 50, Subtoken.class); //hands
		  Subtoken subtoken10 = AnnotationFactory.createAnnotation(jCas, 51, 53, Subtoken.class); //of
		  Subtoken subtoken11 = AnnotationFactory.createAnnotation(jCas, 54, 55, Subtoken.class); //a
		  Subtoken subtoken12 = AnnotationFactory.createAnnotation(jCas, 56, 68, Subtoken.class); //pathological
		  Subtoken subtoken13 = AnnotationFactory.createAnnotation(jCas, 69, 77, Subtoken.class); //criminal
		  Subtoken subtoken14 = AnnotationFactory.createAnnotation(jCas, 77, 78, Subtoken.class); //.
		  
		  AnnotationFactory.createAnnotation(jCas, 0, 13, Token.class); //Technological
		  AnnotationFactory.createAnnotation(jCas, 23, 25, Token.class); //is
		  AnnotationFactory.createAnnotation(jCas, 26, 30, Token.class); //like
		  AnnotationFactory.createAnnotation(jCas, 31, 40, Token.class); //an axe in
		  AnnotationFactory.createAnnotation(jCas, 45, 53, Token.class); //hands of
		  AnnotationFactory.createAnnotation(jCas, 54, 55, Token.class); //a
		  AnnotationFactory.createAnnotation(jCas, 56, 68, Token.class); //pathological
		  AnnotationFactory.createAnnotation(jCas, 77, 78, Token.class); //.
		  
		  defaultChunkLabeler.chunks2Labels(jCas, sentence);
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken1));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken2));
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken3));
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken4));
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken5));
		  assertEquals("I-Token", defaultChunkLabeler.getLabel(subtoken6));
		  assertEquals("I-Token", defaultChunkLabeler.getLabel(subtoken7));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken8));
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken9));
		  assertEquals("I-Token", defaultChunkLabeler.getLabel(subtoken10));
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken11));
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken12));
		  assertEquals(ChunkLabeler_ImplBase.OUTSIDE_LABEL, defaultChunkLabeler.getLabel(subtoken13));
		  assertEquals("B-Token", defaultChunkLabeler.getLabel(subtoken14));
		  
	}

	
	@Test
	public void testGetChunkLabel() throws UIMAException {
		  AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				  JCasAnnotatorAdapter.class,
				    typeSystemDescription,
				    ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME, "org.cleartk.ne.type.NamedEntityMention",
				    DefaultChunkLabeler.PARAM_CHUNK_LABEL_FEATURE_NAME, "mentionType",
		  			 ChunkerAnnotator.PARAM_LABELED_ANNOTATION_CLASS_NAME, "org.cleartk.type.Token"
		  			);
		  
		  DefaultChunkLabeler defaultChunkLabeler = new DefaultChunkLabeler();
		  defaultChunkLabeler.initialize(engine.getUimaContext());
		  
		  jCas.setDocumentText("Technological progress is like an axe in the hands of a pathological criminal."); //Albert Einstein

		  NamedEntityMention nem = AnnotationFactory.createAnnotation(jCas, 0, 22, NamedEntityMention.class);
		  nem.setMentionType("THEME");
		  
		  assertEquals("THEME", defaultChunkLabeler.getChunkLabel(jCas, nem));
		  
		  AnnotationFactory.createAnnotation(jCas, 56, 13, Token.class); //Technological
		  AnnotationFactory.createAnnotation(jCas, 23, 77, Token.class); //is
		  
		  Token token1 = AnnotationFactory.createAnnotation(jCas, 56, 68, Token.class); //pathological
		  Token token2 = AnnotationFactory.createAnnotation(jCas, 69, 77, Token.class); //criminal
		  
		  defaultChunkLabeler.createChunk(jCas, Arrays.asList(token1, token2), "blue");
		  
		  nem = JCasUtil.selectByIndex(jCas, NamedEntityMention.class, 1);
		  assertEquals("pathological criminal", nem.getCoveredText());
		  assertEquals("blue", nem.getMentionType());

	}
}
