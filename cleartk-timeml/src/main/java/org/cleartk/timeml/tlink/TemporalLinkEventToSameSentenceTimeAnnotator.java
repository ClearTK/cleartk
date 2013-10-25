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
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;
import org.cleartk.classifier.feature.extractor.FeatureExtractor2;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Bag;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Covered;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Following;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Ngram;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.classifier.liblinear.LibLinearStringOutcomeDataWriter;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.Time;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.feature.FilteringExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.collect.Lists;

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
    public Class<?> getDataWriterClass() {
      return LibLinearStringOutcomeDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createEngineDescription(TemporalLinkEventToSameSentenceTimeAnnotator.class);
    }
  };

  public TemporalLinkEventToSameSentenceTimeAnnotator() {
    super(Event.class, Time.class, "INCLUDES", "IS_INCLUDED");
  }

  private static final Pattern SUBORDINATE_PATH_PATTERN = Pattern.compile("^((NP|PP|ADVP)>)*((VP|SBAR|S)>)*(S|SBAR|VP|NP)(<(VP|SBAR|S))*(<(NP|PP|ADVP))*$");

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    final FeatureExtractor1<Token> prepOrVerbExtractor = new FilteringExtractor<Token>(
        Token.class,
        new CoveredTextExtractor<Token>()) {
      @Override
      protected boolean accept(Token token) {
        return token.getPos().equals("TO") || token.getPos().equals("IN")
            || token.getPos().startsWith("VB");
      }
    };

    
    List<FeatureExtractor1<Event>> srcExtractors = Lists.newArrayList();
    srcExtractors.add(new TypePathExtractor<Event>(Event.class, "tense"));
    srcExtractors.add(new TypePathExtractor<Event>(Event.class, "eventClass"));
    srcExtractors.add(new CleartkExtractor<Event, Token>(Token.class, prepOrVerbExtractor, new Ngram(new Following(5))));
    this.setSourceExtractors(srcExtractors);
    
    List<FeatureExtractor1<Time>> tgtExtractors = Lists.newArrayList();
    tgtExtractors.add(new CleartkExtractor<Time, Token>(Token.class, new CoveredTextExtractor<Token>(), new Bag(new Covered())));
    tgtExtractors.add(new TypePathExtractor<Time>(Time.class, "timeType"));
    tgtExtractors.add(new TypePathExtractor<Time>(Time.class, "value"));
    tgtExtractors.add(new CleartkExtractor<Time, Token>(Token.class, prepOrVerbExtractor, new Ngram(new Preceding(5))));
    this.setTargetExtractors(tgtExtractors);

//    this.setTargetExtractors(Arrays.asList(
//        new CleartkExtractor<Time, Token>(Token.class, new CoveredTextExtractor(), new Bag(new Covered())),
//        new TypePathExtractor<Time>(Time.class, "timeType"),
//        new TypePathExtractor<Time>(Time.class, "value"),
//        new CleartkExtractor<Time, Token>(Token.class, prepOrVerbExtractor, new Ngram(new Preceding(5)))));

    // this will probably only extract when the source (Event) precedes the target (Time)
    
    List<FeatureExtractor2<Anchor, Anchor>> btweenExtractors = Lists.newArrayList();
    btweenExtractors.add(new CleartkExtractor<Anchor, Token>(
        Token.class,
        prepOrVerbExtractor,
        new Bag(new Covered())));
  }

  @Override
  protected List<SourceTargetPair> getSourceTargetPairs(JCas jCas) {
    List<SourceTargetPair> pairs = Lists.newArrayList();
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Event event : JCasUtil.selectCovered(jCas, Event.class, sentence)) {
        for (Time time : getSubordinateTimes(event, sentence, jCas)) {
          pairs.add(new SourceTargetPair(event, time));
        }
      }
    }
    return pairs;
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
