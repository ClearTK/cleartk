/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.examples.chunking;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instances;
import org.cleartk.ml.chunking.BioChunking;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CombinedExtractor1;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction.PatternType;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

/**
 * This is the most important class in the named entity chunking example -- it demonstrates how to
 * write a ClearTK annotator that creates NamedEntityMention annotations by classifying Token
 * annotations.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class NamedEntityChunker extends CleartkSequenceAnnotator<String> {

  private FeatureExtractor1<Token> extractor;

  private CleartkExtractor<Token, Token> contextExtractor;

  private BioChunking<Token, NamedEntityMention> chunking;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // the token feature extractor: text, char pattern (uppercase, digits, etc.), and part-of-speech
    this.extractor = new CombinedExtractor1<Token>(
        new FeatureFunctionExtractor<Token>(
            new CoveredTextExtractor<Token>(),
            new CharacterCategoryPatternFunction<Token>(PatternType.REPEATS_MERGED)),
        new TypePathExtractor<Token>(Token.class, "pos"));

    // the context feature extractor: the features above for the 3 preceding and 3 following tokens
    this.contextExtractor = new CleartkExtractor<Token, Token>(
        Token.class,
        this.extractor,
        new Preceding(3),
        new Following(3));

    // the chunking definition: Tokens will be combined to form NamedEntityMentions, with labels
    // from the "mentionType" attribute so that we get B-location, I-person, etc.
    this.chunking = new BioChunking<Token, NamedEntityMention>(
        Token.class,
        NamedEntityMention.class,
        "mentionType");
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

      // extract features for each token in the sentence
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
      List<List<Feature>> featureLists = new ArrayList<List<Feature>>();
      for (Token token : tokens) {
        List<Feature> features = new ArrayList<Feature>();
        features.addAll(this.extractor.extract(jCas, token));
        features.addAll(this.contextExtractor.extract(jCas, token));
        featureLists.add(features);
      }

      // during training, convert NamedEntityMentions in the CAS into expected classifier outcomes
      if (this.isTraining()) {

        // extract the gold (human annotated) NamedEntityMention annotations
        List<NamedEntityMention> namedEntityMentions = JCasUtil.selectCovered(
            jCas,
            NamedEntityMention.class,
            sentence);

        // convert the NamedEntityMention annotations into token-level BIO outcome labels
        List<String> outcomes = this.chunking.createOutcomes(jCas, tokens, namedEntityMentions);

        // write the features and outcomes as training instances
        this.dataWriter.write(Instances.toInstances(outcomes, featureLists));
      }

      // during classification, convert classifier outcomes into NamedEntityMentions in the CAS
      else {

        // get the predicted BIO outcome labels from the classifier
        List<String> outcomes = this.classifier.classify(featureLists);

        // create the NamedEntityMention annotations in the CAS
        this.chunking.createChunks(jCas, tokens, outcomes);
      }
    }
  }
}
