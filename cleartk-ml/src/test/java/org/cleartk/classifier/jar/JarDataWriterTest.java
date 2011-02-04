/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.util.FileUtils;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.features.NameNumberFeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.StringToStringOutcomeEncoder;
import org.cleartk.classifier.test.DefaultStringTestDataWriterFactory;
import org.cleartk.classifier.test.StringTestDataWriter;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.UimaContextFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */
public class JarDataWriterTest extends DefaultTestBase {

  @Test
  public void testManifest() throws Throwable {
    String expectedManifest = ("Manifest-Version: 1.0\n"
        + "classifierBuilderClass: org.cleartk.classifier.test.StringTestClassifi\n" + " erBuilder");

    StringTestDataWriter dataWriter = new StringTestDataWriter(outputDirectory);
    dataWriter.setFeaturesEncoder(new NameNumberFeaturesEncoder(false, false));
    dataWriter.setOutcomeEncoder(new StringToStringOutcomeEncoder());
    dataWriter.finish();
    File manifestFile = new File(outputDirectory, "MANIFEST.MF");
    String actualManifest = FileUtils.file2String(manifestFile);
    Assert.assertEquals(expectedManifest, actualManifest.replaceAll("\r", "").trim());
  }

  @Test
  public void testFinish() throws Throwable {

    UimaContext uimaContext = UimaContextFactory.createUimaContext(
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectoryName);
    DefaultStringTestDataWriterFactory factory = new DefaultStringTestDataWriterFactory();
    factory.initialize(uimaContext);
    DataWriter<String> dataWriter = factory.createDataWriter();
    dataWriter.finish();
    assertTrue(new File(outputDirectory, FeaturesEncoder_ImplBase.ENCODERS_FILE_NAME).exists());

  }

}
