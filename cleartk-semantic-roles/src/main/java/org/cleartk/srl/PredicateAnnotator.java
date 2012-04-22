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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.srl.type.Predicate;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * 
 */
public class PredicateAnnotator extends CleartkAnnotator<Boolean> {

  public static AnalysisEngineDescription getWriterDescription(
      Class<? extends DataWriterFactory<Boolean>> dataWriterFactoryClass,
      File outputDirectory) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ArgumentIdentifier.class,
        SrlComponents.TYPE_SYSTEM_DESCRIPTION,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory.toString());
  }

  public static AnalysisEngineDescription getClassifierDescription(File classifierJar)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        PredicateAnnotator.class,
        SrlComponents.TYPE_SYSTEM_DESCRIPTION,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJar.toString());
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    SimpleFeatureExtractor[] tokenExtractors = {
        new CoveredTextExtractor(),
        new TypePathExtractor(Token.class, "stem"),
        new TypePathExtractor(Token.class, "pos") };

    tokenExtractor = new CombinedExtractor(tokenExtractors);

    contextExtractor = new ContextExtractor<Token>(Token.class, new NamingExtractor(
        "Token",
        new CombinedExtractor(tokenExtractors)), new Preceding(2), new Following(2));
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    nPredicates = 0;
    nSentences = 0;

    Set<Token> predicateTokens = new HashSet<Token>();
    for (Predicate predicate : JCasUtil.select(jCas, Predicate.class)) {
      for (Token token : JCasUtil.selectCovered(jCas, Token.class, predicate)) {
        predicateTokens.add(token);
      }
    }

    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      nSentences += 1;
      List<Token> tokenList = JCasUtil.selectCovered(jCas, Token.class, sentence);
      Token[] tokens = tokenList.toArray(new Token[tokenList.size()]);
      for (Token token : tokens) {
        Instance<Boolean> instance = new Instance<Boolean>();
        List<Feature> tokenFeatures = this.tokenExtractor.extract(jCas, token);
        List<Feature> windowFeatures = this.contextExtractor.extractWithin(jCas, token, sentence);

        instance.addAll(tokenFeatures);
        instance.addAll(windowFeatures);

        instance.setOutcome(predicateTokens.contains(token));

        if (this.isTraining()) {
          this.dataWriter.write(instance);
        } else {
          Boolean outcome = this.classifier.classify(instance.getFeatures());
          if (outcome) {
            nPredicates += 1;
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

    Logger.getLogger("org.cleartk.srl.PredicateAnnotator").info(
        String.format("processed %d sentences, found %d predicates", nSentences, nPredicates));
  }

  private int nSentences;

  private int nPredicates;

  private CombinedExtractor tokenExtractor;

  private ContextExtractor<Token> contextExtractor;
}
