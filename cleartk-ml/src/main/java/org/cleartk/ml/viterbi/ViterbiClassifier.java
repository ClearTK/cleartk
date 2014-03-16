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
package org.cleartk.ml.viterbi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.Classifier;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.SequenceClassifier;
import org.cleartk.util.CleartkInitializationException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.ReflectionUtil.TypeArgumentDelegator;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.Initializable;

import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class ViterbiClassifier<OUTCOME_TYPE> implements SequenceClassifier<OUTCOME_TYPE>,
    Initializable, TypeArgumentDelegator {

  protected Classifier<OUTCOME_TYPE> delegatedClassifier;

  protected OutcomeFeatureExtractor[] outcomeFeatureExtractors;

  public static final String PARAM_STACK_SIZE = "stackSize";

  @ConfigurationParameter(
      name = PARAM_STACK_SIZE,
      description = "specifies the maximum number of candidate paths to "
          + "keep track of. In general, this number should be higher than the number "
          + "of possible classifications at any given point in the sequence. This "
          + "guarantees that highest-possible scoring sequence will be returned. If, "
          + "however, the number of possible classifications is quite high and/or you "
          + "are concerned about throughput performance, then you may want to reduce the number "
          + "of candidate paths to maintain.  If Classifier.score is not implemented for the given delegated classifier, then "
          + "the value of this parameter must be 1. ",
      defaultValue = "1")
  protected int stackSize;

  public static final String PARAM_ADD_SCORES = "addScores";

  @ConfigurationParameter(
      name = PARAM_ADD_SCORES,
      description = "specifies whether the scores of candidate sequence classifications should be "
          + "calculated by summing classfication scores for each member of the sequence or by multiplying them. A value of "
          + "true means that the scores will be summed. A value of false means that the scores will be multiplied. ",
      defaultValue = "false")
  protected boolean addScores = false;

  public ViterbiClassifier(
      Classifier<OUTCOME_TYPE> delegatedClassifier,
      OutcomeFeatureExtractor[] outcomeFeatureExtractors) {
    this.delegatedClassifier = delegatedClassifier;
    this.outcomeFeatureExtractors = outcomeFeatureExtractors;
  }

  public void initialize(UimaContext context) throws ResourceInitializationException {
    ConfigurationParameterInitializer.initialize(this, context);
    if (stackSize < 1) {
      throw CleartkInitializationException.parameterLessThan(PARAM_STACK_SIZE, 1, stackSize);
    }
  }

  public List<OUTCOME_TYPE> classify(List<List<Feature>> features)
      throws CleartkProcessingException {
    if (stackSize == 1) {
      List<Object> outcomes = new ArrayList<Object>();
      List<OUTCOME_TYPE> returnValues = new ArrayList<OUTCOME_TYPE>();
      for (List<Feature> instanceFeatures : features) {
        for (OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
          instanceFeatures.addAll(outcomeFeatureExtractor.extractFeatures(outcomes));
        }
        OUTCOME_TYPE outcome = delegatedClassifier.classify(instanceFeatures);
        outcomes.add(outcome);
        returnValues.add(outcome);
      }
      return returnValues;
    } else {
      try {
        return viterbi(features);
      } catch (UnsupportedOperationException uoe) {
        throw CleartkProcessingException.unsupportedOperationSetParameter(
            delegatedClassifier,
            "score",
            PARAM_STACK_SIZE,
            1);
      }
    }

  }

  /**
   * This implementation of Viterbi requires at most stackSize * sequenceLength calls to the
   * classifier. If this proves to be too expensive, then consider using a smaller stack size.
   * 
   * @param featureLists
   *          a sequence-worth of features. Each List<Feature> in features should corresond to all
   *          of the features for a given element in a sequence to be classified.
   * @return a list of outcomes (classifications) - one classification for each member of the
   *         sequence.
   * @see #PARAM_STACK_SIZE
   * @see OutcomeFeatureExtractor
   */
  public List<OUTCOME_TYPE> viterbi(List<List<Feature>> featureLists)
      throws CleartkProcessingException {

    if (featureLists == null || featureLists.size() == 0) {
      return Collections.emptyList();
    }

    // find the best paths through the outcome lattice
    Collection<Path> paths = null;
    for (List<Feature> features : featureLists) {

      // if this is the first instance, start new paths for each outcome
      if (paths == null) {
        paths = Lists.newArrayList();
        Map<OUTCOME_TYPE, Double> scoredOutcomes = this.getScoredOutcomes(features, null);
        for (OUTCOME_TYPE outcome : this.getTopOutcomes(scoredOutcomes)) {
          paths.add(new Path(outcome, scoredOutcomes.get(outcome), null));
        }
      }

      // for later instances, find the best previous path for each outcome
      else {
        Map<OUTCOME_TYPE, Path> maxPaths = Maps.newHashMap();
        for (Path path : paths) {
          Map<OUTCOME_TYPE, Double> scoredOutcomes = this.getScoredOutcomes(features, path);
          for (OUTCOME_TYPE outcome : this.getTopOutcomes(scoredOutcomes)) {
            double outcomeScore = scoredOutcomes.get(outcome);
            double score = this.addScores ? path.score + outcomeScore : path.score * outcomeScore;
            Path maxPath = maxPaths.get(outcome);
            if (maxPath == null || score > maxPath.score) {
              maxPaths.put(outcome, new Path(outcome, score, path));
            }
          }
        }
        paths = maxPaths.values();
      }
    }

    // take the maximum of the final paths
    return Collections.max(paths).outcomes;
  }

  @Override
  public List<Map<OUTCOME_TYPE, Double>> score(List<List<Feature>> features)
      throws CleartkProcessingException {
    throw new UnsupportedOperationException();
  }

  public Map<String, Type> getTypeArguments(Class<?> genericType) {
    if (genericType.equals(SequenceClassifier.class)) {
      genericType = Classifier.class;
    }
    return ReflectionUtil.getTypeArguments(genericType, this.delegatedClassifier);
  }

  private Map<OUTCOME_TYPE, Double> getScoredOutcomes(List<Feature> features, Path path)
      throws CleartkProcessingException {

    // add the features from preceding outcomes
    features = Lists.newArrayList(features);
    if (path != null) {
      List<Object> previousOutcomes = new ArrayList<Object>(path.outcomes);
      for (OutcomeFeatureExtractor outcomeFeatureExtractor : this.outcomeFeatureExtractors) {
        features.addAll(outcomeFeatureExtractor.extractFeatures(previousOutcomes));
      }
    }

    // get the scored outcomes for this instance
    Map<OUTCOME_TYPE, Double> scoredOutcomes = this.delegatedClassifier.score(features);
    if (scoredOutcomes.isEmpty()) {
      throw new IllegalStateException("expected at least one scored outcome, found "
          + scoredOutcomes);
    }
    return scoredOutcomes;
  }

  private List<OUTCOME_TYPE> getTopOutcomes(Map<OUTCOME_TYPE, Double> scoredOutcomes) {
    // get just the outcomes that fit within the stack
    Ordering<OUTCOME_TYPE> ordering = Ordering.natural().onResultOf(
        Functions.forMap(scoredOutcomes));
    return ordering.greatestOf(scoredOutcomes.keySet(), this.stackSize);
  }

  private class Path implements Comparable<Path> {
    public OUTCOME_TYPE outcome;

    public double score;

    public Path parent;

    public List<OUTCOME_TYPE> outcomes;

    public Path(OUTCOME_TYPE outcome, double score, Path parent) {
      this.outcome = outcome;
      this.score = score;
      this.parent = parent;
      this.outcomes = Lists.newArrayList();
      if (this.parent != null) {
        this.outcomes.addAll(this.parent.outcomes);
      }
      this.outcomes.add(this.outcome);
    }

    @Override
    public String toString() {
      ToStringHelper helper = Objects.toStringHelper(this);
      helper.add("outcome", this.outcome);
      helper.add("score", this.score);
      helper.add("parent", this.parent);
      return helper.toString();
    }

    @Override
    public int compareTo(Path that) {
      return Doubles.compare(this.score, that.score);
    }
  }
}
