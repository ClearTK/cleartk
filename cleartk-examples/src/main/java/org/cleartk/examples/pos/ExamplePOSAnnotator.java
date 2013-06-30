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
package org.cleartk.examples.pos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instances;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.classifier.feature.function.CharacterNGramFeatureFunction;
import org.cleartk.classifier.feature.function.CharacterNGramFeatureFunction.Orientation;
import org.cleartk.classifier.feature.function.FeatureFunctionExtractor;
import org.cleartk.classifier.feature.function.LowerCaseFeatureFunction;
import org.cleartk.classifier.feature.function.NumericTypeFeatureFunction;
import org.cleartk.classifier.jar.DefaultDataWriterFactory;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.opennlp.MaxentStringOutcomeDataWriter;
import org.cleartk.classifier.viterbi.DefaultOutcomeFeatureExtractor;
import org.cleartk.classifier.viterbi.ViterbiDataWriterFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Changes to this class may require corresponding updates to the tutorial that describes this code
 * in detail: http://code.google.com/p/cleartk/wiki/Tutorial
 * 
 * @author Steven Bethard
 */
public class ExamplePOSAnnotator extends CleartkSequenceAnnotator<String> {

  public static final String DEFAULT_OUTPUT_DIRECTORY = "target/examples/pos";

  private SimpleFeatureExtractor tokenFeatureExtractor;

  private CleartkExtractor contextFeatureExtractor;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // a feature extractor that creates features corresponding to the word, the word lower cased
    // the capitalization of the word, the numeric characterization of the word, and character ngram
    // suffixes of length 2 and 3.
    this.tokenFeatureExtractor = new FeatureFunctionExtractor(
        new CoveredTextExtractor(),
        new LowerCaseFeatureFunction(),
        new CapitalTypeFeatureFunction(),
        new NumericTypeFeatureFunction(),
        new CharacterNGramFeatureFunction(Orientation.RIGHT_TO_LEFT, 0, 2),
        new CharacterNGramFeatureFunction(Orientation.RIGHT_TO_LEFT, 0, 3));

    // a feature extractor that extracts the surrounding token texts (within the same sentence)
    this.contextFeatureExtractor = new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Preceding(2),
        new Following(2));
  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // for each sentence in the document, generate training/classification instances
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<List<Feature>> tokenFeatureLists = new ArrayList<List<Feature>>();
      List<String> tokenOutcomes = new ArrayList<String>();

      // for each token, extract features and the outcome
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
      for (Token token : tokens) {

        // apply the two feature extractors
        List<Feature> tokenFeatures = new ArrayList<Feature>();
        tokenFeatures.addAll(this.tokenFeatureExtractor.extract(jCas, token));
        tokenFeatures.addAll(this.contextFeatureExtractor.extractWithin(jCas, token, sentence));
        tokenFeatureLists.add(tokenFeatures);

        // add the expected token label from the part of speech
        if (this.isTraining()) {
          tokenOutcomes.add(token.getPos());
        }
      }

      // for training, write instances to the data write
      if (this.isTraining()) {
        this.dataWriter.write(Instances.toInstances(tokenOutcomes, tokenFeatureLists));
      }

      // for classification, set the token part of speech tags from the classifier outcomes.
      else {
        List<String> outcomes = this.classifier.classify(tokenFeatureLists);
        Iterator<Token> tokensIter = tokens.iterator();
        for (String outcome : outcomes) {
          tokensIter.next().setPos(outcome);
        }
      }
    }
  }

  public static AnalysisEngineDescription getClassifierDescription(String modelFileName)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ExamplePOSAnnotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        modelFileName);
  }

  public static AnalysisEngineDescription getWriterDescription(String outputDirectory)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ExamplePOSAnnotator.class,
        CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        ViterbiDataWriterFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory,
        ViterbiDataWriterFactory.PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS,
        DefaultDataWriterFactory.class.getName(),
        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        MaxentStringOutcomeDataWriter.class.getName(),
        ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES,
        new String[] { DefaultOutcomeFeatureExtractor.class.getName() });
  }
}