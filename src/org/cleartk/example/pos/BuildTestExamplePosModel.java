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

package org.cleartk.example.pos;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.metadata.SofaMapping;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.ViewNames;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.SequentialInstanceConsumer;
import org.cleartk.classifier.feature.extractor.outcome.DefaultOutcomeFeatureExtractor;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.viterbi.ViterbiDataWriter;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.TestsUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.SofaMappingFactory;
import org.uutuc.util.JCasIterable;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */

public class BuildTestExamplePosModel {

	public static void main(String[] args) throws Exception {
		
		TypeSystemDescription typeSystemDescription = TestsUtil.getTypeSystemDescription();
		
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(PennTreebankReader.class, typeSystemDescription, 
				PennTreebankReader.PARAM_CORPUS_DIRECTORY, "../ClearTK Data/data/treebank/wsj",
				PennTreebankReader.PARAM_SECTIONS, "02-03");
		
		SofaMapping[] sofaMappings = new SofaMapping[] {
				SofaMappingFactory.createSofaMapping(ViewNames.TREEBANK, TreebankGoldAnnotator.class, ViewNames.TREEBANK),
				SofaMappingFactory.createSofaMapping(ViewNames.TREEBANK_ANNOTATIONS, TreebankGoldAnnotator.class, ViewNames.TREEBANK_ANNOTATIONS),
				SofaMappingFactory.createSofaMapping(ViewNames.TREEBANK_ANNOTATIONS, SequentialDataWriterAnnotator.class, ViewNames.DEFAULT),
				SofaMappingFactory.createSofaMapping(ViewNames.TREEBANK_ANNOTATIONS, SnowballStemmer.class, ViewNames.DEFAULT)
			};

		List<Class<? extends AnalysisComponent>> analysisEngines= new ArrayList<Class<? extends AnalysisComponent>>();
		analysisEngines.add(TreebankGoldAnnotator.class);
		analysisEngines.add(SnowballStemmer.class);
		analysisEngines.add(SequentialDataWriterAnnotator.class);
		
		AnalysisEngine aggregateEngine = AnalysisEngineFactory.createAggregateAnalysisEngine(analysisEngines, typeSystemDescription, (TypePriorities) null, sofaMappings, 
				TreebankGoldAnnotator.PARAM_POST_TREES, false,
				SnowballStemmer.PARAM_STEMMER_NAME, "English",
				SequentialInstanceConsumer.PARAM_ANNOTATION_HANDLER, ExamplePOSAnnotationHandler.class.getName(),
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "example/model",
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, ViterbiDataWriterFactory.class.getName(),
				DefaultMaxentDataWriterFactory.PARAM_COMPRESS, true,
				ViterbiDataWriter.PARAM_OUTCOME_FEATURE_EXTRACTORS,  new String[] {DefaultOutcomeFeatureExtractor.class.getName()},
				ViterbiDataWriter.PARAM_DELEGATED_DATAWRITER_FACTORY_CLASS, DefaultMaxentDataWriterFactory.class.getName()
		);
		
		JCasIterable jCases = new JCasIterable(reader, aggregateEngine);
		
		for(@SuppressWarnings("unused") JCas jCas : jCases) { }
		
		aggregateEngine.collectionProcessComplete();
		org.cleartk.classifier.Train.main(new String[] {"example/model"});

	}
}
