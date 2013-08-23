package org.cleartk.classifier.tksvmlight;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.cleartk.classifier.jar.ClassifierBuilder_ImplBase;
import org.cleartk.classifier.jar.JarStreams;
import org.cleartk.classifier.tksvmlight.model.TreeKernelSVMModel;

public abstract class TreeKernelSVMStringOutcomeClassifierBuilder
    extends
    ClassifierBuilder_ImplBase<TreeKernelSVMStringOutcomeClassifier, TreeFeatureVector, String, Integer> {

  protected TreeMap<Integer, TreeKernelSVMModel> models;

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
    this.models = new TreeMap<Integer, TreeKernelSVMModel>();
    // File model;
    TreeKernelSVMModel model;

    int label = 1;
    while ((model = getNextModel(modelStream, label)) != null) {
      this.models.put(label, model);
      label += 1;
    }

    if (this.models.isEmpty()) {
      throw new IOException(String.format("no models found in %s", modelStream));
    }
  }

  private TreeKernelSVMModel getNextModel(JarInputStream modelStream, int label) throws IOException {
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
    TreeKernelSVMModel model = TreeKernelSVMModel.fromInputStream(modelStream);
    return model;
  }

  /**
   * Create the Classifier.
   */
  @Override
  protected TreeKernelSVMStringOutcomeClassifier newClassifier() {
    return new TreeKernelSVMStringOutcomeClassifier(
        this.featuresEncoder,
        this.outcomeEncoder,
        this.models);
  }
}
