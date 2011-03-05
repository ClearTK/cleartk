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

package org.cleartk.eval;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.admin.CASAdminException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.eval.provider.CleartkPipelineProvider;
import org.cleartk.eval.provider.CorpusReaderPipeline;
import org.cleartk.eval.provider.EvaluationPipelineProvider;

/**
 * 
 * @author Philip Ogren
 * 
 */

public interface Evaluator {

  // private static Logger logger = UIMAFramework.getLogger(Evaluation.class);

  /**
   * This method runs a cross-validation experiment. For each fold, a new model will be created
   * using all of the training data except that which is in the current fold. The model is then
   * evaluated against the data in the fold. After all folds have been evaluated in this way, the
   * evaluation results are aggregated so that overall performance for the experiment is given.
   * 
   * @param corpusReaderPipeline
   *          A CorpusReaderPipeline provides collection readers corresponding to training data and
   *          testing data for each fold. Please see {@link CorpusReaderPipeline}.
   * @param cleartkPipelineProvider
   *          An CleartkPipelineProvider provides aggregate analysis engines that process training
   *          data to generate a training data file suitable for the machine learning library that
   *          is being used, train a model using the training data file, and test the model using
   *          the testing data for a given fold. See {@link CleartkPipelineProvider}.
   * @param evaluationPipelineProvider
   *          An EvaluationPipelineProvider provides methods for creating aggregate analysis engines
   *          which perform evaluation. Generally, this involves comparing the contents of the gold
   *          view with the contents of the system view and collecting performance numbers (i.e.
   *          true positives, false positives) and then generating and writing out evaluation
   *          results to the output directory.
   * @param trainingArguments
   *          The training arguments are arguments that are passed directly to the machine learning
   *          library that is being used for learning a model when the model learner is invoked.
   *          Each machine learning library has different training arguments. These are generally
   *          available by running the machine learning libraries learner from the command line
   *          without any arguments which will print evalOut a usage message including the training
   *          arguments specific to that learner.
   * @throws Exception
   */

  public void runCrossValidation(
      CorpusReaderPipeline corpusReaderPipeline,
      CleartkPipelineProvider cleartkPipelineProvider,
      EvaluationPipelineProvider evaluationPipelineProvider,
      String... trainingArguments) throws Exception;

  void train(
      String runName,
      CollectionReader trainingReader,
      AnalysisEngine preprocessing,
      CleartkPipelineProvider cleartkPipelineProvider,
      String... trainingArguments) throws Exception;

  void evaluate(
      String runName,
      CollectionReader testingReader,
      AnalysisEngine preprocessing,
      CleartkPipelineProvider cleartkPipelineProvider,
      EvaluationPipelineProvider evaluationPipelineProvider) throws Exception;

  public void runPipeline(CollectionReader reader, List<AnalysisEngine> pipeline)
      throws ResourceInitializationException, AnalysisEngineProcessException, CollectionException,
      CASAdminException, IOException;

  /**
   * This method runs "holdout" evaluation on your gold-standard data - i.e. it trains a model on
   * your training data and tests it using the holdout evaluation data.
   * 
   * @param corpusReaderPipeline
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param cleartkPipelineProvider
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param evaluationPipelineProvider
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @throws Exception
   */
  public void runHoldoutEvaluation(
      CorpusReaderPipeline corpusReaderPipeline,
      CleartkPipelineProvider cleartkPipelineProvider,
      EvaluationPipelineProvider evaluationPipelineProvider,
      String... trainingArguments) throws Exception;

  /**
   * This method builds a model using your entire corpus. You should call this method after you have
   * completed your experimentation and you now want to build a model that will be used in your
   * applications.
   * 
   * @param corpusReaderPipeline
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param cleartkPipelineProvider
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @throws Exception
   */
  public void buildCorpusModel(
      String name,
      CorpusReaderPipeline corpusReaderPipeline,
      CleartkPipelineProvider cleartkPipelineProvider,
      String... trainingArguments) throws Exception;
}
