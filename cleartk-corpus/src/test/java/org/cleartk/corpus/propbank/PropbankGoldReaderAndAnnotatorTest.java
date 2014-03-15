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

import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.corpus.penntreebank.TreebankGoldAnnotator;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.test.util.CleartkTestBase;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

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
      reader = CollectionReaderFactory.createReader(PropbankGoldReader.class);
    } catch (ResourceInitializationException e) {
      rie = e;
    }
    assertNotNull(rie);

    rie = null;
    try {
      reader = CollectionReaderFactory.createReader(
          PropbankGoldReader.class,
          PropbankGoldReader.PARAM_WSJ_SECTIONS,
          "02-21");
    } catch (ResourceInitializationException e) {
      rie = e;
    }
    assertNotNull(rie);

    try {
      reader = CollectionReaderFactory.createReader(
          PropbankGoldReader.class,
          PropbankGoldReader.PARAM_WSJ_SECTIONS,
          "02-21",
          PropbankGoldReader.PARAM_PROPBANK_FILE_NAME,
          "src/test/resources/data/propbank-1.0/prop.txt");
    } catch (ResourceInitializationException e) {
      rie = e;
    }
    assertNotNull(rie);

    reader = CollectionReaderFactory.createReader(
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
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(PropbankGoldAnnotator.class);
    engine.collectionProcessComplete();
  }

  @Test
  public void testAnnotator() throws Exception {
    // Issue 385
    String treeText = Resources.toString(
        PropbankGoldReaderAndAnnotatorTest.class.getResource("/org/cleartk/corpus/treebank/wsj_0015.mrg"),
        Charsets.US_ASCII);
    String propText = "wsj/00/wsj_0015.mrg 1 12 gold order.01 p---p 11:1-ARGM-TMP 12:0-rel 10:0*13:0-ARG1 14:1-ARG0-by";
    this.jCas.createView(PennTreebankReader.TREEBANK_VIEW);
    this.jCas.getView(PennTreebankReader.TREEBANK_VIEW).setDocumentText(treeText);
    this.jCas.createView(PropbankConstants.PROPBANK_VIEW);
    this.jCas.getView(PropbankConstants.PROPBANK_VIEW).setDocumentText(propText);
    
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(TreebankGoldAnnotator.getDescription());
    builder.add(AnalysisEngineFactory.createEngineDescription(PropbankGoldAnnotator.class));
    AnalysisEngine engine = builder.createAggregate();
    engine.process(this.jCas);
    Iterator<SemanticArgument> argsIter = JCasUtil.select(this.jCas, SemanticArgument.class).iterator();
    SemanticArgument argmTmp = argsIter.next();
    Assert.assertEquals("previously", argmTmp.getCoveredText());
    Assert.assertEquals("ARGM", argmTmp.getLabel());
    Assert.assertEquals("TMP", argmTmp.getFeature());
    Assert.assertEquals(null, argmTmp.getPreposition());
    // skip "rel" -- should this even be a SemanticArgument? seems like a bug to me
    argsIter.next();
    SemanticArgument arg1 = argsIter.next();
    // note that ARG1 only refers to "-NONE-" annotations so it should be empty
    // but it should also be here in the iterator if its offsets are set correctly
    Assert.assertEquals("", arg1.getCoveredText());
    Assert.assertEquals("ARG1", arg1.getLabel());
    Assert.assertEquals(null, arg1.getFeature());
    Assert.assertEquals(null, arg1.getPreposition());
    SemanticArgument arg0 = argsIter.next();
    Assert.assertEquals("by the Illinois Commerce Commission", arg0.getCoveredText());
    Assert.assertEquals("ARG0", arg0.getLabel());
    Assert.assertEquals(null, arg0.getFeature());
    Assert.assertEquals("by", arg0.getPreposition());
    engine.collectionProcessComplete();
  }
}
