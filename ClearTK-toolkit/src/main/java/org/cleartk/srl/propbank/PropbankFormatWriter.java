/**
 * Copyright (c) 2007-2008, Regen ts of the University of Colorado 
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
 package org.cleartk.srl.propbank;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.type.Sentence;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.linewriter.AnnotationWriter;

public class PropbankFormatWriter implements AnnotationWriter<Predicate>{

	public void initialize(UimaContext context) throws ResourceInitializationException {}

	public String writeAnnotation(JCas jCas, Predicate predicate) throws AnalysisEngineProcessException {
		StringBuilder sb = new StringBuilder();
		
		String uri = ViewURIUtil.getURI(jCas);
		sb.append(uri+"\t");
		
		List<Sentence> sentences = AnnotationRetrieval.getAnnotations(jCas, Sentence.class);
		Sentence predicateSentence = AnnotationRetrieval.getContainingAnnotation(jCas, predicate, Sentence.class);
		int sentenceId = -1;
		for(int i=0; i<sentences.size(); i++) {
			if(sentences.get(i).equals(predicateSentence)) {
				sentenceId = i;
			}
		}
		
		String treeId = Integer.toString(sentenceId);
		sb.append(treeId+"\t");
		
		String frameSetId = predicate.getBaseForm() +"."+predicate.getFrameSet();
		sb.append(frameSetId + "\t");
		
		//this part seems like cheating.  what we should really do here is calculate the propTxt based on the other information
		//in the cas and then compare it to the explicitly stored version and throw an exception if there is a difference.  That way,
		//we know the data is consistent.  
		List<Argument> arguments = UIMAUtil.toList(predicate.getArguments(), Argument.class);
		for(Argument argument : arguments) {
			if(argument instanceof SemanticArgument)
				sb.append(((SemanticArgument) argument).getPropTxt()+"\t");
		}
		return sb.toString();
	}

}
