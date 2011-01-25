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
package org.cleartk.classifier.viterbi;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.uima.UimaContext;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.SequentialClassifier;
import org.cleartk.classifier.feature.extractor.outcome.OutcomeFeatureExtractor;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.ReflectionUtil.TypeArgumentDelegator;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class ViterbiClassifier<OUTCOME_TYPE> implements SequentialClassifier<OUTCOME_TYPE>,
    Initializable, TypeArgumentDelegator {

  protected Classifier<OUTCOME_TYPE> delegatedClassifier;

  protected OutcomeFeatureExtractor[] outcomeFeatureExtractors;

  public static final String PARAM_STACK_SIZE = ConfigurationParameterFactory
      .createConfigurationParameterName(ViterbiClassifier.class, "stackSize");

  @ConfigurationParameter(description = "specifies the maximum number of candidate paths to "
      + "keep track of. In general, this number should be higher than the number "
      + "of possible classifications at any given point in the sequence. This "
      + "guarantees that highest-possible scoring sequence will be returned. If, "
      + "however, the number of possible classifications is quite high and/or you "
      + "are concerned about throughput performance, then you may want to reduce the number "
      + "of candidate paths to maintain.  If Classifier.score is not implemented for the given delegated classifier, then "
      + "the value of this parameter must be 1. ", defaultValue = "1")
  protected int stackSize;

  public static final String PARAM_ADD_SCORES = ConfigurationParameterFactory
      .createConfigurationParameterName(ViterbiClassifier.class, "addScores");

  @ConfigurationParameter(description = "specifies whether the scores of candidate sequence classifications should be "
      + "calculated by summing classfication scores for each member of the sequence or by multiplying them. A value of "
      + "true means that the scores will be summed. A value of false means that the scores will be multiplied. ", defaultValue = "false")
  protected boolean addScores = false;

  public ViterbiClassifier(JarFile modelFile) throws IOException {
    File modelFileDirectory = new File(modelFile.getName()).getParentFile();
    modelFile
        .getInputStream(modelFile.getEntry(ViterbiClassifierBuilder.DELEGATED_MODEL_FILE_NAME));
    FileUtil.extractFilesWithExtFromJar(modelFile, ".jar", modelFileDirectory);

    File delegatedModelFile = new File(
        modelFileDirectory,
        ViterbiClassifierBuilder.DELEGATED_MODEL_FILE_NAME);
    delegatedClassifier = ReflectionUtil.uncheckedCast(JarClassifierFactory
        .createClassifierFromJar(delegatedModelFile.getPath(), Classifier.class));

    ZipEntry zipEntry = modelFile.getEntry(ViterbiDataWriter.OUTCOME_FEATURE_EXTRACTOR_FILE_NAME);
    if (zipEntry == null) {
      outcomeFeatureExtractors = new OutcomeFeatureExtractor[0];
    } else {
      ObjectInputStream is = new ObjectInputStream(modelFile.getInputStream(zipEntry));

      try {
        outcomeFeatureExtractors = (OutcomeFeatureExtractor[]) is.readObject();
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public ViterbiClassifier() {
    // you generally do not want call this constructor.
  }

  public void initialize(UimaContext context) throws ResourceInitializationException {
    ConfigurationParameterInitializer.initialize(this, context);
    if (stackSize < 1) {
      throw new ResourceInitializationException(new IllegalArgumentException(String.format(
          "the parameter '%1$s' must be greater than 0.",
          PARAM_STACK_SIZE)));
    }
  }

  public List<OUTCOME_TYPE> classifySequence(List<List<Feature>> features) throws CleartkException {
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
        throw new IllegalArgumentException(
            "The configuration parameter "
                + PARAM_STACK_SIZE
                + " must be set to 1 if the delegated classifier does not implement the score method.  The classifier you are using is: "
                + delegatedClassifier.getClass().getName());
      }
    }

  }

  /**
   * This implementation of Viterbi requires at most stackSize * sequenceLength calls to the
   * classifier. If this proves to be too expensive, then consider using a smaller stack size.
   * 
   * @param features
   *          a sequence-worth of features. Each List<Feature> in features should corresond to all
   *          of the features for a given element in a sequence to be classified.
   * @return a list of outcomes (classifications) - one classification for each member of the
   *         sequence.
   * @see #PARAM_STACK_SIZE
   * @see OutcomeFeatureExtractor
   */
  public List<OUTCOME_TYPE> viterbi(List<List<Feature>> features) throws CleartkException {

    List<ScoredOutcome<List<OUTCOME_TYPE>>> nbestSequences = new ArrayList<ScoredOutcome<List<OUTCOME_TYPE>>>();

    if (features == null || features.size() == 0) {
      return Collections.emptyList();
    }

    List<ScoredOutcome<OUTCOME_TYPE>> scoredOutcomes = delegatedClassifier.score(
        features.get(0),
        stackSize);
    for (ScoredOutcome<OUTCOME_TYPE> scoredOutcome : scoredOutcomes) {
      double score = scoredOutcome.getScore();
      List<OUTCOME_TYPE> sequence = new ArrayList<OUTCOME_TYPE>();
      sequence.add(scoredOutcome.getOutcome());
      nbestSequences.add(new ScoredOutcome<List<OUTCOME_TYPE>>(sequence, score));
    }

    Map<OUTCOME_TYPE, Double> l = new HashMap<OUTCOME_TYPE, Double>();
    Map<OUTCOME_TYPE, List<OUTCOME_TYPE>> m = new HashMap<OUTCOME_TYPE, List<OUTCOME_TYPE>>();

    for (int i = 1; i < features.size(); i++) {

      List<Feature> instanceFeatures = features.get(i);
      l.clear();
      m.clear();
      for (ScoredOutcome<List<OUTCOME_TYPE>> scoredSequence : nbestSequences) {
        // add features from previous outcomes from each scoredSequence
        // in returnValues
        int outcomeFeaturesCount = 0;
        List<Object> previousOutcomes = new ArrayList<Object>(scoredSequence.getOutcome());
        for (OutcomeFeatureExtractor outcomeFeatureExtractor : outcomeFeatureExtractors) {
          List<Feature> outcomeFeatures = outcomeFeatureExtractor.extractFeatures(previousOutcomes);
          instanceFeatures.addAll(outcomeFeatures);
          outcomeFeaturesCount += outcomeFeatures.size();
        }
        // score the instance features using the features added by the
        // outcomeFeatureExtractors
        scoredOutcomes = delegatedClassifier.score(instanceFeatures, stackSize);
        // remove the added features from previous outcomes for this
        // scoredSequence
        instanceFeatures = instanceFeatures.subList(0, instanceFeatures.size()
            - outcomeFeaturesCount);

        for (ScoredOutcome<OUTCOME_TYPE> scoredOutcome : scoredOutcomes) {

          if (!l.containsKey(scoredOutcome.getOutcome())) {
            double score = scoredSequence.getScore();
            if (addScores) {
              score = score + scoredOutcome.getScore();
            } else {
              score = score * scoredOutcome.getScore();
            }
            l.put(scoredOutcome.getOutcome(), score);
            m.put(
                scoredOutcome.getOutcome(),
                new ArrayList<OUTCOME_TYPE>(scoredSequence.getOutcome()));
          } else {
            double newScore = scoredSequence.getScore();
            if (addScores) {
              newScore = newScore + scoredOutcome.getScore();
            } else {
              newScore = newScore * scoredOutcome.getScore();
            }
            double bestScore = l.get(scoredOutcome.getOutcome());

            if (newScore > bestScore) {
              l.put(scoredOutcome.getOutcome(), newScore);
              m.put(
                  scoredOutcome.getOutcome(),
                  new ArrayList<OUTCOME_TYPE>(scoredSequence.getOutcome()));
            }
          }
        }
      }

      nbestSequences.clear();
      for (OUTCOME_TYPE outcome : l.keySet()) {
        List<OUTCOME_TYPE> outcomeSequence = m.get(outcome);
        outcomeSequence.add(outcome);
        double score = l.get(outcome);
        ScoredOutcome<List<OUTCOME_TYPE>> returnValue = new ScoredOutcome<List<OUTCOME_TYPE>>(
            outcomeSequence,
            score);
        nbestSequences.add(returnValue);
      }

      Collections.sort(nbestSequences);
    }

    Collections.sort(nbestSequences);
    if (nbestSequences.size() > 0) {
      return nbestSequences.get(0).getOutcome();
    }

    return null;
  }

  public List<ScoredOutcome<List<OUTCOME_TYPE>>> scoreSequence(
      List<List<Feature>> features,
      int maxResults) throws CleartkException {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<String, Type> getTypeArguments(Class<?> genericType) {
    if (genericType.equals(SequentialClassifier.class)) {
      genericType = Classifier.class;
    }
    return ReflectionUtil.getTypeArguments(genericType, this.delegatedClassifier);
  }

}
