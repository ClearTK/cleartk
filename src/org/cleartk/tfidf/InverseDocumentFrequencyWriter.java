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
package org.cleartk.tfidf;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;

import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Steven Bethard
 *
 */
public class InverseDocumentFrequencyWriter extends JCasAnnotator_ImplBase {
	
	public static final String PARAM_OUTPUT_FILE = "org.cleartk.tfidf.InverseDocumentFrequencyWriter.PARAM_OUTPUT_FILE";

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		this.documentFrequencies = new HashMap<String, Integer>();
		this.documentCount = 0.0;
		this.outputFilePath = (String)UIMAUtil.getRequiredConfigParameterValue(
				context, InverseDocumentFrequencyWriter.PARAM_OUTPUT_FILE);
	}
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// iterate over each token in the document
		Set<String> stemSet = new HashSet<String>();
		DocumentAnnotation doc = AnnotationRetrieval.getDocument(jCas);
		for (Token token: AnnotationRetrieval.getAnnotations(jCas, doc, Token.class)) {
			String stem = token.getStem();
			if (stem == null) {
				stem = token.getCoveredText();
			}
			stemSet.add(stem);
		}
		
		// increment the document count for each stem found
		for (String stem: stemSet) {
			int count;
			if (documentFrequencies.containsKey(stem)) {
				count = documentFrequencies.get(stem) + 1;
			} else {
				count = 1;
			}
			documentFrequencies.put(stem, count);
		}
		
		// increment the total document count
		this.documentCount++;
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		
		// convert term-document counts to inverse document frequencies
		Map<String, Double> inverseDocumentFrequencies = new HashMap<String, Double>();
		for (String term: this.documentFrequencies.keySet()) {
			int documentFrequency = this.documentFrequencies.get(term);
			double inverseDocumentFrequency = this.documentCount / documentFrequency;
			inverseDocumentFrequencies.put(term, Math.log(inverseDocumentFrequency));
		}
		inverseDocumentFrequencies.put(null, Math.log(this.documentCount));

		// save the inverse document frequency map to the file
		try {
			ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(this.outputFilePath)));
			try {
				output.writeObject(inverseDocumentFrequencies);
			} finally {
				output.close();
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private Map<String, Integer> documentFrequencies;
	private double documentCount;
	private String outputFilePath;

}
