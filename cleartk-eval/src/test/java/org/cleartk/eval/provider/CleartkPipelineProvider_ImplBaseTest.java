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

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Steven Bethard
 */
public class CleartkPipelineProvider_ImplBaseTest {

  public static class Provider extends CleartkPipelineProvider_ImplBase {

    @Override
    public List<AnalysisEngine> getTrainingPipeline(String name) throws UIMAException {
      return null;
    }

    @Override
    public void train(String name, String... trainingArguments) throws Exception {
    }

    @Override
    public List<AnalysisEngine> getClassifyingPipeline(String name) throws UIMAException {
      return null;
    }

  }

  public static class Annotator extends JCasAnnotator_ImplBase {

    public static boolean IS_COMPLETE = false;

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
      super.collectionProcessComplete();
      IS_COMPLETE = true;
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
    }

  }

  @Test
  public void test() throws Exception {
    List<AnalysisEngine> pipeline = Arrays.asList(AnalysisEngineFactory.createPrimitive(Annotator.class));

    Provider provider = new Provider();

    Annotator.IS_COMPLETE = false;
    provider.trainingPipelineComplete("<foo>", pipeline);
    Assert.assertTrue(Annotator.IS_COMPLETE);

    Annotator.IS_COMPLETE = false;
    provider.classifyingPipelineComplete("<foo>", pipeline);
    Assert.assertTrue(Annotator.IS_COMPLETE);

  }
}
