/** 
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
package org.cleartk.eval.util;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This data structure provides an easy way to build and output a confusion matrix. A confusion
 * matrix is a two dimensional table with a row and table for each class. Each element in the matrix
 * shows the number of test examples for which the actual class is the row and the predicted class
 * is the column. Display of this matrix is useful for identifying when a system is confusing two
 * classes
 * 
 * <br>
 * For more info @see <a href="http://en.wikipedia.org/wiki/Confusion_matrix">The wikipedia page on
 * Confusion Matrices</a>
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 * @param <T>
 *          The data type used to represent the class labels
 */

public class ConfusionMatrix<T extends Comparable<? super T>> {
  private Map<T, Multiset<T>> matrix;

  private SortedSet<T> classes;

  /**
   * Creates an empty confusion Matrix
   */
  public ConfusionMatrix() {
    this.matrix = new HashMap<T, Multiset<T>>();
    this.classes = new TreeSet<T>(Ordering.natural().nullsFirst());
  }

  /**
   * Creates a new ConfusionMatrix initialized with the contents of another ConfusionMatrix.
   */
  public ConfusionMatrix(ConfusionMatrix<T> other) {
    this();
    this.add(other);
  }

  /**
   * Increments the entry specified by actual and predicted by one.
   */
  public void add(T actual, T predicted) {
    add(actual, predicted, 1);
  }

  /**
   * Increments the entry specified by actual and predicted by count.
   */
  public void add(T actual, T predicted, int count) {
    if (matrix.containsKey(actual)) {
      matrix.get(actual).add(predicted, count);
    } else {
      Multiset<T> counts = HashMultiset.create();
      counts.add(predicted, count);
      matrix.put(actual, counts);
    }

    classes.add(actual);
    classes.add(predicted);
  }

  /**
   * Adds the entries from another confusion matrix to this one.
   */
  public void add(ConfusionMatrix<T> other) {
    for (T actual : other.matrix.keySet()) {
      Multiset<T> counts = other.matrix.get(actual);
      for (T predicted : counts.elementSet()) {
        int count = counts.count(predicted);
        this.add(actual, predicted, count);
      }
    }
  }

  /**
   * Gives the set of all classes in the confusion matrix.
   */
  public SortedSet<T> getClasses() {
    return classes;
  }

  /**
   * Gives the count of the number of times the "predicted" class was predicted for the "actual"
   * class.
   */
  public int getCount(T actual, T predicted) {
    if (!matrix.containsKey(actual)) {
      return 0;
    } else {
      return matrix.get(actual).count(predicted);
    }
  }

  /**
   * Computes the total number of times the class was predicted by the classifier.
   */
  public int getPredictedTotal(T predicted) {
    int total = 0;
    for (T actual : classes) {
      total += getCount(actual, predicted);
    }
    return total;
  }

  /**
   * Computes the total number of times the class actually appeared in the data.
   */
  public int getActualTotal(T actual) {
    if (!matrix.containsKey(actual)) {
      return 0;
    } else {
      int total = 0;
      for (T elem : matrix.get(actual).elementSet()) {
        total += matrix.get(actual).count(elem);
      }
      return total;
    }
  }

  

  /**
   * Outputs the ConfusionMatrix as comma-separated values for easy import into spreadsheets
   */
  public String toCSV() {
    StringBuilder builder = new StringBuilder();

    // Header Row
    builder.append(",,Predicted Class,\n");

    // Predicted Classes Header Row
    builder.append(",,");
    for (T predicted : classes) {
      builder.append(String.format("%s,", predicted));
    }
    builder.append("Total\n");

    // Data Rows
    String firstColumnLabel = "Actual Class,";
    for (T actual : classes) {
      builder.append(firstColumnLabel);
      firstColumnLabel = ",";
      builder.append(String.format("%s,", actual));

      for (T predicted : classes) {
        builder.append(getCount(actual, predicted));
        builder.append(",");
      }
      // Actual Class Totals Column
      builder.append(getActualTotal(actual));
      builder.append("\n");
    }

    // Predicted Class Totals Row
    builder.append(",Total,");
    for (T predicted : classes) {
      builder.append(getPredictedTotal(predicted));
      builder.append(",");
    }
    builder.append("\n");

    return builder.toString();
  }

  /**
   * Outputs Confusion Matrix in an HTML table. Cascading Style Sheets (CSS) can control the table's
   * appearance by defining the empty-space, actual-count-header, predicted-class-header, and
   * count-element classes. For example
   * 
   * @return html string
   */
  public String toHTML() {
    StringBuilder builder = new StringBuilder();

    int numClasses = classes.size();
    // Header Row
    builder.append("<table>\n");
    builder.append("<tr><th class=\"empty-space\" colspan=\"2\" rowspan=\"2\">");
    builder.append(String.format(
        "<th class=\"predicted-class-header\" colspan=\"%d\">Predicted Class</th></tr>\n",
        numClasses + 1));

    // Predicted Classes Header Row
    builder.append("<tr>");
    // builder.append("<th></th><th></th>");
    for (T predicted : classes) {
      builder.append("<th class=\"predicted-class-header\">");
      builder.append(predicted);
      builder.append("</th>");
    }
    builder.append("<th class=\"predicted-class-header\">Total</th>");
    builder.append("</tr>\n");

    // Data Rows
    String firstColumnLabel = String.format(
        "<tr><th class=\"actual-class-header\" rowspan=\"%d\">Actual Class</th>",
        numClasses + 1);
    for (T actual : classes) {
      builder.append(firstColumnLabel);
      firstColumnLabel = "<tr>";
      builder.append(String.format("<th class=\"actual-class-header\" >%s</th>", actual));

      for (T predicted : classes) {
        builder.append("<td class=\"count-element\">");
        builder.append(getCount(actual, predicted));
        builder.append("</td>");
      }

      // Actual Class Totals Column
      builder.append("<td class=\"count-element\">");
      builder.append(getActualTotal(actual));
      builder.append("</td>");
      builder.append("</tr>\n");
    }

    // Predicted Class Totals Row
    builder.append("<tr><th class=\"actual-class-header\">Total</th>");
    for (T predicted : classes) {
      builder.append("<td class=\"count-element\">");
      builder.append(getPredictedTotal(predicted));
      builder.append("</td>");
    }
    builder.append("<td class=\"empty-space\"></td>\n");
    builder.append("</tr>\n");
    builder.append("</table>\n");

    return builder.toString();
  }

  public static void main(String[] args) {
    ConfusionMatrix<String> confusionMatrix = new ConfusionMatrix<String>();

    confusionMatrix.add("a", "a", 88);
    confusionMatrix.add("a", "b", 10);
    // confusionMatrix.add("a", "c", 2);
    confusionMatrix.add("b", "a", 14);
    confusionMatrix.add("b", "b", 40);
    confusionMatrix.add("b", "c", 6);
    confusionMatrix.add("c", "a", 18);
    confusionMatrix.add("c", "b", 10);
    confusionMatrix.add("c", "c", 12);

    ConfusionMatrix<String> confusionMatrix2 = new ConfusionMatrix<String>(confusionMatrix);
    confusionMatrix2.add(confusionMatrix);
    System.out.println(confusionMatrix2.toHTML());
    System.out.println(confusionMatrix2.toCSV());
  }

  @Override
  public String toString()
  {
      return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("matrix", matrix)
              .toString();
  }
}
