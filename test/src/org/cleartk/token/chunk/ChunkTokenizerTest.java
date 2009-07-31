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
package org.cleartk.token.chunk;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.chunk.ChunkLabeler;
import org.cleartk.chunk.ChunkLabeler_ImplBase;
import org.cleartk.chunk.ChunkerHandler;
import org.cleartk.chunk.DefaultChunkLabeler;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.SequentialInstanceConsumer;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.type.Chunk;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.TestsUtil;
import org.cleartk.util.UIMAUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.JCasAnnotatorAdapter;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class ChunkTokenizerTest {
	
	private final File outputDir = new File("test/data/token/chunk");

	@After
	public void tearDown() throws Exception {
		for (File file: this.outputDir.listFiles()) {
			if (!file.getName().equals("model.jar") &&
				!file.getName().equals("GeniaChunkTokenizerDataWriterCPE.xml")) {
				file.delete();
			}
		}
	}
	
	public static class TestChunkerHandler extends ChunkerHandler {
		public ChunkLabeler getChunkLabeler() {
			return chunkLabeler;
		}
		
	}
	
	@Test
	public void testChunkHandler() throws UIMAException, CleartkException {
		  AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				    JCasAnnotatorAdapter.class,
				    TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
		  			ChunkerHandler.PARAM_LABELED_ANNOTATION_CLASS, "org.cleartk.type.Token",
		  			ChunkerHandler.PARAM_SEQUENCE_CLASS, "org.cleartk.type.Sentence",
		  			ChunkerHandler.PARAM_CHUNK_LABELER_CLASS, "org.cleartk.chunk.DefaultChunkLabeler",
		  			ChunkerHandler.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS, TestChunkFeatureExtractor.class.getName(),
		  			ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS, "org.cleartk.type.Chunk",
		  			DefaultChunkLabeler.PARAM_CHUNK_LABEL_FEATURE, "chunkType"
		  );

		  TestChunkerHandler chunkerHandler = new TestChunkerHandler();
		  chunkerHandler.initialize(engine.getUimaContext());
		  
		  ChunkLabeler chunkLabeler = chunkerHandler.getChunkLabeler();

		  
		  JCas jCas = TestsUtil.getJCas();

		  String text = "What if we built a rocket ship made of cheese?" +
			  "We could fly it to the moon for repairs";
		  TokenFactory.createTokens(jCas, text, Token.class, Sentence.class,
					"What if we built a rocket ship made of cheese ? We could fly it to the moon for repairs",
					"A B C D E F G H I J K L M N O P Q R S T U", null, "org.cleartk.type.Token:pos", null);
		  List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, Token.class);
		  for (int i = 0; i < tokens.size(); i++) {
			Token token1 = tokens.get(i);
			Token token2 = tokens.get(++i);
			Chunk chunk = new Chunk(jCas, token1.getBegin(), token2.getEnd());
			chunk.setChunkType("chunk"+i);
			chunk.addToIndexes();
		  }
		  
		chunkerHandler.process(jCas, new TestChunkHandlerInstanceConsumer(true));
		
		Token token = tokens.get(0);
		assertEquals("B-chunk1", chunkLabeler.getLabel(token));
		token = tokens.get(1);
		assertEquals("I-chunk1", chunkLabeler.getLabel(token));
		token = tokens.get(2);
		assertEquals("B-chunk3", chunkLabeler.getLabel(token));
		token = tokens.get(3);
		assertEquals("I-chunk3", chunkLabeler.getLabel(token));
		token = tokens.get(4);
		assertEquals("B-chunk5", chunkLabeler.getLabel(token));
		token = tokens.get(5);
		assertEquals("I-chunk5", chunkLabeler.getLabel(token));
		
		 List<Chunk> chunks = AnnotationRetrieval.getAnnotations(jCas, Chunk.class);
		 assertEquals(10, chunks.size());
		 for(Chunk chunk : chunks) {
			 chunk.removeFromIndexes();
		 }
		 chunks = AnnotationRetrieval.getAnnotations(jCas, Chunk.class);
		 assertEquals(0, chunks.size());
		 
		 UIMAUtil.initialize(chunkLabeler, engine.getUimaContext());
		chunkerHandler.process(jCas, new TestChunkHandlerInstanceConsumer(false));
		
		chunks = AnnotationRetrieval.getAnnotations(jCas, Chunk.class);
		assertEquals(6, chunks.size());

		Chunk chunk = chunks.get(0);
		assertEquals("What if we", chunk.getCoveredText());
		assertEquals("1", chunk.getChunkType());
		
		chunk = chunks.get(1);
		assertEquals("rocket ship", chunk.getCoveredText());
		assertEquals("nice", chunk.getChunkType());
		
		chunk = chunks.get(2);
		assertEquals("made", chunk.getCoveredText());
		assertEquals("nice", chunk.getChunkType());
		
		chunk = chunks.get(3);
		assertEquals("of", chunk.getCoveredText());
		assertEquals("twice", chunk.getChunkType());
		
		chunk = chunks.get(4);
		assertEquals("?", chunk.getCoveredText());
		assertEquals("twice", chunk.getChunkType());
		
		chunk = chunks.get(5);
		assertEquals("We could", chunk.getCoveredText());
		assertEquals("2", chunk.getChunkType());
		
	}
	
	@Test
	public void testChunkTokenizerDescriptor() throws UIMAException, IOException {
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.token.chunk.ChunkTokenizer");
			Assert.fail("expected exception with missing classifier jar");
		} catch (ResourceInitializationException e) {}
			
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.token.chunk.ChunkTokenizer",
				SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR, "test/data/token/chunk/model.jar");
		Object handler = engine.getConfigParameterValue(
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(ChunkerHandler.class.getName(), handler);
		
		engine.collectionProcessComplete();
	}


	@Test
	public void testChunkTokenizerDataWriterDescriptor() throws UIMAException, IOException {
		String outputPath = this.outputDir.getPath();

		try {
			AnalysisEngineFactory.createAnalysisEngine(
					"org.cleartk.token.chunk.ChunkTokenizerDataWriter",
					SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletCRFDataWriterFactory.class.getName());
			Assert.fail("expected exception with missing output directory");
		} catch (ResourceInitializationException e) {}
			
		try {
			AnalysisEngineFactory.createAnalysisEngine(
					"org.cleartk.token.chunk.ChunkTokenizerDataWriter",
					SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputPath);
			Assert.fail("expected exception with missing data writer");
		} catch (ResourceInitializationException e) {}
			
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.token.chunk.ChunkTokenizerDataWriter",
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputPath,
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletCRFDataWriterFactory.class.getName());
		
		Object handler = engine.getConfigParameterValue(
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER);
		Assert.assertEquals(ChunkerHandler.class.getName(), handler);
		
		Object dataWriter = engine.getConfigParameterValue(
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS);
		Assert.assertEquals(DefaultMalletCRFDataWriterFactory.class.getName(), dataWriter);
		
		Object outputDir = engine.getConfigParameterValue(
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY);
		Assert.assertEquals(outputPath, outputDir);
		
		engine.collectionProcessComplete();
	}
}
