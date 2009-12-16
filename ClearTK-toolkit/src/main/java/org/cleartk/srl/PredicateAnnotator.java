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

import java.io.File;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
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
public class PredicateAnnotator extends CleartkAnnotator<Boolean> {

	public static AnalysisEngineDescription getWriterDescription(
			Class<? extends DataWriterFactory<Boolean>> dataWriterFactoryClass, File outputDirectory)
	throws ResourceInitializationException {
		return CleartkComponents.createPrimitiveDescription(
				ArgumentIdentifier.class,
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, dataWriterFactoryClass.getName(),
				CleartkAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory.toString());
	}

	public static AnalysisEngineDescription getClassifierDescription(File classifierJar)
	throws ResourceInitializationException {
		return CleartkComponents.createPrimitiveDescription(
				PredicateAnnotator.class,
				CleartkAnnotator.PARAM_CLASSIFIER_JAR_PATH, classifierJar.toString());
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
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

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
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
					
					if (this.isTraining()) {
						this.dataWriter.write(instance);
					} else {
						Boolean outcome = this.classifier.classify(instance.getFeatures());
						if (outcome) {
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
		} catch (CASRuntimeException e) {
			throw new AnalysisEngineProcessException(e);
		} catch (CleartkException e) {
			throw new AnalysisEngineProcessException(e);
		} 
	}

	private CombinedExtractor tokenExtractor;
	private WindowExtractor leftWindowExtractor;
	private WindowExtractor rightWindowExtractor;
}
