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
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Bag;
import org.cleartk.classifier.feature.extractor.ContextExtractor.Covered;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.opennlp.MaxentDataWriter;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.timeml.util.SyntacticFirstChildOfGrandparentOfLeafExtractor;
import org.cleartk.timeml.util.SyntacticLeafToLeafPathPartsExtractor;
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
public class TemporalLinkEventToSubordinatedEventAnnotator extends
    TemporalLinkAnnotator_ImplBase<Event, Event> {

  public static final CleartkInternalModelFactory FACTORY = new CleartkInternalModelFactory() {
    @Override
    public Class<?> getAnnotatorClass() {
      return TemporalLinkEventToSubordinatedEventAnnotator.class;
    }

    @Override
    public Class<?> getDataWriterClass() {
      return MaxentDataWriter.class;
    }

    @Override
    public AnalysisEngineDescription getBaseDescription() throws ResourceInitializationException {
      return AnalysisEngineFactory.createPrimitiveDescription(
          TemporalLinkEventToSubordinatedEventAnnotator.class,
          TimeMLComponents.TYPE_SYSTEM_DESCRIPTION);
    }
  };

  public TemporalLinkEventToSubordinatedEventAnnotator() {
    super(Event.class, Event.class, "BEFORE", "OVERLAP", "AFTER");
  }

  private static final Pattern SUBORDINATE_PATH_PATTERN = Pattern.compile("^(VP>|ADJP>|NP>)?(VP|ADJP|S|SBAR)(<(S|SBAR|PP))*((<VP|<ADJP)*|(<NP)*)$");

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    List<SimpleFeatureExtractor> extractors = Arrays.<SimpleFeatureExtractor> asList(
        new TypePathExtractor(Event.class, "tense"),
        new TypePathExtractor(Event.class, "aspect"),
        new TypePathExtractor(Event.class, "eventClass"),
        new SyntacticFirstChildOfGrandparentOfLeafExtractor());
    this.setSourceExtractors(extractors);
    this.setTargetExtractors(extractors);
    this.setBetweenExtractors(Arrays.asList(
        new SyntacticLeafToLeafPathPartsExtractor(),
        new ContextExtractor<Token>(Token.class, new CoveredTextExtractor(), new Bag(new Covered()))));
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Map<Event, Map<Event, TemporalLink>> links = this.getLinks(jCas);

    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Event source : JCasUtil.selectCovered(jCas, Event.class, sentence)) {
        for (Event target : this.getSubordinateEvents(jCas, source, sentence)) {
          this.processLink(source, target, links, jCas);
        }
      }
    }

    this.logSkippedLinks(jCas, links);
  }

  private List<Event> getSubordinateEvents(JCas jCas, Event source, Sentence sentence) {
    List<Event> targets = new ArrayList<Event>();
    TreebankNode sourceNode = TreebankNodeUtil.selectMatchingLeaf(jCas, source);
    for (Event target : JCasUtil.selectCovered(jCas, Event.class, sentence)) {
      TreebankNode targetNode = TreebankNodeUtil.selectMatchingLeaf(jCas, target);
      if (sourceNode != null && targetNode != null) {
        String path = noLeavesPath(TreebankNodeUtil.getPath(sourceNode, targetNode));
        if (SUBORDINATE_PATH_PATTERN.matcher(path).matches()) {
          targets.add(target);
        }
      }
    }
    return targets;
  }
}
