/** 
 * Copyright (c) 2007-2012, Regents of the University of Colorado 
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
package org.cleartk.summarization.classifier;

import java.io.File;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.InstanceDataWriter;
import org.cleartk.classifier.jar.DirectoryDataWriter;
import org.cleartk.summarization.SummarizationModel_ImplBase;

public abstract class SummarizationDataWriter<MODEL_TYPE extends SummarizationModel_ImplBase, CLASSIFIER_BUILDER_TYPE extends SummarizationClassifierBuilder<MODEL_TYPE>>
    extends DirectoryDataWriter<CLASSIFIER_BUILDER_TYPE, SummarizationClassifier<MODEL_TYPE>>
    implements DataWriter<Boolean> {

  protected InstanceDataWriter<Boolean> instanceDataWriter;

  public SummarizationDataWriter(File outputDirectory) {
    super(outputDirectory);
    this.instanceDataWriter = new InstanceDataWriter<Boolean>(outputDirectory);
  }

  @Override
  public void write(Instance<Boolean> instance) throws CleartkProcessingException {
    this.instanceDataWriter.write(instance);
  }

  @Override
  public void finish() throws CleartkProcessingException {
    super.finish();
  }

}
