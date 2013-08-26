package org.cleartk.classifier.tksvmlight;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.encoder.CleartkEncoderException;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.outcome.BooleanToBooleanOutcomeEncoder;
import org.cleartk.classifier.jar.DataWriter_ImplBase;
import org.cleartk.classifier.util.featurevector.FeatureVector;

public class TreeKernelSVMBooleanOutcomeDataWriter extends 
  DataWriter_ImplBase<TreeKernelSVMBooleanOutcomeClassifierBuilder<TreeKernelSVMBooleanOutcomeClassifier>, TreeFeatureVector, Boolean,Boolean>{

  public TreeKernelSVMBooleanOutcomeDataWriter(File outputDirectory) throws FileNotFoundException {
    super(outputDirectory);
    TreeFeatureVectorFeaturesEncoder myFeaturesEncoder = new TreeFeatureVectorFeaturesEncoder();
    myFeaturesEncoder.addEncoder(new NumberEncoder());
    myFeaturesEncoder.addEncoder(new BooleanEncoder());
    myFeaturesEncoder.addEncoder(new StringEncoder());
    this.setFeaturesEncoder(myFeaturesEncoder);
    this.setOutcomeEncoder(new BooleanToBooleanOutcomeEncoder());
  }

  /**
   * creates and formats a string (without the outcome) for a specific feature vector. Public so
   * that the one versus all data writer may use it, not for casual use.
   * 
   * @param features
   *          The featuers to write into a string format.
   * @return The string that represents the features in a format that tk_svm_classify can utilize.
   */
  public static String createString(TreeFeatureVector features) throws CleartkProcessingException {
    StringBuffer output = new StringBuffer();

    for (String tree : features.getTrees().values()) {
      output.append(String.format(Locale.US, " |BT| %s", tree));
    }
    if (!features.getTrees().isEmpty()) {
      output.append(" |ET|");
    }

    for (FeatureVector.Entry entry : features.getFeatures()) {
      if (Double.isInfinite(entry.value) || Double.isNaN(entry.value))
        throw CleartkEncoderException.invalidFeatureVectorValue(entry.index, entry.value);
      output.append(String.format(Locale.US, " %d:%.7f", entry.index, entry.value));
    }

    return output.toString();
  }
  
  /**
   * Write a line representing the feature vector with the appropriate outcome to be used by the
   * tk_svm_classify command.
   * 
   * @param features
   *          The feature vector to be written.
   * @param outcome
   *          The correct classification for that feature vector.
   */
  @Override
  public void writeEncoded(TreeFeatureVector features, Boolean outcome)
      throws CleartkProcessingException {
    StringBuffer output = new StringBuffer();

    if (outcome == null) {
      output.append("0");
    } else if (outcome.booleanValue()) {
      output.append("+1");
    } else {
      output.append("-1");
    }

    output.append(createString(features));

    this.trainingDataWriter.println(output);
  }

  @Override
  protected TKSVMlightBooleanOutcomeClassifierBuilder newClassifierBuilder() {
    return new TKSVMlightBooleanOutcomeClassifierBuilder();
  }
}
