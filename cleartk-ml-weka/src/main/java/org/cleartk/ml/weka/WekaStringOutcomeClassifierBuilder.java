/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.ml.weka;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.ClassifierBuilder_ImplBase;
import org.cleartk.ml.jar.JarStreams;

import com.google.common.annotations.Beta;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */
@Beta
public class WekaStringOutcomeClassifierBuilder extends
    ClassifierBuilder_ImplBase<WekaStringOutcomeClassifier, Iterable<Feature>, String, String> {

  public static final String TRAINING_FILE_NAME = "training-data.arff";

  @Override
  public File getTrainingDataFile(File dir) {
    return new File(dir, TRAINING_FILE_NAME);
  }

  @Override
  public void trainClassifier(File dir, String... args) throws Exception {
    File arffFile = getTrainingDataFile(dir);
    File modelFile = getModelFile(dir);
    String classifierCls;
    String options;
    if (args.length > 0)
      classifierCls = args[0];
    else
      classifierCls = J48.class.getName();

    if (args.length > 1)
      options = args[1];
    else
      options = "";

    buildModelFromArffFile(arffFile, classifierCls, options, modelFile);
  }

  private void buildModelFromArffFile(
      File arffFile,
      String classifierClsName,
      String options,
      File modelFile) throws Exception, InstantiationException, IllegalAccessException,
          ClassNotFoundException, IOException, FileNotFoundException {
    DataSource source = new DataSource(arffFile.getAbsolutePath());
    Instances data = source.getDataSet();
    data.setClassIndex(data.numAttributes() - 1);

    AbstractClassifier classifierCls = (AbstractClassifier) getClass().getClassLoader().loadClass(
        classifierClsName).newInstance();
    String[] splitOptions = weka.core.Utils.splitOptions(options);
    classifierCls.setOptions(splitOptions);
    classifierCls.buildClassifier(data);

    ObjectOutputStream modelStream = new ObjectOutputStream(new FileOutputStream(modelFile));
    modelStream.writeObject(classifierCls);
    modelStream.close();
  }

  private File getModelFile(File dir) {
    return new File(dir, getModelName());
  }

  private String getModelName() {
    return "weka.model";
  }

  private Classifier classifier;

  @Override
  protected WekaStringOutcomeClassifier newClassifier() {
    try {
      return new WekaStringOutcomeClassifier(
          this.featuresEncoder,
          this.outcomeEncoder,
          this.classifier);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);
    JarStreams.putNextJarEntry(modelStream, this.getModelName(), this.getModelFile(dir));
  }

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    JarStreams.getNextJarEntry(modelStream, this.getModelName());

    ObjectInputStream classifierStream = new ObjectInputStream(modelStream);
    try {
      this.classifier = (Classifier) classifierStream.readObject();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

  }
}
