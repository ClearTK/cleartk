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
package org.cleartk.srl;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.CleartkException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.svmlight.DefaultSVMlightDataWriterFactory;
import org.cleartk.srl.type.Predicate;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philipp Wetzler
 *
 */
public class PredicateAnnotationHandler implements AnnotationHandler<Boolean> {

	public static AnalysisEngineDescription createPredicateDataWriter(String outputDirectory)
	throws ResourceInitializationException {
		return CleartkComponents.createDataWriterAnnotator(
				PredicateAnnotationHandler.class,
				DefaultSVMlightDataWriterFactory.class, outputDirectory);
	}

	public static AnalysisEngineDescription createPredicateAnnotator(String classifierJar)
	throws ResourceInitializationException {
		return CleartkComponents.createClassifierAnnotator(
				PredicateAnnotationHandler.class, classifierJar);
	}

	public PredicateAnnotationHandler() {
		SimpleFeatureExtractor[] tokenExtractors = {
						new SpannedTextExtractor(), 
						new TypePathExtractor(Token.class, "stem"),
						new TypePathExtractor(Token.class, "pos") };

		tokenExtractor = new CombinedExtractor(tokenExtractors);
		
		leftWindowExtractor = new WindowExtractor("Token", Token.class,
				new CombinedExtractor( tokenExtractors ),
				WindowFeature.ORIENTATION_LEFT,
				0, 2);
		rightWindowExtractor = new WindowExtractor("Token", Token.class,
				new CombinedExtractor( tokenExtractors ),
				WindowFeature.ORIENTATION_RIGHT,
				0, 2);
	}

	public void process(JCas jCas, InstanceConsumer<Boolean> consumer) throws CleartkException {
		List<Sentence> sentences = AnnotationRetrieval.getAnnotations(jCas, Sentence.class);

		for( Sentence sentence : sentences ) {
			List<Token> tokenList = AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class); 
			Token[] tokens = tokenList.toArray(new Token[tokenList.size()]);
			for( Token token : tokens ) {
				Instance<Boolean> instance = new Instance<Boolean>();
				List<Feature> tokenFeatures = this.tokenExtractor.extract(jCas, token);
				List<Feature> leftWindowFeatures = this.leftWindowExtractor.extract(jCas, token, sentence);
				List<Feature> rightWindowFeatures = this.rightWindowExtractor.extract(jCas, token, sentence);

				instance.addAll(tokenFeatures);
				instance.addAll(leftWindowFeatures);
				instance.addAll(rightWindowFeatures);

				instance.setOutcome(false);
				FSIterator predicates = jCas.getAnnotationIndex(Predicate.type).subiterator(sentence);
				while( predicates.hasNext() ) {
					Predicate predicate = (Predicate) predicates.next();
					List<Token> predicateTokens = AnnotationRetrieval.getAnnotations(jCas, predicate.getAnnotation(), Token.class);
					if( predicateTokens.contains(token) ) {
						instance.setOutcome(true);
						break;
					}
				}
				Boolean outcome = consumer.consume(instance);
			
			
				if( outcome != null ) {
					if( outcome ) {
						Predicate predicate = new Predicate(jCas);
						predicate.setAnnotation(token);
						predicate.setBegin(token.getBegin());
						predicate.setEnd(token.getEnd());
						predicate.setSentence(sentence);
						predicate.addToIndexes();
					}
				}
			}
		} 
	}

	private CombinedExtractor tokenExtractor;
	private WindowExtractor leftWindowExtractor;
	private WindowExtractor rightWindowExtractor;
}
