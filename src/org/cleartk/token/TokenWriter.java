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
package org.cleartk.token;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Steven Bethard
 */
public class TokenWriter extends JCasAnnotator_ImplBase {

	public static final String PARAM_OUTPUT_DIRECTORY = "OutputDirectory";
	
	private File outputDir;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.outputDir = new File((String)UIMAUtil.getRequiredConfigParameterValue(
				context, TokenWriter.PARAM_OUTPUT_DIRECTORY));
		if (!this.outputDir.exists()) {
			outputDir.mkdirs();
		}
	}
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
	    DocumentAnnotation document = AnnotationRetrieval.getDocument(jcas);
		String id = ViewURIUtil.getURI(jcas);
	    File outFile = new File(this.outputDir, id + ".txt");
	    
	    PrintWriter output;
		try {
			output = new PrintWriter(outFile);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	    
	    StringBuffer buffer = new StringBuffer();
	    for (Sentence sentence : AnnotationRetrieval.getAnnotations(jcas, document, Sentence.class)) {
	    	if (buffer.length() > 0) {
	    		buffer.append('\n');
	    	}
	    	for( Token token : AnnotationRetrieval.getAnnotations(jcas, sentence, Token.class)) {
	    		buffer.append(token.getCoveredText());
	    		buffer.append('\n');
	    	}
	    }
	    output.print(buffer);
	    output.flush();
	    output.close();
	}

}
