/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
package org.cleartk.timeml.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.mallet.MalletStringOutcomeDataWriter;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Annotator for TimeML EVENT identification.
 * 
 * @author Steven Bethard
 */
public class EventAnnotator extends CleartkAnnotator<String> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {

    @Override
    public Class<?> getAnnotatorClass() {
      return EventAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterClass() {
      return MalletStringOutcomeDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(EventAnnotator.class);
    }
  };

  protected List<SimpleFeatureExtractor> tokenFeatureExtractors;

  protected List<CleartkExtractor> contextExtractors;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // add features: word, stem, pos
    this.tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.tokenFeatureExtractors.addAll(Arrays.asList(
        new CoveredTextExtractor(),
        new TypePathExtractor(Token.class, "stem"),
        new TypePathExtractor(Token.class, "pos"),
        new ParentNodeFeaturesExtractor()));

    // add window of features before and after
    this.contextExtractors = new ArrayList<CleartkExtractor>();
    this.contextExtractors.add(new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Preceding(3),
        new Following(3)));
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Set<Token> eventTokens = new HashSet<Token>();
    if (this.isTraining()) {
      for (Event event : JCasUtil.select(jCas, Event.class)) {
        for (Token token : JCasUtil.selectCovered(jCas, Token.class, event)) {
          eventTokens.add(token);
        }
      }
    }

    int index = 1;
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence)) {
        List<Feature> features = new ArrayList<Feature>();
        for (SimpleFeatureExtractor extractor : this.tokenFeatureExtractors) {
          features.addAll(extractor.extract(jCas, token));
        }
        for (CleartkExtractor extractor : this.contextExtractors) {
          features.addAll(extractor.extractWithin(jCas, token, sentence));
        }
        if (this.isTraining()) {
          String label = eventTokens.contains(token) ? "Event" : "O";
          this.dataWriter.write(new Instance<String>(label, features));
        } else {
          if (this.classifier.classify(features).equals("Event")) {
            Event event = new Event(jCas, token.getBegin(), token.getEnd());
            event.setId(String.format("e%d", index));
            event.addToIndexes();
            index += 1;
          }
        }
      }
    }
  }

  private static class ParentNodeFeaturesExtractor implements SimpleFeatureExtractor {
    public ParentNodeFeaturesExtractor() {
    }

    @Override
    public List<Feature> extract(JCas view, Annotation focusAnnotation)
        throws CleartkExtractorException {
      TreebankNode node = TreebankNodeUtil.selectMatchingLeaf(view, focusAnnotation);
      List<Feature> features = new ArrayList<Feature>();
      if (node != null) {
        TreebankNode parent = node.getParent();
        if (parent != null) {
          features.add(new Feature("ParentNodeType", parent.getNodeType()));
          TreebankNode firstSibling = parent.getChildren(0);
          if (firstSibling != node && firstSibling.getLeaf()) {
            features.add(new Feature("FirstSiblingText", firstSibling.getCoveredText()));
          }
        }
      }
      return features;
    }
  }
}
