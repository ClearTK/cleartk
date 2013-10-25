/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.classifier.jar;

import java.io.IOException;

import org.cleartk.classifier.SequenceDataWriter;
import org.cleartk.classifier.SequenceDataWriterFactory;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

/**
 * A {@link SequenceDataWriterFactory} that creates a data writer from the class given by
 * {@link #PARAM_DATA_WRITER_CLASS_NAME} and the directory given by {@link #PARAM_OUTPUT_DIRECTORY}.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class DefaultSequenceDataWriterFactory<OUTCOME_TYPE> extends
    GenericDataWriterFactory<OUTCOME_TYPE> implements SequenceDataWriterFactory<OUTCOME_TYPE> {

  public static final String PARAM_DATA_WRITER_CLASS_NAME = "dataWriterClassName";

  @ConfigurationParameter(
      name = PARAM_DATA_WRITER_CLASS_NAME,
      mandatory = true,
      description = "provides the full name of the data writer class to be used.")
  private String dataWriterClassName;

  @Override
  @SuppressWarnings({ "unchecked" })
  public SequenceDataWriter<OUTCOME_TYPE> createDataWriter() throws IOException {
    return this.createDataWriter(this.dataWriterClassName, SequenceDataWriter.class);
  }

}
