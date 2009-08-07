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

package org.cleartk.token.chunk;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.SequentialDataWriterAnnotator;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.corpus.genia.GeniaPosGoldReader;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.chunk.type.Subtoken;
import org.cleartk.token.util.Subtokenizer;
import org.cleartk.util.JCasUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.JCasIterable;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */

public class BuildTestTokenChunkModel {

	public static void main(String[] args) throws Exception {
		
		TypeSystemDescription typeSystemDescription = JCasUtil.getTypeSystemDescription();
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(GeniaPosGoldReader.class, typeSystemDescription, 
				GeniaPosGoldReader.PARAM_GENIA_CORPUS, "test/data/corpus/genia/GENIAcorpus3.02.articleA.pos.xml",
				GeniaPosGoldReader.PARAM_LOAD_SENTENCES, true);
		
		AnalysisEngine subtokenizer = AnalysisEngineFactory.createPrimitive(TokenAnnotator.class, typeSystemDescription, 
				TokenAnnotator.PARAM_TOKEN_TYPE, Subtoken.class.getName(),
				TokenAnnotator.PARAM_TOKENIZER, Subtokenizer.class.getName());
		 
		AnalysisEngine chunkTokenizerDataWriter = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.token.chunk.ChunkTokenizerDataWriter",
				SequentialDataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "test/data/token/chunk",
				SequentialDataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMalletCRFDataWriterFactory.class.getName());

		JCasIterable jCases = new JCasIterable(reader, subtokenizer, chunkTokenizerDataWriter);
		
		for(JCas jCas : jCases) {
			assert jCas != null;
		}
		
		chunkTokenizerDataWriter.collectionProcessComplete();
		org.cleartk.classifier.Train.main("test/data/token/chunk");

	}
}
