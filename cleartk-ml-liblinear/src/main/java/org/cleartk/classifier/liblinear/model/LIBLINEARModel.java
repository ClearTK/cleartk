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
package org.cleartk.classifier.liblinear.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cleartk.classifier.util.featurevector.ArrayFeatureVector;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.InvalidFeatureVectorValueException;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * @author Philip Ogren
 * 
 */

public class LIBLINEARModel {

  public static LIBLINEARModel fromFile(File modelFile) throws IOException {
    InputStream modelStream = new FileInputStream(modelFile);
    LIBLINEARModel model = fromInputStream(modelStream);
    modelStream.close();
    return model;
  }

  public static LIBLINEARModel fromInputStream(InputStream modelStream) throws IOException {
    LIBLINEARModel model = new LIBLINEARModel();

    BufferedReader in = new BufferedReader(new InputStreamReader(modelStream));
    String buffer;
    String[] fields;

    buffer = in.readLine(); // solver_type

    buffer = in.readLine(); // nr_class
    model.numberOfClasses = Integer.valueOf(buffer.split(" ")[1]);
    if (model.numberOfClasses == 2)
      model.numberOfClassifiers = 1;
    else
      model.numberOfClassifiers = model.numberOfClasses;

    model.classifiers = new Classifier[model.numberOfClassifiers];
    for (int i = 0; i < model.numberOfClassifiers; i++)
      model.classifiers[i] = new Classifier();

    buffer = in.readLine(); // label
    fields = buffer.split(" ");
    for (int i = 0; i < model.numberOfClassifiers; i++)
      model.classifiers[i].label = Integer.valueOf(fields[i + 1]);
    if (model.numberOfClasses == 2)
      model.fallbackLabel = Integer.valueOf(fields[2]);

    buffer = in.readLine(); // nr_feature
    int numberOfFeatures = Integer.valueOf(buffer.split(" ")[1]);

    buffer = in.readLine(); // bias
    double bias = Double.valueOf(buffer.split(" ")[1]);

    buffer = in.readLine(); // w

    for (int i = 0; i < numberOfFeatures; i++) {
      buffer = in.readLine();
      fields = buffer.trim().split(" ");
      for (int j = 0; j < model.numberOfClassifiers; j++) {
        try {
          model.classifiers[j].weightVector.set(i + 1, Double.valueOf(fields[j]));
        } catch (InvalidFeatureVectorValueException e) {
          throw new IOException(e);
        }
      }
    }

    if (bias >= 0) {
      buffer = in.readLine();
      fields = buffer.trim().split(" ");
      for (int j = 0; j < model.numberOfClassifiers; j++)
        model.classifiers[j].bias = Double.valueOf(fields[j]) * bias;
    }

    in.close();

    return model;
  }

  int numberOfClasses;

  int numberOfClassifiers;

  double[] biases;

  Classifier[] classifiers;

  int fallbackLabel;

  LIBLINEARModel() {
  }

  public int predict(FeatureVector featureVector) {
    double[] values = new double[numberOfClassifiers];

    for (int i = 0; i < numberOfClassifiers; i++)
      values[i] = classifiers[i].evaluate(featureVector);

    if (numberOfClasses == 2) {
      if (values[0] > 0) {
        return classifiers[0].label;
      } else {
        return fallbackLabel;
      }
    } else {
      int max_index = 0;

      for (int i = 1; i < numberOfClassifiers; i++) {
        if (values[i] > values[max_index])
          max_index = i;
      }
      return classifiers[max_index].label;
    }

  }

  public class ScoredPrediction implements Comparable<ScoredPrediction> {
    int prediction;

    double score;

    public ScoredPrediction(int prediction, double score) {
      super();
      this.prediction = prediction;
      this.score = score;
    }

    public int compareTo(ScoredPrediction arg0) {
      return Double.compare(arg0.score, this.score);
    }

    public int getPrediction() {
      return prediction;
    }

    public double getScore() {
      return score;
    }

  }

  public List<ScoredPrediction> score(FeatureVector featureVector) {
    double[] values = new double[numberOfClassifiers];

    for (int i = 0; i < numberOfClassifiers; i++)
      values[i] = classifiers[i].evaluate(featureVector);

    if (numberOfClasses == 2) {
      if (values[0] > 0) {
        return Arrays.asList(
            new ScoredPrediction(classifiers[0].label, values[0]),
            new ScoredPrediction(fallbackLabel, -values[0]));
      } else {
        return Arrays.asList(new ScoredPrediction(fallbackLabel, -values[0]), new ScoredPrediction(
            classifiers[0].label,
            values[0]));
      }
    } else {
      List<ScoredPrediction> returnValues = new ArrayList<ScoredPrediction>();
      for (int i = 1; i < numberOfClassifiers; i++) {
        returnValues.add(new ScoredPrediction(classifiers[i].label, values[i]));
      }
      Collections.sort(returnValues);
      return returnValues;
    }

  }

  static class Classifier {
    FeatureVector weightVector;

    double bias;

    int label;

    Classifier() {
      this.weightVector = new ArrayFeatureVector();
      this.bias = 0.0;
      this.label = 0;
    }

    double evaluate(FeatureVector featureVector) {
      return weightVector.innerProduct(featureVector) + bias;
    }
  }
}