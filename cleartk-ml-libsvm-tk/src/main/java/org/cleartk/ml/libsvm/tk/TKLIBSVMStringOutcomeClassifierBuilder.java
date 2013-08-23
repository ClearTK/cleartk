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
package org.cleartk.ml.libsvm.tk;

import java.io.File;

import org.cleartk.classifier.tksvmlight.TreeKernelSVMStringOutcomeClassifierBuilder;

/**
 * A class that provided interfaces to package and unpackage a
 * {@link TKLIBSVMStringOutcomeClassifier} into a jar file.
 * 
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @author Tim Miller
 */
public class TKLIBSVMStringOutcomeClassifierBuilder
    extends
    TreeKernelSVMStringOutcomeClassifierBuilder {

  TKLIBSVMBooleanOutcomeClassifierBuilder builder = new TKLIBSVMBooleanOutcomeClassifierBuilder();
  
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
      if (file.getName().matches("training-data-\\d+.libsvm")) {
        builder.trainClassifier(file, args);
      }
    }
  }
  
  @Override
  public String getPackageName() {
    return "libsvm";
  }
}
