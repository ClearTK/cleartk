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
package org.cleartk.example.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.cleartk.util.ViewURIUtil;
import org.uutuc.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class ExamplePOSPlainTextWriter extends JCasAnnotator_ImplBase {

	public static final String DEFAULT_OUTPUT_DIRECTORY = "example/data";
	/**
	 * 
	 * "org.cleartk.example.pos.ExamplePOSPlainTextWriter.PARAM_OUTPUT_DIRECTORY"
	 * is a single, required, string parameter that provides the directory where
	 * the token/pos text files will be written.
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "org.cleartk.example.pos.ExamplePOSPlainTextWriter.PARAM_OUTPUT_DIRECTORY";

	protected File outputDir;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.outputDir = new File((String) UIMAUtil.getRequiredConfigParameterValue(context,
				ExamplePOSPlainTextWriter.PARAM_OUTPUT_DIRECTORY));
		if (!this.outputDir.exists()) {
			this.outputDir.mkdirs();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String id = ViewURIUtil.getURI(jCas);
		int index = Math.max(id.lastIndexOf("/"), id.lastIndexOf("\\")) + 1;
		id = id.substring(index);
		PrintWriter outputWriter;
		try {
			outputWriter = new PrintWriter(new File(this.outputDir, id + ".pos"));
		}
		catch (FileNotFoundException e) {
			throw new AnalysisEngineProcessException(e);
		}
		for (Sentence sentence : AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
			for (Token token : AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class)) {
				outputWriter.print(token.getCoveredText());
				outputWriter.print('/');
				outputWriter.print(token.getPos());
				outputWriter.print(' ');
			}
			outputWriter.println();
		}
		outputWriter.close();
	}

	public static AnalysisEngineDescription getDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(ExamplePOSPlainTextWriter.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION, CleartkComponents.TYPE_PRIORITIES,
				ExamplePOSPlainTextWriter.PARAM_OUTPUT_DIRECTORY, outputDirectory);
	}
}
