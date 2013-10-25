/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.UimaContextFactory;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class ViterbiTest {

  /**
   * This test was created from scratch using a small chart I wrote down on paper and solved by hand
   * before implementing here.
   */
  @Test
  public void test1() throws Throwable {
    List<List<Feature>> features = new ArrayList<List<Feature>>();
    features.add(createFeatures("0"));
    features.add(createFeatures("1"));
    features.add(createFeatures("2"));
    features.add(createFeatures("3"));
    features.add(createFeatures("4"));

    UimaContext uimaContext = UimaContextFactory.createUimaContext(
        DefaultOutcomeFeatureExtractor.PARAM_MOST_RECENT_OUTCOME,
        1,
        DefaultOutcomeFeatureExtractor.PARAM_LEAST_RECENT_OUTCOME,
        1,
        DefaultOutcomeFeatureExtractor.PARAM_USE_BIGRAM,
        false,
        DefaultOutcomeFeatureExtractor.PARAM_USE_TRIGRAM,
        false,
        DefaultOutcomeFeatureExtractor.PARAM_USE4GRAM,
        false);
    DefaultOutcomeFeatureExtractor dofe = new DefaultOutcomeFeatureExtractor();
    dofe.initialize(uimaContext);

    TestViterbiClassifier tvc = new TestViterbiClassifier();
    tvc.setOutcomeFeatureExctractors(new OutcomeFeatureExtractor[] { dofe });
    tvc.setStackSize(4);
    tvc.setAddScores(true);
    tvc.setDelegatedClassifier(new TestClassifier());
    List<String> bestSequence = tvc.viterbi(features);

    assertEquals("A", bestSequence.get(0));
    assertEquals("E", bestSequence.get(1));
    assertEquals("G", bestSequence.get(2));
    assertEquals("K", bestSequence.get(3));
    assertEquals("O", bestSequence.get(4));

  }

  @Test
  public void test1b() throws Throwable {
    List<List<Feature>> features = new ArrayList<List<Feature>>();
    features.add(createFeatures("0"));
    features.add(createFeatures("1"));
    features.add(createFeatures("2"));
    features.add(createFeatures("3"));
    features.add(createFeatures("4"));

    UimaContext uimaContext = UimaContextFactory.createUimaContext(
        DefaultOutcomeFeatureExtractor.PARAM_MOST_RECENT_OUTCOME,
        1,
        DefaultOutcomeFeatureExtractor.PARAM_LEAST_RECENT_OUTCOME,
        1,
        DefaultOutcomeFeatureExtractor.PARAM_USE_BIGRAM,
        false,
        DefaultOutcomeFeatureExtractor.PARAM_USE_TRIGRAM,
        false,
        DefaultOutcomeFeatureExtractor.PARAM_USE4GRAM,
        false);
    DefaultOutcomeFeatureExtractor dofe = new DefaultOutcomeFeatureExtractor();
    dofe.initialize(uimaContext);

    TestViterbiClassifier tvc = new TestViterbiClassifier();
    tvc.setOutcomeFeatureExctractors(new OutcomeFeatureExtractor[] { dofe });
    tvc.setStackSize(4);
    tvc.setAddScores(false);
    tvc.setDelegatedClassifier(new TestClassifier());
    List<String> bestSequence = tvc.viterbi(features);

    assertEquals("C", bestSequence.get(0));
    assertEquals("E", bestSequence.get(1));
    assertEquals("G", bestSequence.get(2));
    assertEquals("K", bestSequence.get(3));
    assertEquals("O", bestSequence.get(4));

  }

  private List<Feature> createFeatures(String featureValue) {
    List<Feature> instanceFeatures = new ArrayList<Feature>();
    instanceFeatures.add(new Feature("position", featureValue));
    return instanceFeatures;

  }

  private class TestClassifier implements Classifier<String> {
    Map<String, Integer> previousLabelWeights;

    public TestClassifier() {
      previousLabelWeights = new HashMap<String, Integer>();
      previousLabelWeights.put("A", 15);
      previousLabelWeights.put("B", 14);
      previousLabelWeights.put("C", 13);
      previousLabelWeights.put("D", 12);
      previousLabelWeights.put("E", 12);
      previousLabelWeights.put("F", 10);
      previousLabelWeights.put("G", 10);
      previousLabelWeights.put("H", 8);
      previousLabelWeights.put("I", 7);
      previousLabelWeights.put("J", 6);
      previousLabelWeights.put("K", 6);
      previousLabelWeights.put("L", 4);
    }

    @Override
    public String classify(List<Feature> features) {
      return null;
    }

    @Override
    public Map<String, Double> score(List<Feature> features) throws CleartkProcessingException {
      Map<String, Double> scores = Maps.newHashMap();

      String position = (String) features.get(0).getValue();
      if (features.size() == 1) {
        scores.put("A", 1.0);
        scores.put("B", 1.0);
        scores.put("C", 2.0);
        return scores;
      }

      String previousLabel = (String) features.get(1).getValue();
      double previousLabelWeight = previousLabelWeights.get(previousLabel);

      if (position.equals("1")) {
        scores.put("D", 2 + previousLabelWeight);
        scores.put("E", 3 + previousLabelWeight);
        scores.put("F", 3 + previousLabelWeight);
        return scores;
      }

      if (position.equals("2")) {
        scores.put("G", 4 + previousLabelWeight);
        scores.put("H", 4 + previousLabelWeight);
        scores.put("I", 5 + previousLabelWeight);
        return scores;
      }

      if (position.equals("3")) {
        scores.put("J", 5 + previousLabelWeight);
        scores.put("K", 6 + previousLabelWeight);
        scores.put("L", 6 + previousLabelWeight);
        return scores;
      }

      if (position.equals("4")) {
        scores.put("M", 7 + previousLabelWeight);
        scores.put("N", 7 + previousLabelWeight);
        scores.put("O", 8 + previousLabelWeight);
        return scores;
      }

      Assert.fail("Invalid position");
      return scores;
    }

  }

  /**
   * This test is based on Figure 15.2 in "Introduction to Algorithms 2nd Edition". Except that here
   * I am finding the most expensive path through the factory.
   */
  @Test
  public void test2() throws Throwable {
    List<List<Feature>> features = new ArrayList<List<Feature>>();
    features.add(createFeatures("1"));
    features.add(createFeatures("2"));
    features.add(createFeatures("3"));
    features.add(createFeatures("4"));
    features.add(createFeatures("5"));
    features.add(createFeatures("6"));

    UimaContext uimaContext = UimaContextFactory.createUimaContext(
        DefaultOutcomeFeatureExtractor.PARAM_MOST_RECENT_OUTCOME,
        1,
        DefaultOutcomeFeatureExtractor.PARAM_LEAST_RECENT_OUTCOME,
        1,
        DefaultOutcomeFeatureExtractor.PARAM_USE_BIGRAM,
        false,
        DefaultOutcomeFeatureExtractor.PARAM_USE_TRIGRAM,
        false,
        DefaultOutcomeFeatureExtractor.PARAM_USE4GRAM,
        false);
    DefaultOutcomeFeatureExtractor dofe = new DefaultOutcomeFeatureExtractor();
    dofe.initialize(uimaContext);

    TestViterbiClassifier tvc = new TestViterbiClassifier();
    tvc.setOutcomeFeatureExctractors(new OutcomeFeatureExtractor[] { dofe });
    tvc.setStackSize(2);
    tvc.setAddScores(true);
    tvc.setDelegatedClassifier(new Test2Classifier());
    List<String> bestSequence = tvc.viterbi(features);

    assertEquals("2", bestSequence.get(0));
    assertEquals("1", bestSequence.get(1));
    assertEquals("2", bestSequence.get(2));
    assertEquals("1", bestSequence.get(3));
    assertEquals("1", bestSequence.get(4));
    assertEquals("2", bestSequence.get(5));

  }

  private class Test2Classifier implements Classifier<String> {

    public Test2Classifier() {
    }

    @Override
    public String classify(List<Feature> features) {
      return null;
    }

    @Override
    public Map<String, Double> score(List<Feature> features) throws CleartkProcessingException {
      Map<String, Double> scores = Maps.newHashMap();

      String position = (String) features.get(0).getValue();
      if (features.size() == 1) {
        // if I am at position 1 then the label "1" corresponds to the
        // 1st assembly line and the score is 2+7.
        scores.put("1", 9.0);
        scores.put("2", 12.0);
        return scores;
      }

      String previousLabel = (String) features.get(1).getValue();
      if (position.equals("2")) {
        if (previousLabel.equals("1")) {
          // if I am at position 2, and I was previously on the 1st
          // assembly then cost to stay in the 1st assembly is 9
          scores.put("1", 9.0);
          // if I am at position 2, and I was previously on the 2nd
          // assembly then cost to move to the 1st assembly is 5 + 2
          scores.put("2", 5.0 + 2.0);
        } else if (previousLabel.equals("2")) {
          scores.put("1", 9.0 + 2.0);
          scores.put("2", 5.0);
        }
      }

      if (position.equals("3")) {
        if (previousLabel.equals("1")) {
          scores.put("1", 3.0);
          scores.put("2", 6.0 + 3.0);
        } else if (previousLabel.equals("2")) {
          scores.put("1", 3.0 + 1.0);
          scores.put("2", 6.0);
        }
      }

      if (position.equals("4")) {
        if (previousLabel.equals("1")) {
          scores.put("1", 4.0);
          scores.put("2", 4.0 + 1.0);
        } else if (previousLabel.equals("2")) {
          scores.put("1", 4.0 + 2.0);
          scores.put("2", 4.0);
        }
      }

      if (position.equals("5")) {
        if (previousLabel.equals("1")) {
          scores.put("1", 8.0);
          scores.put("2", 5.0 + 3.0);
        } else if (previousLabel.equals("2")) {
          // this used to be 8.0 + 2.0, but that gives equal values at state 5
          // and so it was non-deterministic which branch was chosen by viterbi
          scores.put("1", 8.0 + 1.0);
          scores.put("2", 5.0);
        }
      }

      if (position.equals("6")) {
        if (previousLabel.equals("1")) {
          scores.put("1", 4.0 + 3.0);
          scores.put("2", 7.0 + 2.0 + 4.0);
        } else if (previousLabel.equals("2")) {
          scores.put("1", 4.0 + 3.0 + 1.0);
          scores.put("2", 7.0 + 2.0);
        }
      }

      return scores;
    }
  }

  public static class TestViterbiClassifier extends ViterbiClassifier<String> {

    public TestViterbiClassifier() {
      super(null, null);
    }

    public void setOutcomeFeatureExctractors(OutcomeFeatureExtractor[] ofes) {
      outcomeFeatureExtractors = ofes;
    }

    public void setStackSize(int stackSize) {
      this.stackSize = stackSize;
    }

    public void setAddScores(boolean addScores) {
      this.addScores = addScores;
    }

    public void setDelegatedClassifier(Classifier<String> classifier) {
      delegatedClassifier = classifier;
    }
  }
}
