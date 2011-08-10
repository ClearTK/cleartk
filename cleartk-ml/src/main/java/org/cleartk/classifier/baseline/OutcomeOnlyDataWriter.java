/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.classifier.baseline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.DirectoryDataWriter;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class OutcomeOnlyDataWriter<CLASSIFIER_BUILDER extends SingleOutcomeClassifierBuilder<OUTCOME_TYPE>, OUTCOME_TYPE>
    extends DirectoryDataWriter<CLASSIFIER_BUILDER, SingleOutcomeClassifier<OUTCOME_TYPE>>
    implements DataWriter<OUTCOME_TYPE> {

  protected File trainingDataFile;

  protected PrintWriter trainingDataWriter;

  public OutcomeOnlyDataWriter(File outputDirectory) throws FileNotFoundException {
    super(outputDirectory);
    this.trainingDataFile = this.classifierBuilder.getTrainingDataFile(this.outputDirectory);
    this.trainingDataWriter = new PrintWriter(this.trainingDataFile);
  }

  @Override
  public void write(Instance<OUTCOME_TYPE> instance) throws CleartkProcessingException {
    this.trainingDataWriter.println(instance.getOutcome());
  }

  @Override
  public void finish() throws CleartkProcessingException {
    this.trainingDataWriter.close();
    super.finish();
  }

}
