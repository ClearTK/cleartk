/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.ml.feature.selection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.selection.MutualInformationFeatureSelectionExtractor.CombineScoreMethod.CombineScoreFunction;
import org.cleartk.ml.feature.selection.MutualInformationFeatureSelectionExtractor.MutualInformationStats.ComputeFeatureScore;
import org.cleartk.ml.feature.transform.TransformableFeature;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * Selects features via mutual information statistics between the features extracted from its
 * sub-extractor and the outcome values they are paired with in classification instances.
 * 
 * @author Lee Becker
 * 
 */
public class MutualInformationFeatureSelectionExtractor<OUTCOME_T extends Comparable<?>, FOCUS_T extends Annotation>
    extends FeatureSelectionExtractor<OUTCOME_T>implements FeatureExtractor1<FOCUS_T> {

  /**
   * Specifies how scores for each outcome should be combined/aggregated into a single score
   */
  public static enum CombineScoreMethod {
    AVERAGE, // Average mutual information across all classes and take features with k-largest
             // values
    MAX; // Take highest mutual information value for each class
    // MERGE, // Take k-largest mutual information values for each class and merge into a single
    // collection - currently omitted because it requires a different extraction flow

    public abstract static class CombineScoreFunction<OUTCOME_T>
        implements Function<Map<OUTCOME_T, Double>, Double> {
    }

    public static class AverageScores<OUTCOME_T> extends CombineScoreFunction<OUTCOME_T> {
      @Override
      public Double apply(Map<OUTCOME_T, Double> input) {
        Collection<Double> scores = input.values();
        int size = scores.size();
        double total = 0;

        for (Double score : scores) {
          total += score;
        }
        return total / size;
      }
    }

    public static class MaxScores<OUTCOME_T> extends CombineScoreFunction<OUTCOME_T> {
      @Override
      public Double apply(Map<OUTCOME_T, Double> input) {
        return Ordering.natural().max(input.values());
      }
    }
  }

  /**
   * Helper class for aggregating and computing mutual information statistics
   */
  public static class MutualInformationStats<OUTCOME_T extends Comparable<?>> {
    protected Multiset<OUTCOME_T> classCounts;

    protected Table<String, OUTCOME_T, Integer> classConditionalCounts;

    protected double smoothingCount;

    public MutualInformationStats(double smoothingCount) {
      this.classCounts = HashMultiset.<OUTCOME_T> create();
      this.classConditionalCounts = TreeBasedTable.<String, OUTCOME_T, Integer> create();
      this.smoothingCount += smoothingCount;
    }

    public void update(String featureName, OUTCOME_T outcome, int occurrences) {
      Integer count = this.classConditionalCounts.get(featureName, outcome);
      if (count == null) {
        count = 0;
      }
      this.classConditionalCounts.put(featureName, outcome, count + occurrences);
      this.classCounts.add(outcome, occurrences);
    }

    public double mutualInformation(String featureName, OUTCOME_T outcome) {
      // notation index of 0 means false, 1 mean true
      int[] featureCounts = new int[2];
      int[] outcomeCounts = new int[2];
      int[][] featureOutcomeCounts = new int[2][2];

      int n = this.classCounts.size();
      featureCounts[1] = this.sum(this.classConditionalCounts.row(featureName).values());
      featureCounts[0] = n - featureCounts[1];
      outcomeCounts[1] = this.classCounts.count(outcome);
      outcomeCounts[0] = n - outcomeCounts[1];

      featureOutcomeCounts[1][1] = this.classConditionalCounts.contains(featureName, outcome)
          ? this.classConditionalCounts.get(featureName, outcome)
          : 0;
      featureOutcomeCounts[1][0] = featureCounts[1] - featureOutcomeCounts[1][1];
      featureOutcomeCounts[0][1] = outcomeCounts[1] - featureOutcomeCounts[1][1];
      featureOutcomeCounts[0][0] = n - featureCounts[1] - outcomeCounts[1]
          + featureOutcomeCounts[1][1];

      double information = 0.0;
      for (int nFeature = 0; nFeature <= 1; nFeature++) {
        for (int nOutcome = 0; nOutcome <= 1; nOutcome++) {
          featureOutcomeCounts[nFeature][nOutcome] += smoothingCount;
          information += (double) featureOutcomeCounts[nFeature][nOutcome] / (double) n
              * Math.log(
                  ((double) n * featureOutcomeCounts[nFeature][nOutcome])
                      / ((double) featureCounts[nFeature] * outcomeCounts[nOutcome]));
        }
      }

      return information;
    }

    private int sum(Collection<Integer> values) {
      int total = 0;
      for (int v : values) {
        total += v;
      }
      return total;
    }

    public void save(URI outputURI) throws IOException {
      File out = new File(outputURI);
      BufferedWriter writer = null;
      writer = new BufferedWriter(new FileWriter(out));

      // Write out header
      writer.append("Mutual Information Data\n");
      writer.append("Feature\t");
      writer.append(Joiner.on("\t").join(this.classConditionalCounts.columnKeySet()));
      writer.append("\n");

      // Write out Mutual Information data
      for (String featureName : this.classConditionalCounts.rowKeySet()) {
        writer.append(featureName);
        for (OUTCOME_T outcome : this.classConditionalCounts.columnKeySet()) {
          writer.append("\t");
          writer.append(
              String.format(Locale.ROOT, "%f", this.mutualInformation(featureName, outcome)));
        }
        writer.append("\n");
      }
      writer.append("\n");
      writer.append(this.classConditionalCounts.toString());
      writer.close();
    }

    public ComputeFeatureScore<OUTCOME_T> getScoreFunction(CombineScoreMethod combineScoreMethod) {
      return new ComputeFeatureScore<OUTCOME_T>(this, combineScoreMethod);
    }

    public static class ComputeFeatureScore<OUTCOME_T extends Comparable<?>>
        implements Function<String, Double> {

      private MutualInformationStats<OUTCOME_T> stats;

      private CombineScoreFunction<OUTCOME_T> combineScoreFunction;

      public ComputeFeatureScore(
          MutualInformationStats<OUTCOME_T> stats,
          CombineScoreMethod combineMeasureType) {
        this.stats = stats;
        switch (combineMeasureType) {
          case AVERAGE:
            this.combineScoreFunction = new CombineScoreMethod.AverageScores<OUTCOME_T>();
          case MAX:
            this.combineScoreFunction = new CombineScoreMethod.MaxScores<OUTCOME_T>();
        }

      }

      @Override
      public Double apply(String featureName) {
        Set<OUTCOME_T> outcomes = stats.classConditionalCounts.columnKeySet();
        Map<OUTCOME_T, Double> featureOutcomeMI = Maps.newHashMap();
        for (OUTCOME_T outcome : outcomes) {
          featureOutcomeMI.put(outcome, stats.mutualInformation(featureName, outcome));
        }
        return this.combineScoreFunction.apply(featureOutcomeMI);
      }

    }

  }

  public String nameFeature(Feature feature) {
    return (feature.getValue() instanceof Number)
        ? feature.getName()
        : feature.getName() + ":" + feature.getValue();
  }

  protected boolean isTrained;

  private MutualInformationStats<OUTCOME_T> mutualInfoStats;

  private FeatureExtractor1<FOCUS_T> subExtractor;

  private int numFeatures;

  private CombineScoreMethod combineScoreMethod;

  private List<String> selectedFeatures;

  private double smoothingCount;

  public MutualInformationFeatureSelectionExtractor(
      String name,
      FeatureExtractor1<FOCUS_T> extractor) {
    super(name);
    this.init(extractor, CombineScoreMethod.MAX, 1.0, 10);
  }

  public MutualInformationFeatureSelectionExtractor(
      String name,
      FeatureExtractor1<FOCUS_T> extractor,
      int numFeatures) {
    super(name);
    this.init(extractor, CombineScoreMethod.MAX, 1.0, numFeatures);
  }

  public MutualInformationFeatureSelectionExtractor(
      String name,
      FeatureExtractor1<FOCUS_T> extractor,
      CombineScoreMethod combineMeasureType,
      double smoothingCount,
      int numFeatures) {
    super(name);
    this.init(extractor, combineMeasureType, smoothingCount, numFeatures);
  }

  private void init(
      FeatureExtractor1<FOCUS_T> extractor,
      CombineScoreMethod method,
      double smoothCount,
      int n) {
    this.subExtractor = extractor;
    this.combineScoreMethod = method;
    this.smoothingCount = smoothCount;
    this.numFeatures = n;
  }

  @Override
  public List<Feature> extract(JCas view, FOCUS_T focusAnnotation)
      throws CleartkExtractorException {

    List<Feature> extracted = this.subExtractor.extract(view, focusAnnotation);
    List<Feature> result = new ArrayList<Feature>();
    if (this.isTrained) {
      // Filter out selected features
      result.addAll(Collections2.filter(extracted, this));
    } else {
      // We haven't trained this extractor yet, so just mark the existing features
      // for future modification, by creating one uber-container feature
      result.add(new TransformableFeature(this.name, extracted));
    }

    return result;
  }

  @Override
  public void train(Iterable<Instance<OUTCOME_T>> instances) {
    // aggregate statistics for all features and classes
    this.mutualInfoStats = new MutualInformationStats<OUTCOME_T>(this.smoothingCount);

    for (Instance<OUTCOME_T> instance : instances) {
      OUTCOME_T outcome = instance.getOutcome();
      for (Feature feature : instance.getFeatures()) {
        if (this.isTransformable(feature)) {
          for (Feature untransformedFeature : ((TransformableFeature) feature).getFeatures()) {
            mutualInfoStats.update(this.nameFeature(untransformedFeature), outcome, 1);
          }
        }
      }
    }
    // Compute mutual information score for each feature
    Set<String> featureNames = mutualInfoStats.classConditionalCounts.rowKeySet();

    this.selectedFeatures = Ordering.natural().onResultOf(
        this.mutualInfoStats.getScoreFunction(
            this.combineScoreMethod)).reverse().immutableSortedCopy(featureNames);
    this.isTrained = true;
  }

  @Override
  public void save(URI uri) throws IOException {
    if (!this.isTrained) {
      throw new IOException("MutualInformationFeatureExtractor: Cannot save before training.");
    }
    File out = new File(uri);
    BufferedWriter writer = new BufferedWriter(new FileWriter(out));
    writer.append("CombineScoreType\t");
    writer.append(this.combineScoreMethod.toString());
    writer.append("\n");

    ComputeFeatureScore<OUTCOME_T> computeScore = this.mutualInfoStats.getScoreFunction(
        this.combineScoreMethod);
    for (String feature : this.selectedFeatures) {
      writer.append(String.format(Locale.ROOT, "%s\t%f\n", feature, computeScore.apply(feature)));
    }

    writer.close();

  }

  @Override
  public void load(URI uri) throws IOException {
    this.selectedFeatures = Lists.newArrayList();
    File in = new File(uri);
    BufferedReader reader = new BufferedReader(new FileReader(in));

    // First line specifies the combine utility type
    this.combineScoreMethod = CombineScoreMethod.valueOf(reader.readLine().split("\\t")[1]);

    // The rest of the lines are feature + selection scores
    String line = null;
    int n = 0;
    while ((line = reader.readLine()) != null && n < this.numFeatures) {
      String[] featureValuePair = line.split("\\t");
      this.selectedFeatures.add(featureValuePair[0]);
      n++;
    }

    reader.close();
    this.isTrained = true;
  }

  @Override
  public boolean apply(Feature feature) {
    return this.selectedFeatures.contains(this.nameFeature(feature));
  }

  public final List<String> getSelectedFeatures() {
    return this.selectedFeatures;
  }

}
