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
package org.cleartk.token.snowball;

import net.sf.snowball.SnowballProgram;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 * 
 *         This class borrows from
 *         org.apache.lucene.analysis.snowball.SnowballFilter
 * @see org.apache.lucene.analysis.snowball.SnowballFilter
 */
public class SnowballStemmer extends JCasAnnotator_ImplBase {
	/**
	 * "StemmerName" is required, string parameter that specifies which snowball
	 * stemmer to use. Possible values are:
	 * <ul>
	 * <li>Danish</li>
	 * <li>Dutch</li>
	 * <li>English</li>
	 * <li>Finnish</li>
	 * <li>French</li>
	 * <li>German2</li>
	 * <li>German</li>
	 * <li>Italian</li>
	 * <li>Kp</li>
	 * <li>Lovins</li>
	 * <li>Norwegian</li>
	 * <li>Porter</li>
	 * <li>Portuguese</li>
	 * <li>Russian</li>
	 * <li>Spanish</li>
	 * <li>Swedish</li>
	 * </ul>
	 */
	public static final String PARAM_STEMMER_NAME = "org.cleartk.token.snowball.SnowballStemmer.PARAM_STEMMER_NAME";

	protected SnowballProgram stemmer;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		String stemmerName = (String) UIMAUtil.getRequiredConfigParameterValue(context,
				SnowballStemmer.PARAM_STEMMER_NAME);
		String className = String.format("net.sf.snowball.ext.%sStemmer", stemmerName);
		try {
			this.stemmer = (SnowballProgram) Class.forName(className).newInstance();
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Token token : AnnotationRetrieval.getAnnotations(jCas, Token.class)) {
			stemmer.setCurrent(token.getCoveredText().toLowerCase());
			stemmer.stem();
			String stem = stemmer.getCurrent();
			token.setStem(stem);
		}
	}

}
