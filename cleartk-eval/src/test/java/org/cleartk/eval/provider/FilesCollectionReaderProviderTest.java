/*
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
package org.cleartk.eval.provider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.util.CasCreationUtils;
import org.cleartk.eval.EvaluationTestBase;
import org.cleartk.util.ViewURIUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Steven Bethard
 */
public class FilesCollectionReaderProviderTest extends EvaluationTestBase {

  @Test
  public void test() throws Exception {
    List<String> trainNames = Arrays.asList("1", "2", "3", "4", "5");
    List<String> testNames = Arrays.asList("6", "7", "8");
    this.createFiles(trainNames);
    this.createFiles(testNames);
    FilesCollectionReaderProvider provider = new FilesCollectionReaderProvider(
        this.outputDirectory,
        trainNames,
        testNames);

    this.checkNames(provider.getReader(), "1", "2", "3", "4", "5", "6", "7", "8");
    this.checkNames(provider.getTrainReader(), "1", "2", "3", "4", "5");
    this.checkNames(provider.getTestReader(), "6", "7", "8");
    provider.setNumberOfFolds(3);
    this.checkNames(provider.getTestReader(0), "1", "4");
    this.checkNames(provider.getTrainReader(0), "2", "3", "5");
    this.checkNames(provider.getTestReader(1), "2", "5");
    this.checkNames(provider.getTrainReader(1), "1", "3", "4");
    this.checkNames(provider.getTestReader(2), "3");
    this.checkNames(provider.getTrainReader(2), "1", "2", "4", "5");

  }

  private void createFiles(List<String> fileNames) throws IOException {
    for (String name : fileNames) {
      File file = new File(this.outputDirectory, name);
      FileUtils.writeStringToFile(file, name);
    }
  }

  private void checkNames(CollectionReader reader, String... expectedNames) throws Exception {
    CAS cas = CasCreationUtils.createCas(Arrays.asList(reader.getMetaData()));
    int i = 0;
    while (reader.hasNext()) {
      cas.reset();
      reader.getNext(cas);
      URI expected = new File(this.outputDirectory, expectedNames[i]).toURI();
      URI actual = ViewURIUtil.getURI(cas.getJCas());
      Assert.assertEquals(expected, actual);
      i += 1;
    }
    Assert.assertEquals(expectedNames.length, i);
  }
}
