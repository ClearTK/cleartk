package org.cleartk.ml.libsvm.tk;

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
import org.cleartk.classifier.tksvmlight.TreeFeatureVector;
import org.cleartk.classifier.tksvmlight.TreeFeatureVectorFeaturesEncoder;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * The data writer for tree kernel svm light.
 * 
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @author Tim Miller
 */
public class TKLIBSVMBooleanOutcomeDataWriter extends 
  DataWriter_ImplBase<TKLIBSVMBooleanOutcomeClassifierBuilder, TreeFeatureVector, Boolean,Boolean> {
  
  /**
   * Constructor for the Tree Kernel LIBSVM data writer.
   * 
   * @param outputDirectory
   *          The directory the data/files should be written within.
   */
  public TKLIBSVMBooleanOutcomeDataWriter(File outputDirectory)
      throws FileNotFoundException {
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
   * @throws CleartkEncoderException 
   */
  public static Object createString(TreeFeatureVector features) throws CleartkEncoderException {
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

  @Override
  protected void writeEncoded(TreeFeatureVector features, Boolean outcome)
      throws CleartkProcessingException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected TKLIBSVMBooleanOutcomeClassifierBuilder newClassifierBuilder() {
    // TODO Auto-generated method stub
    return null;
  }

}
