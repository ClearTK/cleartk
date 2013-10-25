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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;
import org.cleartk.classifier.feature.extractor.FeatureExtractor2;
import org.cleartk.classifier.feature.extractor.NamingExtractor1;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil.TreebankNodePath;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.token.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class TemporalLinkAnnotator_ImplBase<SOURCE extends Anchor, TARGET extends Anchor>
    extends CleartkAnnotator<String> {

  public static Map<String, String> REVERSE_RELATION = new HashMap<String, String>();
  static {
    // TimeML
    REVERSE_RELATION.put("BEFORE", "AFTER");
    REVERSE_RELATION.put("AFTER", "BEFORE");
    REVERSE_RELATION.put("INCLUDES", "IS_INCLUDED");
    REVERSE_RELATION.put("IS_INCLUDED", "INCLUDES");
    REVERSE_RELATION.put("DURING", "DURING_INV");
    REVERSE_RELATION.put("DURING_INV", "DURING");
    REVERSE_RELATION.put("SIMULTANEOUS", "SIMULTANEOUS");
    REVERSE_RELATION.put("IAFTER", "IBEFORE");
    REVERSE_RELATION.put("IBEFORE", "IAFTER");
    REVERSE_RELATION.put("IDENTITY", "IDENTITY");
    REVERSE_RELATION.put("BEGINS", "BEGUN_BY");
    REVERSE_RELATION.put("ENDS", "ENDED_BY");
    REVERSE_RELATION.put("BEGUN_BY", "BEGINS");
    REVERSE_RELATION.put("ENDED_BY", "ENDS");
    // TempEval
    REVERSE_RELATION.put("OVERLAP", "OVERLAP");
    REVERSE_RELATION.put("OVERLAP-OR-AFTER", "BEFORE-OR-OVERLAP");
    REVERSE_RELATION.put("BEFORE-OR-OVERLAP", "OVERLAP-OR-AFTER");
    REVERSE_RELATION.put("VAGUE", "VAGUE");
    REVERSE_RELATION.put("UNKNOWN", "UNKNOWN");
    REVERSE_RELATION.put("NONE", "NONE");
  }

  private Class<SOURCE> sourceClass;

  private Class<TARGET> targetClass;

  private Set<String> trainingRelationTypes;
  
  private static final String NO_RELATION = "-NO-RELATION-";

  protected List<FeatureExtractor1<SOURCE>> sourceExtractors;

  protected List<FeatureExtractor1<TARGET>> targetExtractors;

  protected List<FeatureExtractor2<Anchor,Anchor>> betweenExtractors;

  protected class SourceTargetPair {

    public SOURCE source;

    public TARGET target;

    public SourceTargetPair(SOURCE source, TARGET target) {
      this.source = source;
      this.target = target;
    }
  }

  public TemporalLinkAnnotator_ImplBase(
      Class<SOURCE> sourceClass,
      Class<TARGET> targetClass,
      String... trainingRelationTypes) {
    this.sourceClass = sourceClass;
    this.targetClass = targetClass;
    this.trainingRelationTypes = new HashSet<String>(Arrays.asList(trainingRelationTypes));
    this.sourceExtractors = Lists.newArrayList();
    this.targetExtractors = Lists.newArrayList();
    this.betweenExtractors = Lists.newArrayList();
  }

  protected void setSourceExtractors(List<FeatureExtractor1<SOURCE>> extractors) {
    this.sourceExtractors = new ArrayList<FeatureExtractor1<SOURCE>>();
    for (FeatureExtractor1<SOURCE> extractor : extractors) {
      this.sourceExtractors.add(new NamingExtractor1<SOURCE>("Source", extractor));
    }
  }

  protected void setTargetExtractors(List<FeatureExtractor1<TARGET>> extractors) {
    this.targetExtractors = new ArrayList<FeatureExtractor1<TARGET>>();
    for (FeatureExtractor1<TARGET> extractor : extractors) {
      this.targetExtractors.add(new NamingExtractor1<TARGET>("Target", extractor));
    }
  }

  protected void setBetweenExtractors(List<FeatureExtractor2<Anchor, Anchor>> extractors) {
    this.betweenExtractors = extractors;
  }

  /**
   * Returns the (source, target) anchor pairs for which a relation should be classified.
   */
  protected abstract List<SourceTargetPair> getSourceTargetPairs(JCas jCas);

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    // collect all annotated relations
    Table<SOURCE, TARGET, String> links = HashBasedTable.create();
    for (TemporalLink tlink : JCasUtil.select(jCas, TemporalLink.class)) {
      Anchor sourceAnchor = tlink.getSource();
      Anchor targetAnchor = tlink.getTarget();

      // collect the relation from source to target
      if (this.sourceClass.isInstance(sourceAnchor) && this.targetClass.isInstance(targetAnchor)) {
        SOURCE source = this.sourceClass.cast(sourceAnchor);
        TARGET target = this.targetClass.cast(targetAnchor);
        String relation = tlink.getRelationType();
        links.put(source, target, relation);
      }

      // collect the (reversed) relation from target to source
      if (this.sourceClass.isInstance(targetAnchor) && this.targetClass.isInstance(sourceAnchor)) {
        SOURCE source = this.sourceClass.cast(targetAnchor);
        TARGET target = this.targetClass.cast(sourceAnchor);
        String relation = REVERSE_RELATION.get(tlink.getRelationType());
        if (relation == null) {
          throw new UnsupportedOperationException("Unknown relation: " + tlink.getRelationType());
        }
        links.put(source, target, relation);
      }
    }

    // for each pair of anchors, write training data or classify the relation
    for (SourceTargetPair pair : this.getSourceTargetPairs(jCas)) {
      SOURCE source = pair.source;
      TARGET target = pair.target;

      // extract features
      List<Feature> features = new ArrayList<Feature>();
      for (FeatureExtractor1<SOURCE> extractor : this.sourceExtractors) {
        features.addAll(extractor.extract(jCas, source));
      }
      for (FeatureExtractor1<TARGET> extractor : this.targetExtractors) {
        features.addAll(extractor.extract(jCas, target));
      }
      for (FeatureExtractor2<Anchor, Anchor> extractor : this.betweenExtractors) {
        features.addAll(extractor.extract(jCas, source, target));
      }

      // during training, write an instance if this pair was labeled
      if (this.isTraining()) {
        String relation = links.remove(source, target);
        if (relation != null) {
          if (!this.trainingRelationTypes.isEmpty() && !this.trainingRelationTypes.contains(relation)) {
            relation = NO_RELATION;
          }
          this.dataWriter.write(new Instance<String>(relation, features));
        }
      } else {
        String relation = this.classifier.classify(features);
        if (!NO_RELATION.equals(relation)) {
          int offset = jCas.getDocumentText().length();
          TemporalLink tlink = new TemporalLink(jCas, offset, offset);
          tlink.setSource(source);
          tlink.setTarget(target);
          tlink.setRelationType(relation);
          tlink.addToIndexes();
        }
      }
    }

    // log a message for any links that were annotated but not used
    if (!links.isEmpty()) {
      
      // map anchors to the sentences that contain them
      Map<Anchor, Sentence> sentences = new HashMap<Anchor, Sentence>();
      for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
        for (SOURCE source : JCasUtil.selectCovered(jCas, this.sourceClass, sentence)) {
          sentences.put(source, sentence);
        }
        for (TARGET target : JCasUtil.selectCovered(jCas, this.targetClass, sentence)) {
          sentences.put(target, sentence);
        }
      }

      // sort relations by the location of their source
      List<Cell<SOURCE, TARGET, String>> cells = Lists.newArrayList(links.cellSet());
      Ordering<Cell<SOURCE, TARGET, String>> order = Ordering.natural().onResultOf(
          new Function<Cell<SOURCE, TARGET, String>, Integer>() {
            @Override
            public Integer apply(Cell<SOURCE, TARGET, String> cell) {
              return cell.getRowKey().getBegin();
            }
          });
      Collections.sort(cells, order);

      // assemble an error message
      StringBuilder errorBuilder = new StringBuilder();
      errorBuilder.append("Missed ").append(links.size()).append(" TLINK(s)\n");
      for (Cell<SOURCE, TARGET, String> cell : cells) {
        SOURCE source = cell.getRowKey();
        TARGET target = cell.getColumnKey();
        String relation = cell.getValue();
        Sentence sent1 = sentences.get(source);
        Sentence sent2 = sentences.get(target);
        errorBuilder.append(String.format(
            "%s(%s, %s)\n%s\n%s\n",
            relation,
            source.getCoveredText(),
            target.getCoveredText(),
            sent1 == null ? null : sent1.getCoveredText(),
            sent2 == null ? null : sent2.getCoveredText()));
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
