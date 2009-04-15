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
package org.cleartk.ne.term;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.term.util.SimpleTermFinder;
import org.cleartk.ne.term.util.TermFinder;
import org.cleartk.ne.term.util.TermList;
import org.cleartk.ne.term.util.TermMatch;
import org.cleartk.token.util.PennTreebankTokenizer;
import org.cleartk.token.util.Token;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip Ogren
 * 
 * This annotator provides a UIMA wrapper around the SimpleTermFinder class.  
 */

public class TermFinderAnnotator extends JCasAnnotator_ImplBase {

	/**
	 * "TermListListing" is a single, required string parameter that points to a
	 * file that contains a listing of term list files that are to be loaded.
	 * Each line of the file should contain the name of a term list followed by
	 * the name of the file that contains the terms, a boolean ('true' or
	 * 'false') that indicates whether the file should be treated as case
	 * sensitive followed optionally by separator string to be used to separate
	 * an id from a term if the file contains ids. The values on each line
	 * should be tab delimited. Also, see data/lexicons/README
	 */

	public static final String PARAM_TERM_LIST_LISTING = "org.cleartk.ne.term.TermFinderAnnotator.PARAM_TERM_LIST_LISTING";

	/**
	 * "SentenceClass" is a single, optional, string parameter that names the
	 * class of the type system type from which to extract tokens. Any
	 * annotation that contains tokens can be used (e.g. sentence, paragraph,
	 * document). If no value is given for this parameter, then all tokens will
	 * be searched. An example value might be: <br>
	 * <code>org.cleartk.type.Sentence</code>
	 */
	public static final String PARAM_SENTENCE_CLASS = "org.cleartk.ne.term.TermFinderAnnotator.PARAM_SENTENCE_CLASS";

	/**
	 * "TokenClass" is a single, required, string parameter that names the class
	 * of the type system type corresponding to tokens. A good value for this
	 * would be: <br>
	 * <code>org.cleartk.type.Token</code>
	 */
	public static final String PARAM_TOKEN_CLASS = "org.cleartk.ne.term.TermFinderAnnotator.PARAM_TOKEN_CLASS";

	/**
	 * "TermMatchAnnotationCreator" is a single, optional, string parameter that
	 * provides the class name of a class that extends
	 * org.cleartk.bio.TermMatchAnnotationCreator. If this parameter is
	 * not given a value, then the parameter PARAM_TERM_MATCH_ANNOTATION_CLASS
	 * must be given a value.
	 */
	public static final String PARAM_TERM_MATCH_ANNOTATION_CREATOR = "org.cleartk.ne.term.TermFinderAnnotator.PARAM_TERM_MATCH_ANNOTATION_CREATOR";

	/**
	 * "TermMatchAnnotationClass" is a single, optional, string parameter that
	 * names the class of the type system type that specifies the annotations
	 * created of found term matches. One annotation is created for each term
	 * match found of the given type specified by this parameter. This parameter
	 * is ignored if PARAM_TERM_MATCH_ANNOTATION_CREATOR is given a value. An
	 * example value might be something like: <br>
	 * <code>org.cleartk.type.ne.NamedEntityMention</code>
	 */
	public static final String PARAM_TERM_MATCH_ANNOTATION_CLASS = "org.cleartk.ne.term.TermFinderAnnotator.PARAM_TERM_MATCH_ANNOTATION_CLASS";

	TermFinder caseSensitiveTermFinder;

	TermFinder caseInsensitiveTermFinder;

	boolean allTokens = true;

	boolean typesInitialized = false;

	protected Class<? extends Annotation> sentenceClass;

	protected Type sentenceType;

	protected Class<? extends Annotation> tokenClass;

	protected Type tokenType;

	TermMatchAnnotationCreator annotationCreator;

	Constructor<? extends Annotation> annotationConstructor;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			// load the term lists
			String termLists = (String) context.getConfigParameterValue(PARAM_TERM_LIST_LISTING);
			BufferedReader input = new BufferedReader(new FileReader(termLists));
			String line;
			while ((line = input.readLine()) != null) {
				line = line.trim();
				String[] columns = line.split("\t");
				String termListName = columns[0];
				String fileName = columns[1];
				boolean caseSensitive = Boolean.parseBoolean(columns[2]);
				String separator = columns.length == 4 ? columns[3] : null;

				if (caseSensitive && caseSensitiveTermFinder == null) caseSensitiveTermFinder = new SimpleTermFinder(
						true, new PennTreebankTokenizer());
				if (!caseSensitive && caseInsensitiveTermFinder == null) caseInsensitiveTermFinder = new SimpleTermFinder(
						false, new PennTreebankTokenizer());

				TermList termList = TermList.loadSimpleFile(termListName, new File(fileName), separator);
				if (caseSensitive) caseSensitiveTermFinder.addTermList(termList);
				else caseInsensitiveTermFinder.addTermList(termList);
			}

			// sentence class
			String sentenceClassName = (String) context.getConfigParameterValue(PARAM_SENTENCE_CLASS);
			if (sentenceClassName != null) {
				allTokens = false;
				Class<?> cls = Class.forName(sentenceClassName);
				sentenceClass = cls.asSubclass(Annotation.class);
			}

			// token class
			String tokenClassName = (String) context.getConfigParameterValue(PARAM_TOKEN_CLASS);
			Class<?> cls = Class.forName(tokenClassName);
			tokenClass = cls.asSubclass(Annotation.class);

			// annotation creator or constructor
			String termMatchAnnotationCreatorClassName = (String) context
					.getConfigParameterValue(PARAM_TERM_MATCH_ANNOTATION_CREATOR);
			if (termMatchAnnotationCreatorClassName != null && !termMatchAnnotationCreatorClassName.equals("")) {
				cls = Class.forName(termMatchAnnotationCreatorClassName);
				Class<? extends TermMatchAnnotationCreator> annotationCreatorClass = cls
						.asSubclass(TermMatchAnnotationCreator.class);
				annotationCreator = annotationCreatorClass.newInstance();
				annotationCreator.initialize(context);
			}
			else {
				// annotationConstructor
				String annotationClassName = (String) context
						.getConfigParameterValue(PARAM_TERM_MATCH_ANNOTATION_CLASS);
				cls = Class.forName(annotationClassName);
				Class<? extends Annotation> annotationClass = cls.asSubclass(Annotation.class);
				annotationConstructor = annotationClass.getConstructor(new Class[] { JCas.class,
						java.lang.Integer.TYPE, java.lang.Integer.TYPE });
			}
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	private void initializeTypes(JCas jCas) {
		if (!allTokens) sentenceType = UIMAUtil.getCasType(jCas, sentenceClass);
		tokenType = UIMAUtil.getCasType(jCas, tokenClass);
		typesInitialized = true;
	}

	private Token createToken(Annotation annotation) {
		return new Token(annotation.getBegin(), annotation.getEnd(), annotation.getCoveredText());
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		if (!typesInitialized) initializeTypes(jCas);

		List<Token> tokens = new ArrayList<Token>();
		if (allTokens) {
			FSIterator tokenAnnotations = jCas.getAnnotationIndex(tokenType).iterator();
			while (tokenAnnotations.hasNext()) {
				Annotation tokenAnnotation = (Annotation) tokenAnnotations.next();
				tokens.add(createToken(tokenAnnotation));
			}
			findTerms(jCas, tokens);
		}
		else {
			FSIterator sentenceAnnotations = jCas.getAnnotationIndex(sentenceType).iterator();
			while (sentenceAnnotations.hasNext()) {
				tokens.clear();
				Annotation sentenceAnnotation = (Annotation) sentenceAnnotations.next();
				FSIterator tokenAnnotations = jCas.getAnnotationIndex(tokenType).subiterator(sentenceAnnotation);
				while (tokenAnnotations.hasNext()) {
					Annotation tokenAnnotation = (Annotation) tokenAnnotations.next();
					tokens.add(createToken(tokenAnnotation));
				}
				findTerms(jCas, tokens);
			}
		}
	}

	public void findTerms(JCas jCas, List<Token> tokens) throws AnalysisEngineProcessException {

		if (caseSensitiveTermFinder != null) {
			findTerms(jCas, tokens, caseSensitiveTermFinder);
		}
		if (caseInsensitiveTermFinder != null) {
			findTerms(jCas, tokens, caseInsensitiveTermFinder);
		}
	}

	public void findTerms(JCas jCas, List<Token> tokens, TermFinder termFinder) throws AnalysisEngineProcessException {

		List<TermMatch> termMatches = termFinder.getMatches(tokens);
		for (TermMatch termMatch : termMatches) {
			if (annotationCreator != null) {
				annotationCreator.createTermMatchAnnotation(jCas, termMatch);
			}
			else {
				try {
					int begin = termMatch.getBegin();
					int end = termMatch.getEnd();
					Annotation annotation = annotationConstructor.newInstance(new Object[] { jCas, begin, end });
					annotation.addToIndexes();
				}
				catch (Exception e) {
					throw new AnalysisEngineProcessException(e);
				}
			}

		}
	}

}
