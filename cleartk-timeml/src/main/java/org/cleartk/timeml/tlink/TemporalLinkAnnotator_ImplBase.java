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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.BetweenAnnotationsFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil.TreebankNodePath;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.token.type.Sentence;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class TemporalLinkAnnotator_ImplBase<SOURCE extends Anchor, TARGET extends Anchor>
    extends CleartkAnnotator<String> {

  private Class<SOURCE> sourceClass;

  private Class<TARGET> targetClass;

  private Set<String> trainingRelationTypes;

  protected List<SimpleFeatureExtractor> sourceExtractors;

  protected List<SimpleFeatureExtractor> targetExtractors;

  protected List<BetweenAnnotationsFeatureExtractor> betweenExtractors;

  public TemporalLinkAnnotator_ImplBase(
      Class<SOURCE> sourceClass,
      Class<TARGET> targetClass,
      String... trainingRelationTypes) {
    this.sourceClass = sourceClass;
    this.targetClass = targetClass;
    this.trainingRelationTypes = new HashSet<String>(Arrays.asList(trainingRelationTypes));
    this.sourceExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.targetExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.betweenExtractors = new ArrayList<BetweenAnnotationsFeatureExtractor>();
  }

  protected void setSourceExtractors(List<SimpleFeatureExtractor> extractors) {
    this.sourceExtractors = new ArrayList<SimpleFeatureExtractor>();
    for (SimpleFeatureExtractor extractor : extractors) {
      this.sourceExtractors.add(new NamingExtractor("Source", extractor));
    }
  }

  protected void setTargetExtractors(List<SimpleFeatureExtractor> extractors) {
    this.targetExtractors = new ArrayList<SimpleFeatureExtractor>();
    for (SimpleFeatureExtractor extractor : extractors) {
      this.targetExtractors.add(new NamingExtractor("Target", extractor));
    }
  }

  protected void setBetweenExtractors(List<BetweenAnnotationsFeatureExtractor> extractors) {
    this.betweenExtractors = extractors;
  }

  protected Map<SOURCE, Map<TARGET, TemporalLink>> getLinks(JCas jCas) {
    Map<SOURCE, Map<TARGET, TemporalLink>> links = new HashMap<SOURCE, Map<TARGET, TemporalLink>>();
    if (this.isTraining()) {
      for (TemporalLink tlink : JCasUtil.select(jCas, TemporalLink.class)) {
        SOURCE source;
        try {
          source = this.sourceClass.cast(tlink.getSource());
        } catch (ClassCastException e) {
          String message = "Expected all sources to be of type %s, found %s";
          throw new RuntimeException(String.format(message, this.sourceClass, tlink.getSource()));
        }
        TARGET target;
        try {
          target = this.targetClass.cast(tlink.getTarget());
        } catch (ClassCastException e) {
          String message = "Expected all targets to be of type %s, found %s";
          throw new RuntimeException(String.format(message, this.targetClass, tlink.getSource()));
        }
        if (!links.containsKey(source)) {
          links.put(source, new HashMap<TARGET, TemporalLink>());
        }
        if (links.get(source).containsKey(target)) {
          String message = "Duplicate link between source %s and target %s";
          throw new RuntimeException(String.format(message, source.getId(), target.getId()));
        }
        links.get(source).put(target, tlink);
      }
    }
    return links;
  }

  protected void processLink(
      SOURCE source,
      TARGET target,
      Map<SOURCE, Map<TARGET, TemporalLink>> links,
      JCas jCas) throws CleartkProcessingException {

    List<Feature> features = new ArrayList<Feature>();
    for (SimpleFeatureExtractor extractor : this.sourceExtractors) {
      features.addAll(extractor.extract(jCas, source));
    }
    for (SimpleFeatureExtractor extractor : this.targetExtractors) {
      features.addAll(extractor.extract(jCas, target));
    }
    for (BetweenAnnotationsFeatureExtractor extractor : this.betweenExtractors) {
      features.addAll(extractor.extractBetween(jCas, source, target));
    }
    if (this.isTraining()) {
      if (links.containsKey(source) && links.get(source).containsKey(target)) {
        TemporalLink link = null;
        Map<TARGET, TemporalLink> targetLinks = links.get(source);
        if (targetLinks != null) {
          link = targetLinks.remove(target);
          if (targetLinks.isEmpty()) {
            links.remove(source);
          }
        }
        if (link != null) {
          String relation = link.getRelationType();
          if (this.trainingRelationTypes.contains(relation)) {
            this.dataWriter.write(new Instance<String>(relation, features));
          }
        }
      }
    } else {
      String relation = this.classifier.classify(features);
      int offset = jCas.getDocumentText().length();
      TemporalLink tlink = new TemporalLink(jCas, offset, offset);
      tlink.setSource(source);
      tlink.setTarget(target);
      tlink.setRelationType(relation);
      tlink.addToIndexes();
    }
  }

  protected void logSkippedLinks(JCas jCas, Map<SOURCE, Map<TARGET, TemporalLink>> links) {
    if (!links.isEmpty()) {
      int count = 0;
      for (Map<TARGET, TemporalLink> targetLinks : links.values()) {
        count += targetLinks.size();
      }
      Map<Anchor, Sentence> sentences = new HashMap<Anchor, Sentence>();
      Map<Sentence, Integer> sentenceIndexes = new HashMap<Sentence, Integer>();
      int index = 0;
      for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
        sentenceIndexes.put(sentence, index);
        index += 1;
        for (SOURCE source : JCasUtil.selectCovered(jCas, this.sourceClass, sentence)) {
          sentences.put(source, sentence);
        }
        for (TARGET target : JCasUtil.selectCovered(jCas, this.targetClass, sentence)) {
          sentences.put(target, sentence);
        }
      }
      StringBuilder errorBuilder = new StringBuilder();
      errorBuilder.append("Missed ").append(count).append(" TLINK(s)\n");
      List<SOURCE> sources = new ArrayList<SOURCE>(links.keySet());
      Collections.sort(sources, new Comparator<SOURCE>() {
        @Override
        public int compare(SOURCE source1, SOURCE source2) {
          return source1.getBegin() - source2.getBegin();
        }
      });
      for (SOURCE source : sources) {
        for (TARGET target : links.get(source).keySet()) {
          TemporalLink link = links.get(source).get(target);
          Sentence sent1 = sentences.get(source);
          Sentence sent2 = sentences.get(target);
          errorBuilder.append(String.format(
              "%s(%s, %s)\n%d: %s\n%d: %s\n",
              link.getRelationType(),
              source.getCoveredText(),
              target.getCoveredText(),
              sentenceIndexes.get(sent1),
              sent1.getCoveredText(),
              sentenceIndexes.get(sent2),
              sent2.getCoveredText()));
        }
      }
      this.getContext().getLogger().log(Level.FINE, errorBuilder.toString());
    }
  }

  protected static String noLeavesPath(TreebankNodePath path) {
    if (path.getCommonAncestor() == null) {
      return null;
    }
    List<String> sourceTypes = new ArrayList<String>();
    for (TreebankNode node : path.getSourceToAncestorPath()) {
      if (!node.getLeaf()) {
        sourceTypes.add(node.getNodeType());
      }
    }
    List<String> targetTypes = new ArrayList<String>();
    for (TreebankNode node : path.getTargetToAncestorPath()) {
      if (!node.getLeaf()) {
        targetTypes.add(node.getNodeType());
      }
    }
    Collections.reverse(targetTypes);
    StringBuilder builder = new StringBuilder();
    for (String type : sourceTypes) {
      builder.append(type).append('>');
    }
    builder.append(path.getCommonAncestor().getNodeType());
    for (String type : targetTypes) {
      builder.append('<').append(type);
    }
    return builder.toString();
  }
}
