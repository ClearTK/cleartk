/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.timeml.tlink;

import static org.cleartk.syntax.constituent.type.TreebankNodeUtil.selectHighestCoveredTreebankNode;
import static org.cleartk.syntax.constituent.type.TreebankNodeUtil.selectMatchingLeaf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.feature.extractor.BetweenAnnotationsFeatureExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Bag;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Following;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Ngram;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Preceding;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.timeml.util.FilteringExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TemporalLinkEventToSameSentenceTimeAnnotator extends
    TemporalLinkAnnotator_ImplBase<Event, Time> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {
    @Override
    public Class<?> getAnnotatorClass() {
      return TemporalLinkEventToSameSentenceTimeAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterFactoryClass() {
      return DefaultMaxentDataWriterFactory.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(
          TemporalLinkEventToSameSentenceTimeAnnotator.class,
          TimeMLComponents.TYPE_SYSTEM_DESCRIPTION);
    }
  };

  public TemporalLinkEventToSameSentenceTimeAnnotator() {
    super(Event.class, Time.class, "BEFORE", "OVERLAP", "AFTER");
  }

  private static final Pattern SUBORDINATE_PATH_PATTERN = Pattern.compile("^((NP|PP|ADVP)>)*((VP|SBAR|S)>)*(S|SBAR|VP|NP)(<(VP|SBAR|S))*(<(NP|PP|ADVP))*$");

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    final SimpleFeatureExtractor prepOrVerbExtractor = new FilteringExtractor<Token>(
        Token.class,
        new CoveredTextExtractor()) {
      @Override
      protected boolean accept(Token token) {
        return token.getPos().equals("TO") || token.getPos().equals("IN")
            || token.getPos().startsWith("VB");
      }
    };

    this.setSourceExtractors(Arrays.asList(
        new TypePathExtractor(Event.class, "tense"),
        new TypePathExtractor(Event.class, "eventClass"),
        new ContextExtractor<Token>(Token.class, prepOrVerbExtractor, new Ngram(new Following(5)))));

    this.setTargetExtractors(Arrays.asList(
        new ContextExtractor<Token>(Token.class, new CoveredTextExtractor(), new Bag(new Covered())),
        new TypePathExtractor(Time.class, "timeType"),
        new TypePathExtractor(Time.class, "value"),
        new ContextExtractor<Token>(Token.class, prepOrVerbExtractor, new Ngram(new Preceding(5)))));

    // this will probably only extract when the source (Event) precedes the target (Time)
    this.setBetweenExtractors(Arrays.<BetweenAnnotationsFeatureExtractor> asList(new ContextExtractor<Token>(
        Token.class,
        prepOrVerbExtractor,
        new Bag(new Covered()))));
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Map<Event, Map<Time, TemporalLink>> links = this.getLinks(jCas);

    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Event event : JCasUtil.selectCovered(jCas, Event.class, sentence)) {
        for (Time time : getSubordinateTimes(event, sentence, jCas)) {
          this.processLink(event, time, links, jCas);
        }
      }
    }
    this.logSkippedLinks(jCas, links);
  }

  private static List<Time> getSubordinateTimes(Event event, Sentence sentence, JCas jCas) {
    List<Time> times = new ArrayList<Time>();
    TreebankNode eventNode = selectMatchingLeaf(jCas, event);
    for (Time time : JCasUtil.selectCovered(jCas, Time.class, sentence)) {
      TreebankNode timeNode = selectHighestCoveredTreebankNode(jCas, time);
      if (eventNode != null && timeNode != null) {
        String path = noLeavesPath(TreebankNodeUtil.getPath(eventNode, timeNode));
        if (SUBORDINATE_PATH_PATTERN.matcher(path).matches()) {
          times.add(time);
        }
      }
    }
    return times;
  }
}
