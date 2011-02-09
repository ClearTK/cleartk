/* 
 * This file was copied from edu.umn.biomedicus.evaluation.EngineFactory in the Biomedicus project (see http://biomedicus.googlecode.com).
 * The original file is made available under the ASL 2.0 with the following text:

 
 Copyright 2010 University of Minnesota  
 All rights reserved. 

 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 

 http://www.apache.org/licenses/LICENSE-2.0 

 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */

package org.cleartk.evaluation.factory;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * 
 * @author Philip Ogren
 * 
 */

public interface EvaluationFactory {

  /**
   * This method creates an aggregate analysis engine that performs task-specific evaluation using
   * the gold-standard data which should be found in the {@link ViewNames#GOLD_VIEW} view against
   * the system-generated data which should be found in the {@link ViewNames#SYSTEM_VIEW}. All
   * results should be written to the evaluation directory provided. This directory will generally
   * be specific to either a fold or the holdout evaluation.
   * 
   * @param evaluationDirectory
   *          a directory where all evaluation results should be written to.
   * @return
   * @throws ResourceInitializationException
   */
  public AnalysisEngine getEvaluationAggregate(String name) throws ResourceInitializationException;

  /**
   * This method aggregates results from a number of directories and writes the aggregated results
   * to the output directory.
   * 
   * @param evaluationDirectories
   *          a list of evaluation directories that correspond directly to the evaluation
   *          directories passed into multiple calls to {@link #getEvaluationAggregate(File)}
   * @param outputDirectory
   *          a directory where aggregated results are written to.
   * @throws Exception
   */
  public void aggregateResults() throws Exception;

  public void complete(String name);

}
