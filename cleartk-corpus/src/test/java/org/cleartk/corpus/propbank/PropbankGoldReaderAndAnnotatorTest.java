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
package org.cleartk.corpus.propbank;

import static org.junit.Assert.assertNotNull;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.test.CleartkTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class PropbankGoldReaderAndAnnotatorTest extends CleartkTestBase {

  @Test
  public void testReaderDescriptor() throws UIMAException {
    CollectionReader reader;

    ResourceInitializationException rie = null;
    try {
      reader = CollectionReaderFactory.createCollectionReader(PropbankGoldReader.class);
    } catch (ResourceInitializationException e) {
      rie = e;
    }
    assertNotNull(rie);

    rie = null;
    try {
      reader = CollectionReaderFactory.createCollectionReader(
          PropbankGoldReader.class,
          PropbankGoldReader.PARAM_WSJ_SECTIONS,
          "02-21");
    } catch (ResourceInitializationException e) {
      rie = e;
    }
    assertNotNull(rie);

    try {
      reader = CollectionReaderFactory.createCollectionReader(
          PropbankGoldReader.class,
          PropbankGoldReader.PARAM_WSJ_SECTIONS,
          "02-21",
          PropbankGoldReader.PARAM_PROPBANK_FILE_NAME,
          "src/test/resources/data/propbank-1.0/prop.txt");
    } catch (ResourceInitializationException e) {
      rie = e;
    }
    assertNotNull(rie);

    reader = CollectionReaderFactory.createCollectionReader(
        PropbankGoldReader.class,
        PropbankGoldReader.PARAM_WSJ_SECTIONS,
        "02-21",
        PropbankGoldReader.PARAM_PROPBANK_FILE_NAME,
        "src/test/resources/data/propbank-1.0/prop.txt",
        PropbankGoldReader.PARAM_PENNTREEBANK_DIRECTORY_NAME,
        "src/test/resources/data/propbank-1.0/treebank");

    Object propbankCorpusFile = reader.getConfigParameterValue(PropbankGoldReader.PARAM_PROPBANK_FILE_NAME);
    Assert.assertEquals("src/test/resources/data/propbank-1.0/prop.txt", propbankCorpusFile);

    Object treebankCorpusDirectory = reader.getConfigParameterValue(PropbankGoldReader.PARAM_PENNTREEBANK_DIRECTORY_NAME);
    Assert.assertEquals("src/test/resources/data/propbank-1.0/treebank", treebankCorpusDirectory);

    Object wsjSections = reader.getConfigParameterValue(PropbankGoldReader.PARAM_WSJ_SECTIONS);
    Assert.assertEquals("02-21", wsjSections);

  }

  @Test
  public void testAnnotatorDescriptor() throws UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(PropbankGoldAnnotator.class);
    engine.collectionProcessComplete();
  }

}
