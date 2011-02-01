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

package org.cleartk.classifier.mallet;

import java.io.IOException;
import java.util.List;

import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.outcome.StringToStringOutcomeEncoder;
import org.cleartk.classifier.jar.SequentialDataWriterFactory_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren, Philipp Wetzler
 * 
 */

public class DefaultMalletCRFDataWriterFactory extends
    SequentialDataWriterFactory_ImplBase<List<NameNumber>, String, String> {

  public static final String PARAM_COMPRESS = ConfigurationParameterFactory
      .createConfigurationParameterName(DefaultMalletCRFDataWriterFactory.class, "compress");

  @ConfigurationParameter(description = "indicates whether the FeaturesEncoder should compress the feature names", defaultValue = "false")
  private boolean compress;

  public static final String PARAM_SORT = ConfigurationParameterFactory
      .createConfigurationParameterName(DefaultMalletCRFDataWriterFactory.class, "sort");

  @ConfigurationParameter(description = "indicates that the FeaturesEncoder should write the feature names in sorted order", defaultValue = "false")
  private boolean sort;

  public SequentialDataWriter<String> createSequentialDataWriter() throws IOException {
    MalletCRFDataWriter mdw = new MalletCRFDataWriter(outputDirectory);

    if (!this.setEncodersFromFileSystem(mdw)) {
      NameNumberFeaturesEncoder fe = new NameNumberFeaturesEncoder(compress, sort);
      fe.addEncoder(new NumberEncoder());
      fe.addEncoder(new BooleanEncoder());
      fe.addEncoder(new StringEncoder());
      mdw.setFeaturesEncoder(fe);

      mdw.setOutcomeEncoder(new StringToStringOutcomeEncoder());
    }

    return mdw;
  }

}
