package org.cleartk.example.pos;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.SequentialClassifierAnnotator;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.TestsUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.JCasIterable;

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

public class RunExamplePOSAnnotator {

	/**
	 * <br>
	 * Copyright (c) 2009, Regents of the University of Colorado <br>
	 * All rights reserved.
	 * @author Philip Ogren
	 * 
	 */

	public static void main(String[] args)  throws Exception {
		TypeSystemDescription typeSystemDescription = TestsUtil.getTypeSystemDescription();
		
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(FilesCollectionReader.class, typeSystemDescription, 
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, "example/data/2008_Sichuan_earthquake.txt");

		AnalysisEngine sentenceSegmenter = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.sentence.SentenceSegmenter");
		AnalysisEngine tokenizer = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.token.TokenAnnotator");
		AnalysisEngine stemmer = AnalysisEngineFactory.createAnalysisEngine(SnowballStemmer.class, typeSystemDescription,
				SnowballStemmer.PARAM_STEMMER_NAME, "English");
		AnalysisEngine posTagger = AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.pos.ExamplePOSAnnotator", 
				SequentialClassifierAnnotator.PARAM_CLASSIFIER_JAR, "example/model/model.jar");
		AnalysisEngine posWriter= AnalysisEngineFactory.createAnalysisEngine("org.cleartk.example.pos.ExamplePOSPlainTextWriter"); 
				
		JCasIterable jCases = new JCasIterable(reader, sentenceSegmenter, tokenizer, stemmer, posTagger, posWriter);
		
		for(@SuppressWarnings("unused") JCas jCas : jCases) { }
		
		posWriter.collectionProcessComplete();
	}
}
