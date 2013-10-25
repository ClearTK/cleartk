/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.libsvm;

import java.io.File;
import java.io.IOException;

import org.cleartk.classifier.encoder.CleartkEncoderException;
import org.cleartk.classifier.encoder.outcome.BooleanToBooleanOutcomeEncoder;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 */

public class LibSvmBooleanOutcomeDataWriter extends
    LibSvmDataWriter<LibSvmBooleanOutcomeClassifierBuilder, Boolean, Boolean, libsvm.svm_model> {

  public LibSvmBooleanOutcomeDataWriter(File outputDirectory) throws IOException {
    super(outputDirectory);
    this.setOutcomeEncoder(new BooleanToBooleanOutcomeEncoder());
  }

  @Override
  protected String encode(Boolean outcome) throws CleartkEncoderException {
    return this.classifierBuilder.getOutcomeEncoder().encode(outcome).booleanValue() ? "+1" : "-1";
  }

  @Override
  protected LibSvmBooleanOutcomeClassifierBuilder newClassifierBuilder() {
    return new LibSvmBooleanOutcomeClassifierBuilder();
  }

}
