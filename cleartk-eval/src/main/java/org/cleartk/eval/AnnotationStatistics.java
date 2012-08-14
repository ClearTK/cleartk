/* 
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
package org.cleartk.eval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.eval.util.ConfusionMatrix;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Stores statistics for comparing {@link Annotation}s extracted by a system to gold
 * {@link Annotation}s.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class AnnotationStatistics<OUTCOME_TYPE extends Comparable<? super OUTCOME_TYPE>> implements
    Serializable {

  private static final long serialVersionUID = 1L;

  private Multiset<OUTCOME_TYPE> referenceOutcomes;

  private Multiset<OUTCOME_TYPE> predictedOutcomes;

  private Multiset<OUTCOME_TYPE> correctOutcomes;

  private ConfusionMatrix<OUTCOME_TYPE> confusionMatrix;

  /**
   * Creates a {@link Function} that converts an {@link Annotation} into a hashable representation
   * of its begin and end offsets.
   * 
   * The {@link Function} created by this method is suitable for passing to the first
   * {@link Function} argument of {@link #add(Collection, Collection, Function, Function)}.
   */
  public static <ANNOTATION_TYPE extends Annotation> Function<ANNOTATION_TYPE, Span> annotationToSpan() {
    return new Function<ANNOTATION_TYPE, Span>() {
      @Override
      public Span apply(ANNOTATION_TYPE annotation) {
        return new Span(annotation);
      }
    };
  }

  /**
   * Creates a {@link Function} that extracts a feature value from a {@link TOP}.
   * 
   * The {@link Function} created by this method is suitable for passing to the second
   * {@link Function} argument of {@link #add(Collection, Collection, Function, Function)}.
   * 
   * @param featureName
   *          The name of the feature whose value is to be extracted.
   */
  public static <ANNOTATION_TYPE extends TOP> Function<ANNOTATION_TYPE, String> annotationToFeatureValue(
      final String featureName) {
    return new Function<ANNOTATION_TYPE, String>() {
      @Override
      public String apply(ANNOTATION_TYPE annotation) {
        Feature feature = annotation.getType().getFeatureByBaseName(featureName);
        return annotation.getFeatureValueAsString(feature);
      }
    };
  }

  /**
   * Creates a {@link Function} that always returns null.
   * 
   * This may be useful when only the span of the offset is important, but you still need to pass in
   * the final argument of {@link #add(Collection, Collection, Function, Function)}.
   */
  public static <ANNOTATION_TYPE, OUTCOME_TYPE> Function<ANNOTATION_TYPE, OUTCOME_TYPE> annotationToNull() {
    return new Function<ANNOTATION_TYPE, OUTCOME_TYPE>() {
      @Override
      public OUTCOME_TYPE apply(ANNOTATION_TYPE annotation) {
        return null;
      }
    };
  }

  /**
   * Add all statistics together.
   * 
   * This is often useful for combining individual fold statistics that result from methods like
   * {@link Evaluation_ImplBase#crossValidation(List, int)}.
   * 
   * @param statistics
   *          The sequence of statistics that should be combined.
   * @return The combination of all the individual statistics.
   */
  public static <OUTCOME_TYPE extends Comparable<? super OUTCOME_TYPE>> AnnotationStatistics<OUTCOME_TYPE> addAll(
      Iterable<AnnotationStatistics<OUTCOME_TYPE>> statistics) {
    AnnotationStatistics<OUTCOME_TYPE> result = new AnnotationStatistics<OUTCOME_TYPE>();
    for (AnnotationStatistics<OUTCOME_TYPE> item : statistics) {
      result.addAll(item);
    }
    return result;
  }

  /**
   * Create an AnnotationStatistics that compares {@link Annotation}s based on their begin and end
   * offsets, plus a {@link Feature} of the {@link Annotation} that represents the outcome or label.
   */
  public AnnotationStatistics() {
    this.referenceOutcomes = HashMultiset.create();
    this.predictedOutcomes = HashMultiset.create();
    this.correctOutcomes = HashMultiset.create();
    this.confusionMatrix = new ConfusionMatrix<OUTCOME_TYPE>();
  }

  /**
   * Update the statistics, comparing the reference annotations to the predicted annotations.
   * 
   * Annotations are considered to match if they have the same character offsets in the text. All
   * outcomes (e.g. as returned in {@link #confusions()}) will be <code>null</code>.
   * 
   * @param referenceAnnotations
   *          The reference annotations, typically identified by humans.
   * @param predictedAnnotations
   *          The predicted annotations, typically identified by a model.
   */
  public <ANNOTATION_TYPE extends Annotation> void add(
      Collection<? extends ANNOTATION_TYPE> referenceAnnotations,
      Collection<? extends ANNOTATION_TYPE> predictedAnnotations) {
    this.add(
        referenceAnnotations,
        predictedAnnotations,
        AnnotationStatistics.<ANNOTATION_TYPE> annotationToSpan(),
        AnnotationStatistics.<ANNOTATION_TYPE, OUTCOME_TYPE> annotationToNull());
  }

  /**
   * Update the statistics, comparing the reference annotations to the predicted annotations.
   * 
   * Annotations are considered to match if they have the same span (according to
   * {@code annotationToSpan}) and if they have the same outcome (according to
   * {@code annotationToOutcome}).
   * 
   * @param referenceAnnotations
   *          The reference annotations, typically identified by humans.
   * @param predictedAnnotations
   *          The predicted annotations, typically identified by a model.
   * @param annotationToSpan
   *          A function that defines how to convert an annotation into a hashable object that
   *          represents the span of that annotation. The {@link #annotationToSpan()} method
   *          provides an example function that could be used here.
   * @param annotationToOutcome
   *          A function that defines how to convert an annotation into an object that represents
   *          the outcome (or "label") assigned to that annotation. The
   *          {@link #annotationToFeatureValue(String)} method provides a sample function that could
   *          be used here.
   */
  public <ANNOTATION_TYPE, SPAN_TYPE> void add(
      Collection<? extends ANNOTATION_TYPE> referenceAnnotations,
      Collection<? extends ANNOTATION_TYPE> predictedAnnotations,
      Function<ANNOTATION_TYPE, SPAN_TYPE> annotationToSpan,
      Function<ANNOTATION_TYPE, OUTCOME_TYPE> annotationToOutcome) {

    // map gold spans to their outcomes
    Map<SPAN_TYPE, OUTCOME_TYPE> referenceSpanOutcomes = new HashMap<SPAN_TYPE, OUTCOME_TYPE>();
    for (ANNOTATION_TYPE ann : referenceAnnotations) {
      referenceSpanOutcomes.put(annotationToSpan.apply(ann), annotationToOutcome.apply(ann));
    }

    // map system spans to their outcomes
    Map<SPAN_TYPE, OUTCOME_TYPE> predictedSpanOutcomes = new HashMap<SPAN_TYPE, OUTCOME_TYPE>();
    for (ANNOTATION_TYPE ann : predictedAnnotations) {
      predictedSpanOutcomes.put(annotationToSpan.apply(ann), annotationToOutcome.apply(ann));
    }

    // update the gold and system outcomes
    this.referenceOutcomes.addAll(referenceSpanOutcomes.values());
    this.predictedOutcomes.addAll(predictedSpanOutcomes.values());

    // determine the outcomes that were correct
    Set<SPAN_TYPE> intersection = new HashSet<SPAN_TYPE>();
    intersection.addAll(referenceSpanOutcomes.keySet());
    intersection.retainAll(predictedSpanOutcomes.keySet());
    for (SPAN_TYPE span : intersection) {
      OUTCOME_TYPE goldOutcome = referenceSpanOutcomes.get(span);
      OUTCOME_TYPE systemOutcome = predictedSpanOutcomes.get(span);
      if (Objects.equal(goldOutcome, systemOutcome)) {
        this.correctOutcomes.add(goldOutcome);
      }
    }

    // update the confusion matrix
    Set<SPAN_TYPE> union = new HashSet<SPAN_TYPE>();
    union.addAll(referenceSpanOutcomes.keySet());
    union.addAll(predictedSpanOutcomes.keySet());
    for (SPAN_TYPE span : union) {
      OUTCOME_TYPE goldOutcome = referenceSpanOutcomes.get(span);
      OUTCOME_TYPE systemOutcome = predictedSpanOutcomes.get(span);
      this.confusionMatrix.add(goldOutcome, systemOutcome);
    }
  }

  /**
   * Adds all the statistics collected by another AnnotationStatistics to this one.
   * 
   * @param that
   *          The other statistics that should be added to this one.
   */
  public void addAll(AnnotationStatistics<OUTCOME_TYPE> that) {
    this.referenceOutcomes.addAll(that.referenceOutcomes);
    this.predictedOutcomes.addAll(that.predictedOutcomes);
    this.correctOutcomes.addAll(that.correctOutcomes);
    this.confusionMatrix.add(that.confusionMatrix);
  }

  /**
   * Returns the {@link ConfusionMatrix} tabulating reference outcomes matched to predicted
   * outcomes.
   * 
   * @return The confusion matrix.
   */
  public ConfusionMatrix<OUTCOME_TYPE> confusions() {
    return this.confusionMatrix;
  }

  public double precision() {
    int nSystem = this.predictedOutcomes.size();
    return nSystem == 0 ? 1.0 : ((double) this.correctOutcomes.size()) / nSystem;
  }

  public double precision(OUTCOME_TYPE outcome) {
    int nSystem = this.predictedOutcomes.count(outcome);
    return nSystem == 0 ? 1.0 : ((double) this.correctOutcomes.count(outcome)) / nSystem;
  }

  public double recall() {
    int nGold = this.referenceOutcomes.size();
    return nGold == 0 ? 1.0 : ((double) this.correctOutcomes.size()) / nGold;
  }

  public double recall(OUTCOME_TYPE outcome) {
    int nGold = this.referenceOutcomes.count(outcome);
    return nGold == 0 ? 1.0 : ((double) this.correctOutcomes.count(outcome)) / nGold;
  }

  public double f1() {
    double p = this.precision();
    double r = this.recall();
    double sum = p + r;
    return sum == 0.0 ? 0.0 : (2 * p * r) / sum;
  }

  public double f1(OUTCOME_TYPE outcome) {
    double p = this.precision(outcome);
    double r = this.recall(outcome);
    double sum = p + r;
    return sum == 0.0 ? 0.0 : (2 * p * r) / sum;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("P\tR\tF1\t#gold\t#system\t#correct\n");
    result.append(String.format(
        "%.3f\t%.3f\t%.3f\t%d\t%d\t%d\tOVERALL\n",
        this.precision(),
        this.recall(),
        this.f1(),
        this.referenceOutcomes.size(),
        this.predictedOutcomes.size(),
        this.correctOutcomes.size()));
    List<OUTCOME_TYPE> outcomes = new ArrayList<OUTCOME_TYPE>(this.referenceOutcomes.elementSet());
    if (outcomes.size() > 1) {
      Collections.sort(outcomes);
      for (OUTCOME_TYPE outcome : outcomes) {
        result.append(String.format(
            "%.3f\t%.3f\t%.3f\t%d\t%d\t%d\t%s\n",
            this.precision(outcome),
            this.recall(outcome),
            this.f1(outcome),
            this.referenceOutcomes.count(outcome),
            this.predictedOutcomes.count(outcome),
            this.correctOutcomes.count(outcome),
            outcome));
      }
    }
    return result.toString();
  }

  private static class Span {

    public int end;

    public int begin;

    public Span(Annotation annotation) {
      this.begin = annotation.getBegin();
      this.end = annotation.getEnd();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.begin, this.end);
    }

    @Override
    public boolean equals(Object obj) {
      if (!this.getClass().equals(obj.getClass())) {
        return false;
      }
      Span that = (Span) obj;
      return this.begin == that.begin && this.end == that.end;
    }

    @Override
    public String toString() {
      ToStringHelper helper = Objects.toStringHelper(this);
      helper.add("begin", this.begin);
      helper.add("end", this.end);
      return helper.toString();
    }
  }
}
