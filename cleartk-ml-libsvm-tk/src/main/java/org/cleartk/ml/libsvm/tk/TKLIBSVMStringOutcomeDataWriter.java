package org.cleartk.ml.libsvm.tk;

import java.io.File;
import java.io.FileNotFoundException;

import org.cleartk.classifier.tksvmlight.TreeKernelSVMStringOutcomeDataWriter;


public class TKLIBSVMStringOutcomeDataWriter extends
TreeKernelSVMStringOutcomeDataWriter<TKLIBSVMStringOutcomeClassifierBuilder>{

  public TKLIBSVMStringOutcomeDataWriter(File outputDirectory)
      throws FileNotFoundException {
    super(outputDirectory);
  }

  @Override
  protected TKLIBSVMStringOutcomeClassifierBuilder newClassifierBuilder() {
    return new TKLIBSVMStringOutcomeClassifierBuilder();
  }
}
