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

package org.cleartk.eval.provider.corpus;

import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.test.DefaultTestBase;
import org.junit.Test;
import org.uimafit.component.JCasAnnotatorAdapter;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;

/**
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class XmiCorpusFactoryTest extends DefaultTestBase {

  @Test
  public void testCorpusCounts() throws Exception {
    XmiTestCorpusFactory testFactory = new XmiTestCorpusFactory(typeSystemDescription);
    testCollectionReaderCount(testFactory.getReader(), 11); // 11 files total (8+3 see next 2
                                                            // lines)
    testCollectionReaderCount(testFactory.getTrainReader(), 8); // 8 files in training set
    testCollectionReaderCount(testFactory.getTestReader(), 3); // 3 files in test set
    // all of the following pairs should add up to 8 because the union of the training and test sets
    // for a given fold should be the collection's training set
    // fold1
    testCollectionReaderCount(testFactory.getTestReader(1), 3);
    testCollectionReaderCount(testFactory.getTrainReader(1), 5);
    // fold2
    testCollectionReaderCount(testFactory.getTestReader(2), 1);
    testCollectionReaderCount(testFactory.getTrainReader(2), 7);
    // fold3
    testCollectionReaderCount(testFactory.getTestReader(3), 2);
    testCollectionReaderCount(testFactory.getTrainReader(3), 6);
    // fold4
    testCollectionReaderCount(testFactory.getTestReader(4), 2);
    testCollectionReaderCount(testFactory.getTrainReader(3), 6);

    testCollectionReaderCount(testFactory.createReader(1, 2, 3), 6);
    testCollectionReaderCount(testFactory.createReader(1, 2), 4);
    testCollectionReaderCount(testFactory.createReader(2, 4), 3);

  }

  @Test
  public void testXmiCorpusFactory() throws Exception {
    XmiTestCorpusFactory testFactory = new XmiTestCorpusFactory(typeSystemDescription);
    CollectionReader reader = testFactory.getTestReader(1);

    AnalysisEngine aeAdapter = AnalysisEngineFactory.createPrimitive(
        JCasAnnotatorAdapter.class,
        typeSystemDescription);

    JCasIterable jCases = new JCasIterable(reader, aeAdapter);
    int i = 0;
    for (JCas jcs : jCases) {
      if (i == 0) {
        assertEquals("This is a test.", jcs.getDocumentText());
      } else if (i == 1) {
        assertEquals("A B C D", jcs.getDocumentText());
      } else if (i == 2) {
        assertEquals("1 2 3 4", jcs.getDocumentText());
      }
      i++;
    }

  }

  public static void main(String[] args) throws Exception {
    XmiCorpusFactoryTest xcft = new XmiCorpusFactoryTest();
    xcft.buildTestXmiFiles();
  }

  private void buildTestXmiFiles() throws Exception {
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
        XWriter.class,
        typeSystemDescription,
        XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        "src/test/resources/eval/provider/corpus/xmi-factory-test-data/xmi");
    super.setUp();

    tokenBuilder.buildTokens(jCas, "This is a test.", "This is a test .", "t1 t2 t3 t4 t5");
    SimplePipeline.runPipeline(jCas, xWriter); // 1.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "A B C D", "A B C D", "t1 t2 t3 t4");
    SimplePipeline.runPipeline(jCas, xWriter); // 2.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "1 2 3 4", "1 2 3 4", "tA tB tC tD");
    SimplePipeline.runPipeline(jCas, xWriter); // 3.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "1 2 3 4", "1 2 3 4", "tA tB tC tD");
    SimplePipeline.runPipeline(jCas, xWriter); // 4.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "10 20 30 40 50", "10 20 30 40 50", "1 2 3 4 5");
    SimplePipeline.runPipeline(jCas, xWriter); // 5.xmi
    jCas.reset();
    tokenBuilder.buildTokens(
        jCas,
        "first sentence. second sentence.",
        "first sentence . \n second sentence .",
        "1 2 3 1 2 3");
    SimplePipeline.runPipeline(jCas, xWriter); // 6.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "A1 A1 B2 B2", "A1 A1 B2 B2", "A A B B");
    SimplePipeline.runPipeline(jCas, xWriter); // 7.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "AAAA", "A A A A", "1 2 3 4");
    SimplePipeline.runPipeline(jCas, xWriter); // 8.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "AA AA", "AA AA", "1 2");
    SimplePipeline.runPipeline(jCas, xWriter); // 9.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "A", "A", "1");
    SimplePipeline.runPipeline(jCas, xWriter); // 10.xmi
    jCas.reset();
    tokenBuilder.buildTokens(jCas, "b", "b", "BB1");
    SimplePipeline.runPipeline(jCas, xWriter); // 11.xmi

  }
}
