/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.token.pos.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.ViewNames;
import org.cleartk.classifier.BuildJar;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.SequentialInstanceConsumer;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.viterbi.ViterbiDataWriter;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.token.pos.POSHandler;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.ReusableUIMAObjects;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.JCasIterable;
import org.uutuc.util.TearDownUtil;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */
public class DefaultPOSHandlerTest {

	private File outputDirectory = new File("test/data/token/poshandler");
	
	@Before
	public void setUp() {
		outputDirectory.mkdirs();
	}
	
	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(outputDirectory);
	}
	
	@Test
	public void testCraft() throws Exception {
		
		TypeSystemDescription defaultTypeSystemDescription = ReusableUIMAObjects.getTypeSystemDescription();
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class, defaultTypeSystemDescription, 
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, "test/data/docs/treebank",
				FilesCollectionReader.PARAM_SUFFIXES, new String[] {".tree"},
				FilesCollectionReader.PARAM_VIEW_NAME, ViewNames.TREEBANK);
		
		List<Class<? extends AnalysisComponent>> aggregatedClasses = new ArrayList<Class<? extends AnalysisComponent>>();
		aggregatedClasses.add(TreebankGoldAnnotator.class);
		aggregatedClasses.add(SequentialDataWriterAnnotator.class);

		AnalysisEngine aggregateEngine = AnalysisEngineFactory.createAggregate(aggregatedClasses, 
				defaultTypeSystemDescription, (TypePriorities)null, null,
				TreebankGoldAnnotator.PARAM_POST_TREES, false,
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, ViterbiDataWriterFactory.class.getName(),
				ViterbiDataWriter.PARAM_DELEGATED_DATAWRITER_FACTORY_CLASS, DefaultMaxentDataWriterFactory.class.getName(),
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER, DefaultPOSHandler.class.getName(),
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory.getPath(),
				POSHandler.PARAM_FEATURE_EXTRACTOR_CLASS_NAME, DefaultFeatureExtractor.class.getName());
		
		for(JCas jCas : new JCasIterable(reader, aggregateEngine)) {
			assert jCas != null;
		}
		
		aggregateEngine.collectionProcessComplete();
		
		
		HideOutput hider = new HideOutput();
		Train.main(outputDirectory.getPath(), "20", "5");
		hider.restoreOutput();
		
		AnalysisEngine tagger = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.token.pos.impl.DefaultPOSAnnotator", 
				SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR, new File(outputDirectory, BuildJar.MODEL_FILE_NAME).getPath());

		JCas jCas = ReusableUIMAObjects.getJCas();
		TokenFactory.createTokens(jCas, "What kitchen utensil is like a vampire ? Spatula", Token.class, Sentence.class );
		tagger.process(jCas);
		assertEquals("WP", AnnotationRetrieval.get(jCas, Token.class, 0).getPos());
		assertEquals("NN", AnnotationRetrieval.get(jCas, Token.class, 1).getPos());
		assertEquals("NN", AnnotationRetrieval.get(jCas, Token.class, 2).getPos());
		
	
	}
}
