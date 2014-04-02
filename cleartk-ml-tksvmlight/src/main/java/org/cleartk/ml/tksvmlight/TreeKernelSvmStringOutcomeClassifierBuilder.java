/*
 * Copyright (c) 2007-2013, Regents of the University of Colorado 
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
package org.cleartk.ml.tksvmlight;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.ml.jar.ClassifierBuilder_ImplBase;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.tksvmlight.model.TreeKernelSvmModel;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2007-2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @author Tim Miller
 */
@Beta
public abstract class TreeKernelSvmStringOutcomeClassifierBuilder
    extends
    ClassifierBuilder_ImplBase<TreeKernelSvmStringOutcomeClassifier, TreeFeatureVector, String, Integer> {

  protected TreeMap<Integer, TreeKernelSvmModel> models;
  protected TreeKernelSvmBooleanOutcomeClassifierBuilder<?> builder = null;
  
  public abstract String getPackageName();

  /**
   * @param dir
   *          The directory that contains the all false training data.
   */
  @Override
  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data-allfalse." + this.getPackageName()); // svmlight");
  }

  /**
   * Get the training data file for a specific label.
   * 
   * @param dir
   *          The directory to keep the training data.
   * @param label
   *          The integer number that represents an index for a particular label.
   * @return The file of the training data to be used to train the model.
   */
  public File getTrainingDataFile(File dir, int label) {
    return new File(dir, String.format("training-data-%d.%s", label, this.getPackageName()));
  }

  /**
   * Train the classifier.
   * 
   * @param dir
   *          The directory where the training data has been written.
   * @param args
   *          The arguments to be used by the tk_svm_classify command. Note: -t 5 is used to specify
   *          the use of Tree Kernels.
   */
  @Override
  public void trainClassifier(File dir, String... args) throws Exception {
    for (File file : dir.listFiles()) {
      if (file.getName().matches("training-data-\\d+." + this.getPackageName())){
        builder.trainClassifier(file, args);
      }
    }
  }

  /**
   * Package the classifier into a jar file.
   */
  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);

    int label = 1;
    while (true) {
      File modelFile = new File(dir, String.format(
          "training-data-%d.%s.model",
          label,
          this.getPackageName()));

      if (!modelFile.exists()) {
        break;
      }

      String modelName = String.format("model-%d.%s", label, this.getPackageName());
      JarStreams.putNextJarEntry(modelStream, modelName, modelFile);

      label += 1;
    }
  }

  /**
   * Unpackage the classifier out of a JarInputStream.
   */
  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    this.models = new TreeMap<Integer, TreeKernelSvmModel>();
    // File model;
    TreeKernelSvmModel model;

    int label = 1;
    while ((model = getNextModel(modelStream, label)) != null) {
      this.models.put(label, model);
      label += 1;
    }

    if (this.models.isEmpty()) {
      throw new IOException(String.format("no models found in %s", modelStream));
    }
  }

  private TreeKernelSvmModel getNextModel(JarInputStream modelStream, int label) throws IOException {
    // look for a next entry or return null if there isn't one
    JarEntry entry = modelStream.getNextJarEntry();
    if (entry == null) {
      return null;
    }

    // make sure the name was the model we expected
    String expectedName = String.format("model-%d.%s", label, this.getPackageName());
    if (!entry.getName().equals(expectedName)) {
      throw new IOException(String.format(
          "expected next jar entry to be %s, found %s",
          expectedName,
          entry.getName()));
    }
    TreeKernelSvmModel model = TreeKernelSvmModel.fromInputStream(modelStream);
    return model;
  }

  /**
   * Create the Classifier.
   */
  @Override
  protected TreeKernelSvmStringOutcomeClassifier newClassifier() {
    return new TreeKernelSvmStringOutcomeClassifier(
        this.featuresEncoder,
        this.outcomeEncoder,
        this.models);
  }
}
