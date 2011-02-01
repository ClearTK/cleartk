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
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.CleartkSequentialAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.feature.proliferate.CapitalTypeProliferator;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.NumericTypeProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.opennlp.MaxentDataWriterFactory_ImplBase;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */
public class ExamplePOSAnnotator extends CleartkSequentialAnnotator<String> {

  public static final String DEFAULT_OUTPUT_DIRECTORY = "target/examples/pos";

  public static final String DEFAULT_MODEL = "target/examples/pos/model.jar";

  private List<SimpleFeatureExtractor> tokenFeatureExtractors;

  private List<WindowExtractor> tokenSentenceFeatureExtractors;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // a list of feature extractors that require only the token
    this.tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();

    // a list of feature extractors that require the token and the sentence
    this.tokenSentenceFeatureExtractors = new ArrayList<WindowExtractor>();

    // basic feature extractors for word, stem and part-of-speech
    SimpleFeatureExtractor wordExtractor, stemExtractor;
    wordExtractor = new SpannedTextExtractor();
    stemExtractor = new TypePathExtractor(Token.class, "stem");

    // aliases for NGram feature parameters
    int fromRight = CharacterNGramProliferator.RIGHT_TO_LEFT;

    // add the feature extractor for the word itself
    // also add proliferators which create new features from the word text
    this.tokenFeatureExtractors.add(new ProliferatingExtractor(
        wordExtractor,
        new LowerCaseProliferator(),
        new CapitalTypeProliferator(),
        new NumericTypeProliferator(),
        new CharacterNGramProliferator(fromRight, 0, 2),
        new CharacterNGramProliferator(fromRight, 0, 3)));

    // add the feature extractors for the stem and part of speech
    this.tokenFeatureExtractors.add(stemExtractor);

    // add 2 stems to the left and right
    this.tokenSentenceFeatureExtractors.add(new WindowExtractor(
        Token.class,
        stemExtractor,
        WindowFeature.ORIENTATION_LEFT,
        0,
        2));
    this.tokenSentenceFeatureExtractors.add(new WindowExtractor(
        Token.class,
        stemExtractor,
        WindowFeature.ORIENTATION_RIGHT,
        0,
        2));

  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      // generate a list of training instances for each sentence in the
      // document
      for (Sentence sentence : AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
        List<Instance<String>> instances = new ArrayList<Instance<String>>();
        List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class);

        // for each token, extract all feature values and the label
        for (Token token : tokens) {
          Instance<String> instance = new Instance<String>();

          // extract all features that require only the token
          // annotation
          for (SimpleFeatureExtractor extractor : this.tokenFeatureExtractors) {
            instance.addAll(extractor.extract(jCas, token));
          }

          // extract all features that require the token and sentence
          // annotations
          for (WindowExtractor extractor : this.tokenSentenceFeatureExtractors) {
            instance.addAll(extractor.extract(jCas, token, sentence));
          }

          // set the instance label from the token's part of speech
          instance.setOutcome(token.getPos());

          // add the instance to the list
          instances.add(instance);
        }

        // for training, write instances to the data write
        if (this.isTraining()) {
          this.sequentialDataWriter.writeSequence(instances);
        }

        // for classification, set the labels as the token POS labels
        else {
          Iterator<Token> tokensIter = tokens.iterator();
          for (String label : this.classifySequence(instances)) {
            tokensIter.next().setPos(label.toString());
          }
        }
      }
    } catch (CleartkException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  public static AnalysisEngineDescription getClassifierDescription(String modelFileName)
      throws ResourceInitializationException {
    return CleartkAnnotatorDescriptionFactory.createCleartkSequentialAnnotator(
        ExamplePOSAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        modelFileName);
  }

  public static AnalysisEngineDescription getWriterDescription(String outputDirectory)
      throws ResourceInitializationException {
    AnalysisEngineDescription aed = CleartkAnnotatorDescriptionFactory.createViterbiAnnotator(
        ExamplePOSAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        DefaultMaxentDataWriterFactory.class,
        outputDirectory);
    ConfigurationParameterFactory.addConfigurationParameter(
        aed,
        MaxentDataWriterFactory_ImplBase.PARAM_COMPRESS,
        true);
    return aed;
  }
}