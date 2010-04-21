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
package org.cleartk.example.sentence;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.CleartkComponents;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.type.Sentence;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.linewriter.LineWriter;
import org.uimafit.util.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */
public class Docs2Sentences {

	public static void main(String[] args) throws UIMAException, IOException {
		String inputDirectoryName = args[0];
		String outputFileName = args[1];
		
		CollectionReader filesReader = CleartkComponents.createCollectionReader(FilesCollectionReader.class, FilesCollectionReader.PARAM_ROOT_FILE, inputDirectoryName);
		AnalysisEngine sentences = CleartkComponents.createPrimitive(OpenNLPSentenceSegmenter.class);
		AnalysisEngine lineWriter = CleartkComponents.createPrimitive(LineWriter.class, LineWriter.PARAM_OUTPUT_FILE_NAME, outputFileName, LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, Sentence.class.getName());

		SimplePipeline.runPipeline(filesReader, sentences, lineWriter);
	
		
	}
}
