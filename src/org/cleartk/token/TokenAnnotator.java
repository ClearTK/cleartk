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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.ClearTKComponents;
import org.cleartk.token.util.PennTreebankTokenizer;
import org.cleartk.token.util.Token;
import org.cleartk.token.util.Tokenizer;
import org.cleartk.type.Sentence;
import org.cleartk.util.UIMAUtil;
import org.uutuc.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * 
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {
	/**
	 * "org.cleartk.token.TokenAnnotator.PARAM_TOKENIZER" is a single, optional,
	 * string parameter that specifies the class type of the tokenizer that will
	 * be used by this annotator. If this parameter is not filled, then the
	 * default tokenenizer (org.cleartk.token.util.PennTreebankTokenizer) is
	 * used. A tokenenizer is defined as any implementation of the interface
	 * defined by org.cleartk.token.util.Tokenizer.
	 * 
	 * @see Tokenizer
	 * @see PennTreebankTokenizer
	 */
	public static final String PARAM_TOKENIZER = "org.cleartk.token.TokenAnnotator.PARAM_TOKENIZER";

	/**
	 * "org.cleartk.token.TokenAnnotator.PARAM_TOKEN_TYPE" is a single,
	 * optional, string parameter the class type of the tokens that are created
	 * by this annotator. If this parameter is not filled, then tokens of type
	 * org.cleartk.type.Token will be created.
	 */
	public static final String PARAM_TOKEN_TYPE = "org.cleartk.token.TokenAnnotator.PARAM_TOKEN_TYPE";

	/**
	 * "org.cleartk.token.TokenAnnotator.PARAM_WINDOW_TYPE" is a single,
	 * optional, string parameter that specifies the class type of annotations
	 * that will be tokenized. If no value is given, then the entire document
	 * will be tokenized at once. A good value for this parameter would be
	 * <code>org.cleartk.type.Sentence</code> (especially when using the
	 * PennTreebankTokenizer).
	 */
	public static final String PARAM_WINDOW_TYPE = "org.cleartk.token.TokenAnnotator.PARAM_WINDOW_TYPE";

	Tokenizer tokenizer;

	Class<? extends Annotation> tokenClass;

	Constructor<? extends Annotation> tokenConstructor;

	private Class<? extends Annotation> windowAnnotationClass;

	private Type windowAnnotationType = null;

	private boolean typesInitialized = false;

	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		try {
			super.initialize(uimaContext);

			String tokenCreatorClassName = (String) uimaContext.getConfigParameterValue(PARAM_TOKENIZER);
			if (tokenCreatorClassName != null && !tokenCreatorClassName.trim().equals("")) {
				Class<?> cls = Class.forName(tokenCreatorClassName);
				Class<? extends Tokenizer> tokensClass = cls.asSubclass(Tokenizer.class);
				tokenizer = tokensClass.newInstance();
			}
			else {
				tokenizer = new PennTreebankTokenizer();
				windowAnnotationClass = Sentence.class;
			}
			String tokenClassName = (String) uimaContext.getConfigParameterValue(PARAM_TOKEN_TYPE);
			if (tokenClassName != null && !tokenClassName.trim().equals("")) {
				Class<?> cls = Class.forName(tokenClassName);
				tokenClass = cls.asSubclass(Annotation.class);
			}
			else {
				tokenClass = org.cleartk.type.Token.class;
			}
			tokenConstructor = tokenClass.getConstructor(new Class[] { JCas.class, Integer.TYPE, Integer.TYPE });

			String windowAnnotationClassName = (String) uimaContext.getConfigParameterValue(PARAM_WINDOW_TYPE);
			if (windowAnnotationClassName != null && !windowAnnotationClassName.trim().equals("")) {
				Class<?> cls = Class.forName(windowAnnotationClassName);
				windowAnnotationClass = cls.asSubclass(Annotation.class);
			}

		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	private void initializeTypes(JCas jCas) throws AnalysisEngineProcessException {
		if (windowAnnotationClass != null) {
			windowAnnotationType = UIMAUtil.getCasType(jCas, windowAnnotationClass);
		}
		typesInitialized = true;
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			if (!typesInitialized) initializeTypes(jCas);
			if (windowAnnotationType != null) {
				FSIterator windows = jCas.getAnnotationIndex(windowAnnotationType).iterator();
				while (windows.hasNext()) {
					Annotation window = (Annotation) windows.next();
					List<Token> pojoTokens = tokenizer.getTokens(window.getCoveredText());
					createTokens(pojoTokens, window.getBegin(), jCas);
				}
			}
			else {
				String text = jCas.getDocumentText();
				List<Token> pojoTokens = tokenizer.getTokens(text);
				createTokens(pojoTokens, 0, jCas);
			}
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void createTokens(List<Token> pojoTokens, int offset, JCas jCas) throws InstantiationException,
			InvocationTargetException, IllegalAccessException {
		for (Token pojoToken : pojoTokens) {
			int tokenBegin = pojoToken.getBegin() + offset;
			int tokenEnd = pojoToken.getEnd() + offset;
			tokenConstructor.newInstance(jCas, tokenBegin, tokenEnd).addToIndexes();
		}
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(TokenAnnotator.class,
				ClearTKComponents.TYPE_SYSTEM_DESCRIPTION, ClearTKComponents.TYPE_PRIORITIES);

	}

	public static AnalysisEngineDescription getDescription(TypeSystemDescription typeSystemDescription,
			TypePriorities typePriorities, Class<? extends Tokenizer> tokenizerClass,
			Class<? extends Annotation> tokenClass, Class<? extends Annotation> windowClass)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(TokenAnnotator.class,
				typeSystemDescription, typePriorities, TokenAnnotator.PARAM_TOKENIZER, tokenizerClass.getName(),
				TokenAnnotator.PARAM_TOKEN_TYPE, tokenClass.getName(), TokenAnnotator.PARAM_WINDOW_TYPE, windowClass
						.getName());
	}

}
