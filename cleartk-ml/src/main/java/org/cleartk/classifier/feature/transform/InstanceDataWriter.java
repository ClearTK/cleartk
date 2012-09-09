/** 
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

package org.cleartk.classifier.feature.transform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Instance;

/**
 * 
 * <br>
 * Copyright (c) 2011-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * This training data (instance) consumer produces intermediate storage of instances for later use
 * in feature transformation (i.e. normalization, clustering, etc...)
 * 
 * @author Lee Becker
 * 
 * @param <OUTCOME_T>
 *          - The outcome type of the instances it writes out
 */
public class InstanceDataWriter<OUTCOME_T> implements DataWriter<OUTCOME_T> {

  ObjectOutputStream objout;

  public static String INSTANCES_OUTPUT_FILENAME = "training-data.instances";

  public InstanceDataWriter(File outputDirectory) throws IOException {
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    // Initialize Object Serializer
    File outputFile = new File(outputDirectory, INSTANCES_OUTPUT_FILENAME);
    this.objout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
  }

  @Override
  public void write(Instance<OUTCOME_T> instance) throws CleartkProcessingException {
    try {
      this.objout.writeObject(instance);
    } catch (IOException e) {
      throw new CleartkProcessingException("", "Unable to write Instance", e);
    }
  }

  @Override
  public void finish() throws CleartkProcessingException {
    try {
      // We need to add a "null" instance terminator to gracefully handle the iteration while
      // reading in serialized objects from file
      InstanceStream.Terminator<OUTCOME_T> terminator = new InstanceStream.Terminator<OUTCOME_T>();
      this.objout.writeObject(terminator);

      this.objout.close();
    } catch (IOException e) {
      throw new CleartkProcessingException("", "Unable to write terminal instance", e);
    }

  }

}
