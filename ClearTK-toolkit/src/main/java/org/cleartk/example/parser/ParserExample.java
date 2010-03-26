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

package org.cleartk.example.parser;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.CleartkComponents;
import org.cleartk.syntax.opennlp.OpenNLPTreebankParser;
import org.cleartk.token.opennlp.OpenNLPPOSTagger;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.XWriter;
import org.uutuc.util.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */
public class ParserExample {

	public static void main(String[] args) throws Exception {

		String filesDirectory = args[0];
		String outputDirectory = args[1];

		CollectionReader reader = CleartkComponents.createCollectionReader(FilesCollectionReader.class,
				FilesCollectionReader.PARAM_ROOT_FILE, filesDirectory);

		AnalysisEngineDescription sentenceAndTokensDescription = CleartkComponents.createSentencesAndTokens();
		AnalysisEngineDescription posTaggerDescription = OpenNLPPOSTagger.getDescription();
		AnalysisEngineDescription parserDescription = OpenNLPTreebankParser.getDescription();
		AnalysisEngineDescription xWriterDescription = CleartkComponents.createPrimitiveDescription(XWriter.class,
				XWriter.PARAM_OUTPUT_DIRECTORY_NAME, outputDirectory);

		SimplePipeline.runPipeline(reader, sentenceAndTokensDescription, posTaggerDescription, parserDescription, xWriterDescription);
	}
}
