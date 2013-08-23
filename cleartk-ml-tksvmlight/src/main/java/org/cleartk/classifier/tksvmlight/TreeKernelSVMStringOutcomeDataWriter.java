package org.cleartk.classifier.tksvmlight;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.TreeMap;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.outcome.StringToIntegerOutcomeEncoder;
import org.cleartk.classifier.jar.DataWriter_ImplBase;

public abstract class TreeKernelSVMStringOutcomeDataWriter<CLASSIFIER_BUILDER_TYPE extends TreeKernelSVMStringOutcomeClassifierBuilder>
    extends
    // <CLASSIFIER_BUILDER_TYPE extends EncodingJarClassifierBuilder<? extends Classifier<String>,
    // TreeFeatureVector, String, Integer>> extends
    DataWriter_ImplBase<CLASSIFIER_BUILDER_TYPE, TreeFeatureVector, String, Integer> {

  protected File allFalseFile;

  protected PrintWriter allFalseWriter;

  protected Map<Integer, PrintWriter> trainingDataWriters;

  public TreeKernelSVMStringOutcomeDataWriter(File outputDirectory) throws FileNotFoundException {
    super(outputDirectory);
    TreeFeatureVectorFeaturesEncoder myFeaturesEncoder = new TreeFeatureVectorFeaturesEncoder();
    myFeaturesEncoder.addEncoder(new NumberEncoder());
    myFeaturesEncoder.addEncoder(new BooleanEncoder());
    myFeaturesEncoder.addEncoder(new StringEncoder());
    this.setFeaturesEncoder(myFeaturesEncoder);
    this.setOutcomeEncoder(new StringToIntegerOutcomeEncoder());

    // aliases to make it easy to remember what the "main" file is being used for
    allFalseFile = this.trainingDataFile;
    allFalseWriter = this.trainingDataWriter;

    // create the output writers
    trainingDataWriters = new TreeMap<Integer, PrintWriter>();
  }

  @Override
  protected void writeEncoded(TreeFeatureVector features, Integer outcome)
      throws CleartkProcessingException {
    if (outcome != null && !trainingDataWriters.containsKey(outcome)) {
      try {
        addClass(outcome);
      } catch (IOException e) {
        throw new CleartkProcessingException(e);
      }
    }

    StringBuffer featureString = new StringBuffer();
    featureString.append(TKSVMlightBooleanOutcomeDataWriter.createString(features));

    StringBuffer output = new StringBuffer();
    if (outcome == null)
      output.append("0");
    else
      output.append("-1");
    output.append(featureString);
    allFalseWriter.println(output);

    for (int i : trainingDataWriters.keySet()) {
      output = new StringBuffer();
      if (outcome == null)
        output.append("0");
      else if (outcome == i)
        output.append("+1");
      else
        output.append("-1");

      output.append(featureString);
      trainingDataWriters.get(i).println(output);
    }
  }

  /**
   * Callback called after the last feature vector has been written. Flushes the various streams and
   * deletes the all false file.
   */
  @Override
  public void finish() throws CleartkProcessingException {
    // close and remove all-false file
    allFalseWriter.close();
    allFalseFile.delete();

    // flush and close all training data writers
    for (PrintWriter pw : trainingDataWriters.values()) {
      pw.flush();
      pw.close();
    }

    // finish in the superclass
    super.finish();
  }

  private void addClass(int label) throws IOException {
    File newTDFile = this.classifierBuilder.getTrainingDataFile(this.outputDirectory, label);
    newTDFile.delete();

    allFalseWriter.flush();
    copyFile(allFalseFile, newTDFile);
    trainingDataWriters.put(label, new PrintWriter(new BufferedWriter(new FileWriter(
        newTDFile,
        true))));
  }

  private void copyFile(File source, File target) throws IOException {
    // Create channel on the source
    FileInputStream srcStream = new FileInputStream(source);
    FileChannel srcChannel = srcStream.getChannel();

    // Create channel on the destination
    FileOutputStream dstStream = new FileOutputStream(target);
    FileChannel dstChannel = dstStream.getChannel();

    // Copy file contents from source to destination
    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

    // Close the channels
    srcStream.close();
    srcChannel.close();
    dstStream.close();
    dstChannel.close();
  }
}
