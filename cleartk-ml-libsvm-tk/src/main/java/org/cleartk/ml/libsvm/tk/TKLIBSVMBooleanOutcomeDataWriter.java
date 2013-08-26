package org.cleartk.ml.libsvm.tk;

import java.io.File;
import java.io.FileNotFoundException;

import org.cleartk.classifier.tksvmlight.TreeKernelSVMBooleanOutcomeDataWriter;

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
  TreeKernelSVMBooleanOutcomeDataWriter {
  
  /**
   * Constructor for the Tree Kernel LIBSVM data writer.
   * 
   * @param outputDirectory
   *          The directory the data/files should be written within.
   */
  public TKLIBSVMBooleanOutcomeDataWriter(File outputDirectory)
      throws FileNotFoundException {
    super(outputDirectory);
  }
}
