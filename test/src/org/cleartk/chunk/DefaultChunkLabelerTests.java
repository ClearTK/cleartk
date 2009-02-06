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
package org.cleartk.chunk;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.chunk.ChunkLabeler_ImplBase;
import org.cleartk.chunk.DefaultChunkLabeler;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.chunk.type.Subtoken;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.EmptyAnnotator;
import org.cleartk.util.TestsUtil;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class DefaultChunkLabelerTests {


	/**
	 * these tests represent what would typically happen when the InstanceConsumer is ClassifierAnnotator
	 */
	@Test
	public void testClassifierAnnotator() throws ResourceInitializationException, AnalysisEngineProcessException {
		  AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				    EmptyAnnotator.class,
				    TestsUtil.getTypeSystem("org.cleartk.TypeSystem"),
		  			"ChunkAnnotationClass", "org.cleartk.type.Token",
		  			"LabeledAnnotationClass", "org.cleartk.token.chunk.type.Subtoken"
		  			);
		  
		  DefaultChunkLabeler defaultChunkLabeler = new DefaultChunkLabeler();
		  defaultChunkLabeler.initialize(engine.getUimaContext());
		  
		  JCas jCas = engine.newJCas();
		  
		  
		  jCas = engine.newJCas();
		  jCas.setDocumentText("Technological progress is like an axe in the hands of a pathological criminal."); //Albert Einstein
		  Sentence sentence = new Sentence(jCas, 0, 78);
		  sentence.addToIndexes();
		  
		  Subtoken subtoken1 = TestsUtil.createAnnotation(jCas, 0, 13, Subtoken.class);  //Technological
		  Subtoken subtoken2 = TestsUtil.createAnnotation(jCas, 14, 22, Subtoken.class); //progress
		  Subtoken subtoken3 = TestsUtil.createAnnotation(jCas, 23, 25, Subtoken.class); //is
		  Subtoken subtoken4 = TestsUtil.createAnnotation(jCas, 26, 30, Subtoken.class); //like
		  Subtoken subtoken5 = TestsUtil.createAnnotation(jCas, 31, 33, Subtoken.class); //an
		  Subtoken subtoken6 = TestsUtil.createAnnotation(jCas, 34, 37, Subtoken.class); //axe
		  Subtoken subtoken7 = TestsUtil.createAnnotation(jCas, 38, 40, Subtoken.class); //in
		  Subtoken subtoken8 = TestsUtil.createAnnotation(jCas, 41, 44, Subtoken.class); //the
		  Subtoken subtoken9 = TestsUtil.createAnnotation(jCas, 45, 50, Subtoken.class); //hands
		  Subtoken subtoken10 = TestsUtil.createAnnotation(jCas, 51, 53, Subtoken.class); //of
		  Subtoken subtoken11 = TestsUtil.createAnnotation(jCas, 54, 55, Subtoken.class); //a
		  Subtoken subtoken12 = TestsUtil.createAnnotation(jCas, 56, 68, Subtoken.class); //pathological
		  Subtoken subtoken13 = TestsUtil.createAnnotation(jCas, 69, 77, Subtoken.class); //criminal
		  Subtoken subtoken14 = TestsUtil.createAnnotation(jCas, 77, 78, Subtoken.class); //.
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
		  Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		  assertEquals("Technological", token.getCoveredText());
		  token = AnnotationRetrieval.get(jCas, Token.class, 1);
		  assertEquals("is", token.getCoveredText());
		  token = AnnotationRetrieval.get(jCas, Token.class, 2);
		  assertEquals("like", token.getCoveredText());
		  token = AnnotationRetrieval.get(jCas, Token.class, 3);
		  assertEquals("an axe in", token.getCoveredText());
		  token = AnnotationRetrieval.get(jCas, Token.class, 4);
		  assertEquals("hands of", token.getCoveredText());
		  token = AnnotationRetrieval.get(jCas, Token.class, 5);
		  assertEquals("a", token.getCoveredText());
		  token = AnnotationRetrieval.get(jCas, Token.class, 6);
		  assertEquals("pathological", token.getCoveredText());
		  token = AnnotationRetrieval.get(jCas, Token.class, 7);
		  assertEquals(".", token.getCoveredText());
		  
		  
	}

	/**
	 * these tests represent what would typically happen when the InstanceConsumer is a DataWriter
	 */
	
	@Test
	public void testDataWriter() throws ResourceInitializationException, AnalysisEngineProcessException {
		  AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				    EmptyAnnotator.class,
				    TestsUtil.getTypeSystem("org.cleartk.TypeSystem"),
		  			"ChunkAnnotationClass", "org.cleartk.type.Token",
		  			"LabeledAnnotationClass", "org.cleartk.token.chunk.type.Subtoken"
		  			);
		  
		  DefaultChunkLabeler defaultChunkLabeler = new DefaultChunkLabeler();
		  defaultChunkLabeler.initialize(engine.getUimaContext());
		  
		  JCas jCas = engine.newJCas();
		  
		  
		  jCas = engine.newJCas();
		  jCas.setDocumentText("Technological progress is like an axe in the hands of a pathological criminal."); //Albert Einstein
		  Sentence sentence = new Sentence(jCas, 0, 78);
		  sentence.addToIndexes();

		  Subtoken subtoken1 = TestsUtil.createAnnotation(jCas, 0, 13, Subtoken.class);  //Technological
		  Subtoken subtoken2 = TestsUtil.createAnnotation(jCas, 14, 22, Subtoken.class); //progress
		  Subtoken subtoken3 = TestsUtil.createAnnotation(jCas, 23, 25, Subtoken.class); //is
		  Subtoken subtoken4 = TestsUtil.createAnnotation(jCas, 26, 30, Subtoken.class); //like
		  Subtoken subtoken5 = TestsUtil.createAnnotation(jCas, 31, 33, Subtoken.class); //an
		  Subtoken subtoken6 = TestsUtil.createAnnotation(jCas, 34, 37, Subtoken.class); //axe
		  Subtoken subtoken7 = TestsUtil.createAnnotation(jCas, 38, 40, Subtoken.class); //in
		  Subtoken subtoken8 = TestsUtil.createAnnotation(jCas, 41, 44, Subtoken.class); //the
		  Subtoken subtoken9 = TestsUtil.createAnnotation(jCas, 45, 50, Subtoken.class); //hands
		  Subtoken subtoken10 = TestsUtil.createAnnotation(jCas, 51, 53, Subtoken.class); //of
		  Subtoken subtoken11 = TestsUtil.createAnnotation(jCas, 54, 55, Subtoken.class); //a
		  Subtoken subtoken12 = TestsUtil.createAnnotation(jCas, 56, 68, Subtoken.class); //pathological
		  Subtoken subtoken13 = TestsUtil.createAnnotation(jCas, 69, 77, Subtoken.class); //criminal
		  Subtoken subtoken14 = TestsUtil.createAnnotation(jCas, 77, 78, Subtoken.class); //.
		  
		  TestsUtil.createAnnotation(jCas, 0, 13, Token.class); //Technological
		  TestsUtil.createAnnotation(jCas, 23, 25, Token.class); //is
		  TestsUtil.createAnnotation(jCas, 26, 30, Token.class); //like
		  TestsUtil.createAnnotation(jCas, 31, 40, Token.class); //an axe in
		  TestsUtil.createAnnotation(jCas, 45, 53, Token.class); //hands of
		  TestsUtil.createAnnotation(jCas, 54, 55, Token.class); //a
		  TestsUtil.createAnnotation(jCas, 56, 68, Token.class); //pathological
		  TestsUtil.createAnnotation(jCas, 77, 78, Token.class); //.
		  
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
	public void testGetChunkLabel() throws ResourceInitializationException, AnalysisEngineProcessException {
		  AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				    EmptyAnnotator.class,
				    TestsUtil.getTypeSystem("org.cleartk.TypeSystem"),
		  			"ChunkAnnotationClass", "org.cleartk.ne.type.NamedEntityMention",
		  			"ChunkLabelFeature", "mentionType",
		  			"LabeledAnnotationClass", "org.cleartk.type.Token"
		  			);
		  
		  DefaultChunkLabeler defaultChunkLabeler = new DefaultChunkLabeler();
		  defaultChunkLabeler.initialize(engine.getUimaContext());
		  
		  JCas jCas = engine.newJCas();
		  
		  
		  jCas = engine.newJCas();
		  jCas.setDocumentText("Technological progress is like an axe in the hands of a pathological criminal."); //Albert Einstein

		  NamedEntityMention nem = TestsUtil.createAnnotation(jCas, 0, 22, NamedEntityMention.class);
		  nem.setMentionType("THEME");
		  
		  assertEquals("THEME", defaultChunkLabeler.getChunkLabel(jCas, nem));
		  
		  TestsUtil.createAnnotation(jCas, 56, 13, Token.class); //Technological
		  TestsUtil.createAnnotation(jCas, 23, 77, Token.class); //is
		  
		  Token token1 = TestsUtil.createAnnotation(jCas, 56, 68, Token.class); //pathological
		  Token token2 = TestsUtil.createAnnotation(jCas, 69, 77, Token.class); //criminal
		  
		  defaultChunkLabeler.createChunk(jCas, Arrays.asList(token1, token2), "blue");
		  
		  nem = AnnotationRetrieval.get(jCas, NamedEntityMention.class, 1);
		  assertEquals("pathological criminal", nem.getCoveredText());
		  assertEquals("blue", nem.getMentionType());

	}
}
