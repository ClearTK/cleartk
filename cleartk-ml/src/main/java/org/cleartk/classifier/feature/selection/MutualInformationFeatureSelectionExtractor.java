package org.cleartk.classifier.feature.selection;

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
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.selection.MutualInformationFeatureSelectionExtractor.CombineScoreType.CombineScoreFunction;
import org.cleartk.classifier.feature.selection.MutualInformationFeatureSelectionExtractor.MutualInformationStats.ComputeScore;
import org.cleartk.classifier.feature.transform.TransformableFeature;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

public class MutualInformationFeatureSelectionExtractor<OUTCOME_T> extends
    FeatureSelectionExtractor<OUTCOME_T> implements SimpleFeatureExtractor/* , Predicate<Feature> */{

  /**
   * Specifies how scores for each outcome should be combined/aggregated into a single score
   */
  public static enum CombineScoreType {
    AVERAGE, // Average mutual information across all classes and take features with k-largest
             // values
    MAX; // Take highest mutual information value for each class
    // MERGE, // Take k-largest mutual information values for each class and merge into a single
    // collection - currently omitted because it requires a different extraction flow

    public abstract static class CombineScoreFunction<OUTCOME_T> implements
        Function<Map<OUTCOME_T, Double>, Double> {
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
        double max = Double.MIN_VALUE;
        for (Double score : input.values()) {
          max = Math.max(max, score);
        }
        return max;
      }
    }

  }

  /**
   * Helper class for aggregating stats for mutual information of features
   */
  public static class MutualInformationStats<OUTCOME_T> {
    protected Multiset<OUTCOME_T> classCounts;

    protected Table<String, OUTCOME_T, Integer> classConditionalCounts;

    public MutualInformationStats() {
      this.classCounts = HashMultiset.<OUTCOME_T> create();
      this.classConditionalCounts = HashBasedTable.<String, OUTCOME_T, Integer> create();
    }

    public void update(Instance<OUTCOME_T> instance) {
      this.classCounts.add(instance.getOutcome());
      for (Feature feature : instance.getFeatures()) {
        // Rows are feature names
        // Columns are outcome label
        Integer count = this.classConditionalCounts.get(feature.getName(), instance.getOutcome());
        if (count == null) {
          count = 0;
        }
        count++;
        this.classConditionalCounts.put(feature.getName(), instance.getOutcome(), count);
      }
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

      featureOutcomeCounts[1][1] = this.classConditionalCounts.get(featureName, outcome);
      featureOutcomeCounts[1][0] = featureCounts[1] - featureOutcomeCounts[1][1];
      featureOutcomeCounts[0][1] = outcomeCounts[1] - featureOutcomeCounts[1][1];
      featureOutcomeCounts[0][0] = n - featureCounts[1] - outcomeCounts[1]
          + featureOutcomeCounts[1][1];

      double information = 0.0;
      for (int nFeature = 0; nFeature <= 1; nFeature++) {
        for (int nOutcome = 0; nOutcome <= 1; nOutcome++) {
          information += (double) featureOutcomeCounts[nFeature][nOutcome]
              / (double) n
              * this.log2(((double) n * featureOutcomeCounts[nFeature][nOutcome])
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

    private double log2(double x) {
      return Math.log(x) / Math.log(2);
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
          writer.append(String.format("%f", this.mutualInformation(featureName, outcome)));
        }
        writer.append("\n");
      }
      writer.close();
    }

    public double computeScore(
        String featureName,
        CombineScoreType.CombineScoreFunction<OUTCOME_T> combineScoreFunction) {

      Set<OUTCOME_T> outcomes = this.classConditionalCounts.columnKeySet();
      Map<OUTCOME_T, Double> featureOutcomeMI = Maps.newHashMap();
      for (OUTCOME_T outcome : outcomes) {
        featureOutcomeMI.put(outcome, this.mutualInformation(featureName, outcome));
      }

      return combineScoreFunction.apply(featureOutcomeMI);
    }

    public ComputeScore<OUTCOME_T> getScoreFunction(CombineScoreType combineScoreType) {
      return new ComputeScore<OUTCOME_T>(this, combineScoreType);
    }

    public static class ComputeScore<OUTCOME_T> implements Function<String, Double> {

      private MutualInformationStats<OUTCOME_T> stats;

      private CombineScoreFunction<OUTCOME_T> combineScoreFunction;

      public ComputeScore(
          MutualInformationStats<OUTCOME_T> stats,
          CombineScoreType combineMeasureType) {
        this.stats = stats;
        switch (combineMeasureType) {
          case AVERAGE:
            this.combineScoreFunction = new CombineScoreType.AverageScores<OUTCOME_T>();
          case MAX:
            this.combineScoreFunction = new CombineScoreType.MaxScores<OUTCOME_T>();
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

  protected boolean isTrained;

  private MutualInformationStats<OUTCOME_T> mutualInfoStats;

  private SimpleFeatureExtractor subExtractor;

  private int numFeatures;

  private CombineScoreType combineScoreType;

  private Set<String> selectedFeatures;

  public MutualInformationFeatureSelectionExtractor(
      String name,
      SimpleFeatureExtractor extractor,
      int numFeatures,
      CombineScoreType combineMeasureType) {
    super(name);
    this.subExtractor = extractor;
    this.numFeatures = numFeatures;
    this.combineScoreType = combineMeasureType;
  }

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
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
    this.mutualInfoStats = new MutualInformationStats<OUTCOME_T>();

    for (Instance<OUTCOME_T> instance : instances) {
      OUTCOME_T outcome = instance.getOutcome();
      for (Feature feature : instance.getFeatures()) {
        if (this.isTransformable(feature)) {
          mutualInfoStats.update(feature.getName(), outcome, 1);
        }
      }
    }

    // Compute mutual information score for each feature
    Set<String> featureNames = mutualInfoStats.classConditionalCounts.rowKeySet();
    this.selectedFeatures = Sets.newTreeSet(Ordering.natural().onResultOf(
        this.mutualInfoStats.getScoreFunction(this.combineScoreType)).reverse());
    this.selectedFeatures.addAll(featureNames);
  }

  @Override
  public void save(URI uri) throws IOException {
    if (!this.isTrained) {
      throw new IOException("MutualInformationFeatureExtractor: Cannot save before training.");
    }
    File out = new File(uri);
    BufferedWriter writer = new BufferedWriter(new FileWriter(out));
    writer.append(this.combineScoreType.toString());
    writer.append("\n");

    ComputeScore<OUTCOME_T> computeScore = this.mutualInfoStats.getScoreFunction(this.combineScoreType);
    for (String feature : this.selectedFeatures) {
      writer.append(String.format("%s\t%d\n", feature, computeScore.apply(feature)));
    }

    writer.close();

  }

  @Override
  public void load(URI uri) throws IOException {
    File in = new File(uri);
    BufferedReader reader = new BufferedReader(new FileReader(in));

    // First line specifies the combine utility type
    this.combineScoreType = CombineScoreType.valueOf(reader.readLine());

    // The rest of the lines are feature + selection scores
    String line = null;
    int n = 0;
    while ((line = reader.readLine()) != null && n < this.numFeatures) {
      String[] featureValuePair = line.split("\\t");
      this.selectedFeatures.add(featureValuePair[0]);
    }

    reader.close();
  }

  @Override
  public boolean apply(Feature feature) {
    return this.selectedFeatures.contains(feature.getName());
  }

}
