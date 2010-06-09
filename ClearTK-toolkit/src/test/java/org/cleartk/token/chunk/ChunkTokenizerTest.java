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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.CleartkException;
import org.cleartk.chunk.ChunkLabeler;
import org.cleartk.chunk.ChunkLabeler_ImplBase;
import org.cleartk.chunk.ChunkerAnnotator;
import org.cleartk.chunk.ChunkerFeatureExtractor;
import org.cleartk.chunk.DefaultChunkLabeler;
import org.cleartk.classifier.CleartkSequentialAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.jar.JarSequentialDataWriterFactory;
import org.cleartk.type.Chunk;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.InstanceCollector;
import org.cleartk.util.ReusableUIMAObjects;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TokenFactory;
import org.uimafit.util.initialize.InitializableFactory;

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
	
	public static class TestFeatureExtractor implements ChunkerFeatureExtractor {

		private List<SimpleFeatureExtractor> simpleFeatureExtractors;
		private List<WindowExtractor> windowExtractors;

		public Instance<String> extractFeatures(JCas jCas, Annotation labeledAnnotation, Annotation sequence) throws CleartkException {
			Instance<String> instance = new Instance<String>();
			
			// extract all features that require only the token annotation
			for (SimpleFeatureExtractor extractor: this.simpleFeatureExtractors) {
				instance.addAll(extractor.extract(jCas, labeledAnnotation));
			}
			
			// extract all features that require the token and sentence annotations
			for (WindowExtractor extractor: this.windowExtractors) {
				instance.addAll(extractor.extract(jCas, labeledAnnotation, sequence));
			}

			return instance;
		}

		public void initialize(UimaContext context) throws ResourceInitializationException {
					this.simpleFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
			this.windowExtractors = new ArrayList<WindowExtractor>();
			
			SimpleFeatureExtractor wordExtractor = new SpannedTextExtractor();
			
			int fromLeft = CharacterNGramProliferator.LEFT_TO_RIGHT;
			this.simpleFeatureExtractors.add(new ProliferatingExtractor(
					wordExtractor,
					new LowerCaseProliferator(),
					new CharacterNGramProliferator(fromLeft, 0, 3, 3, true)));
			
			this.windowExtractors.add(new WindowExtractor(
					Token.class, wordExtractor, WindowFeature.ORIENTATION_LEFT, 0, 2));
			
		}

	}

	public static class TestChunkerAnnotator extends ChunkerAnnotator {
		public ChunkLabeler getChunkLabeler() {
			return chunkLabeler;
		}
		
	}
	
	public static class TestChunkerAnnotator2 extends ChunkerAnnotator {
		public ChunkLabeler getChunkLabeler() {
			return chunkLabeler;
		}
		@Override
		public List<String> classifySequence(List<Instance<String>> instances) {
			return Arrays.asList(new String[] {"B-1", "I-1", "I-1","O","O","B-nice","I-nice","B-nice", "B-twice", "O", "I-twice", "B-2", "I-2", "O", "O", "O", "O", "O", "O", "O"});
		}
	}
	@Test
	public void testChunkHandler() throws UIMAException, CleartkException {
		  AnalysisEngine engine = CleartkComponents.createPrimitive(
				  TestChunkerAnnotator.class,
				  ChunkerAnnotator.PARAM_LABELED_ANNOTATION_CLASS_NAME, Token.class.getName(),
				  ChunkerAnnotator.PARAM_SEQUENCE_CLASS_NAME, Sentence.class.getName(),
				  ChunkerAnnotator.PARAM_CHUNK_LABELER_CLASS_NAME, DefaultChunkLabeler.class.getName(),
				  ChunkerAnnotator.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME, TestFeatureExtractor.class.getName(),
				  ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME, Chunk.class.getName(),
				  DefaultChunkLabeler.PARAM_CHUNK_LABEL_FEATURE_NAME, "chunkType",
				  CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME, InstanceCollector.StringFactory.class.getName(),
				  JarSequentialDataWriterFactory.PARAM_OUTPUT_DIRECTORY, "test/data/scratch"
		  );
		  TestChunkerAnnotator copy = new TestChunkerAnnotator();
		  copy.initialize(engine.getUimaContext());
		  ChunkLabeler chunkLabeler = copy.getChunkLabeler();

		  
		  JCas jCas = ReusableUIMAObjects.getJCas();

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
		  
		  copy.process(jCas);
		
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
		 
		 engine = CleartkComponents.createPrimitive(
				 TestChunkerAnnotator2.class,
				  ChunkerAnnotator.PARAM_LABELED_ANNOTATION_CLASS_NAME, Token.class.getName(),
				  ChunkerAnnotator.PARAM_SEQUENCE_CLASS_NAME, Sentence.class.getName(),
				  ChunkerAnnotator.PARAM_CHUNK_LABELER_CLASS_NAME, DefaultChunkLabeler.class.getName(),
				  ChunkerAnnotator.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME, TestFeatureExtractor.class.getName(),
				  ChunkLabeler_ImplBase.PARAM_CHUNK_ANNOTATION_CLASS_NAME, Chunk.class.getName(),
				  DefaultChunkLabeler.PARAM_CHUNK_LABEL_FEATURE_NAME, "chunkType",
				  JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, "example/pos/model/model.jar"
		  );
		 InitializableFactory.initialize(chunkLabeler, engine.getUimaContext());
		engine.process(jCas);
		
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
	}


}
