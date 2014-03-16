/**
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Würzburg nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
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
package org.cleartk.ml.grmm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.cleartk.ml.encoder.features.BooleanEncoder;
import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.ml.encoder.features.NumberEncoder;
import org.cleartk.ml.encoder.features.StringEncoder;
import org.cleartk.ml.encoder.outcome.StringArrayToStringArrayEncoder;
import org.cleartk.ml.jar.SequenceDataWriter_ImplBase;

/**
 * <br>
 * Copyright (c) 2010, University of Würzburg <br>
 * All rights reserved.
 * <p>
 * 
 * @author Martin Toepfer
 */
public class GrmmDataWriter extends
    SequenceDataWriter_ImplBase<GrmmClassifierBuilder, List<NameNumber>, String[], String[]> {

  public GrmmDataWriter(File outputDirectory) throws IOException {
    super(outputDirectory);
    NameNumberFeaturesEncoder fe = new NameNumberFeaturesEncoder();
    fe.addEncoder(new NumberEncoder());
    fe.addEncoder(new BooleanEncoder());
    fe.addEncoder(new StringEncoder());
    this.setFeaturesEncoder(fe);
    this.setOutcomeEncoder(new StringArrayToStringArrayEncoder());
  }

  @Override
  public void writeEncoded(List<NameNumber> features, String[] outcome) {
    writeEncoded(features, outcome, this.trainingDataWriter);
  }

  @Override
  public void writeEndSequence() {
    this.trainingDataWriter.println();
  }

  @Override
  protected GrmmClassifierBuilder newClassifierBuilder() {
    return new GrmmClassifierBuilder();
  }

  public static void writeEncoded(List<NameNumber> features, String[] outcome, PrintWriter writer) {
    for (int i = 0; i < outcome.length; i++) {
      if (i > 0) {
        writer.print(" ");
      }
      writer.print(outcome[i]);
    }
    writer.print(" ");
    writer.print("---- ");
    // (command line interface only supports binary features)
    for (NameNumber nameNumber : features) {
      writer.print(nameNumber.name);
      writer.print(":");
      writer.print(nameNumber.number);
      writer.print(" ");
    }
    writer.println();
  }
}
