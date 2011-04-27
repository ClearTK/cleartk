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

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * A simple {@link EvaluationPipelineProvider} that equates the folds in a cross-validation with
 * {@link AnalysisEngine} batches, calling {@link AnalysisEngine#batchProcessComplete()} at the end
 * of each fold, and {@link AnalysisEngine#collectionProcessComplete()} at the end of the
 * cross-validation. This approach allows a single annotator, like {@link AnnotationEvaluator}, to
 * aggregate evaluation metrics over all the folds of a cross-validation.
 * 
 * In contrast, approaches that equate cross-validation folds with full collections instead of
 * batches require a new annotator for each fold, and some serialization to and from disk to merge
 * evaluation metrics across folds.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class BatchBasedEvaluationPipelineProvider implements EvaluationPipelineProvider {

  private List<AnalysisEngine> engines;

  public BatchBasedEvaluationPipelineProvider(List<AnalysisEngine> engines) {
    this.engines = engines;
  }

  public BatchBasedEvaluationPipelineProvider(AnalysisEngine... engines) {
    this(Arrays.asList(engines));
  }

  @Override
  public List<AnalysisEngine> getEvaluationPipeline(String name)
      throws ResourceInitializationException {
    return this.engines;
  }

  @Override
  public void evaluationPipelineComplete(String name, List<AnalysisEngine> evaluationEngines)
      throws AnalysisEngineProcessException {
    for (AnalysisEngine engine : evaluationEngines) {
      engine.batchProcessComplete();
    }
  }

  @Override
  public void evaluationComplete() throws AnalysisEngineProcessException {
    for (AnalysisEngine engine : this.engines) {
      engine.collectionProcessComplete();
    }
  }
}
