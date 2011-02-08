package org.cleartk.evaluation.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This data structure is convenient if you want to stratify evaluation across different dimensions.
 * For example, if you wanted to see how good a part-of-speech tagger works for different tags you
 * might declare a FCollection of type String. Another example is a named-entity recoginizer for
 * which you want to measure performance for different lengths of named entities (in tokens). For
 * this example, you could declare a FCollection of type Integer.
 * 
 * @author Philip Ogren
 * 
 * @param <T>
 */
public class FCollection<T extends Comparable<T>> {

  protected CountCollection<T> truePositives;

  protected CountCollection<T> falsePositives;

  protected CountCollection<T> falseNegatives;

  protected int totalTruePositives = 0;

  protected int totalFalsePositives = 0;

  protected int totalFalseNegatives = 0;

  protected Set<T> objects;

  public FCollection() {
    truePositives = new CountCollection<T>();
    falsePositives = new CountCollection<T>();
    falseNegatives = new CountCollection<T>();
    objects = new HashSet<T>();
  }

  public void addTruePositive(T truePositive) {
    truePositives.add(truePositive);
    objects.add(truePositive);
    totalTruePositives++;
  }

  public void addTruePositives(T truePositive, int countIncrement) {
    truePositives.add(truePositive, countIncrement);
    objects.add(truePositive);
    totalTruePositives += countIncrement;
  }

  public int getTruePositivesCount(T object) {
    return truePositives.getCount(object);
  }

  public int getTruePositivesCount() {
    return totalTruePositives;
  }

  public void addFalsePositive(T falsePositive) {
    falsePositives.add(falsePositive);
    objects.add(falsePositive);
    totalFalsePositives++;
  }

  public void addFalsePositives(T falsePositive, int countIncrement) {
    falsePositives.add(falsePositive, countIncrement);
    objects.add(falsePositive);
    totalFalsePositives += countIncrement;
  }

  public int getFalsePositivesCount(T object) {
    return falsePositives.getCount(object);
  }

  public int getFalsePositivesCount() {
    return totalFalsePositives;
  }

  public void addFalseNegative(T falseNegative) {
    falseNegatives.add(falseNegative);
    objects.add(falseNegative);
    totalFalseNegatives++;
  }

  public void addFalseNegatives(T falseNegative, int countIncrement) {
    falseNegatives.add(falseNegative, countIncrement);
    objects.add(falseNegative);
    totalFalseNegatives += countIncrement;
  }

  public int getFalseNegativesCount(T object) {
    return falseNegatives.getCount(object);
  }

  public int getFalseNegativesCount() {
    return totalFalseNegatives;
  }

  public List<T> getSortedObjects() {
    List<T> sortedObjects = new ArrayList<T>(objects);
    Collections.sort(sortedObjects);
    return sortedObjects;
  }

  public double getPrecision(T object) {
    return precision(getTruePositivesCount(object), getFalsePositivesCount(object));
  }

  public double getPrecision() {
    return precision(getTruePositivesCount(), getFalsePositivesCount());
  }

  public static double precision(int tps, int fps) {
    return (double) tps / (tps + fps);
  }

  public double getRecall(T object) {
    return recall(getTruePositivesCount(object), getFalseNegativesCount(object));
  }

  public double getRecall() {
    return recall(getTruePositivesCount(), getFalseNegativesCount());
  }

  public static double recall(int tps, int fns) {
    return (double) tps / (tps + fns);
  }

  public double getF(T object) {
    return F(
        getTruePositivesCount(object),
        getFalsePositivesCount(object),
        getFalseNegativesCount(object));
  }

  public double getF() {
    return F(getTruePositivesCount(), getFalsePositivesCount(), getFalseNegativesCount());
  }

  /**
   * When the beta weight is .5, F-measure works out to be a very simple ratio if you work out the
   * algebra (which I did once.) This result is published in the literature here:
   * 
   * http://www.ncbi.nlm.nih.gov/pmc/articles/PMC1090460/
   * 
   * See equation 3.
   * 
   * @param object
   * @return
   */

  public static double F(int tps, int fps, int fns) {
    return (double) (2 * tps) / (2 * tps + fps + fns);
  }

  public static double F(int tps, int fps, int fns, double B) {
    double precision = precision(tps, fps);
    double recall = recall(tps, fns);
    double numerator = (1.0d + B * B) * recall * precision;
    double denominator = ((B * B) * precision) + recall;
    return numerator / denominator;
  }

  public double getF(T object, double B) {
    return F(
        getTruePositivesCount(object),
        getFalsePositivesCount(object),
        getFalseNegativesCount(object),
        B);
  }

}
