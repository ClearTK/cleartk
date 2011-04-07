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
package org.cleartk.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.admin.CASAdminException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.util.CasCreationUtils;
import org.cleartk.eval.provider.CleartkPipelineProvider;
import org.cleartk.eval.provider.CorpusReaderProvider;
import org.cleartk.eval.provider.EvaluationPipelineProvider;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public class Evaluation {

  // private static Logger logger = UIMAFramework.getLogger(Evaluation.class);

  /**
   * This method runs a cross-validation experiment. For each fold, a new model will be created
   * using all of the training data except that which is in the current fold. The model is then
   * evaluated against the data in the fold. After all folds have been evaluated in this way, the
   * evaluation results are aggregated so that overall performance for the experiment is given.
   * 
   * @param corpusReaderPipeline
   *          A CorpusReaderPipeline provides collection readers corresponding to training data and
   *          testing data for each fold. Please see {@link CorpusReaderProvider}.
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
      CorpusReaderProvider corpusReaderPipeline,
      CleartkPipelineProvider cleartkPipelineProvider,
      EvaluationPipelineProvider evaluationPipelineProvider,
      String... trainingArguments) throws Exception {

    int folds = corpusReaderPipeline.getNumberOfFolds();
    for (int fold = 0; fold < folds; fold++) {
      String foldName = createFoldName(fold, folds);

      train(
          foldName,
          corpusReaderPipeline.getTrainReader(fold),
          corpusReaderPipeline.getPreprocessor(),
          cleartkPipelineProvider,
          trainingArguments);
      evaluate(
          foldName,
          corpusReaderPipeline.getTestReader(fold),
          corpusReaderPipeline.getPreprocessor(),
          cleartkPipelineProvider,
          evaluationPipelineProvider);
    }

    cleartkPipelineProvider.trainingComplete();
    cleartkPipelineProvider.classifyingComplete();
    evaluationPipelineProvider.evaluationComplete();
  }

  public static String createFoldName(int fold, int totalFolds) {
    fold++;
    int totalPower = (int) Math.log10(totalFolds); // TOTAL POWERRRRRR!!!
    int foldPower = (int) Math.log10(fold);
    char[] zeros = new char[totalPower - foldPower];
    Arrays.fill(zeros, '0');
    return "fold-" + new String(zeros) + fold;
  }

  public void train(
      String runName,
      CollectionReader trainingReader,
      AnalysisEngine preprocessing,
      CleartkPipelineProvider cleartkPipelineProvider,
      String... trainingArguments) throws Exception {

    List<AnalysisEngine> trainingPipeline = cleartkPipelineProvider.getTrainingPipeline(runName);

    List<AnalysisEngine> pipeline = new ArrayList<AnalysisEngine>();
    pipeline.add(preprocessing);
    pipeline.addAll(trainingPipeline);

    runPipeline(trainingReader, pipeline);
    preprocessing.collectionProcessComplete();
    cleartkPipelineProvider.trainingPipelineComplete(runName, trainingPipeline);

    cleartkPipelineProvider.train(runName, trainingArguments);
  }

  public void evaluate(
      String runName,
      CollectionReader testingReader,
      AnalysisEngine preprocessing,
      CleartkPipelineProvider cleartkPipelineProvider,
      EvaluationPipelineProvider evaluationPipelineProvider) throws Exception {
    List<AnalysisEngine> classifyingPipeline = cleartkPipelineProvider.getClassifyingPipeline(runName);
    List<AnalysisEngine> evaluationPipeline = evaluationPipelineProvider.getEvaluationPipeline(runName);

    List<AnalysisEngine> pipeline = new ArrayList<AnalysisEngine>();
    pipeline.add(preprocessing);
    pipeline.addAll(classifyingPipeline);
    pipeline.addAll(evaluationPipeline);

    runPipeline(testingReader, pipeline);
    preprocessing.collectionProcessComplete();
    cleartkPipelineProvider.classifyingPipelineComplete(runName, classifyingPipeline);
    evaluationPipelineProvider.evaluationPipelineComplete(runName, evaluationPipeline);
  }

  public void runPipeline(CollectionReader reader, List<AnalysisEngine> pipeline)
      throws ResourceInitializationException, AnalysisEngineProcessException, CollectionException,
      CASAdminException, IOException {
    List<ResourceMetaData> metaData = new ArrayList<ResourceMetaData>();
    metaData.add(reader.getMetaData());
    for (Resource resource : pipeline) {
      metaData.add(resource.getMetaData());
    }

    CAS cas = CasCreationUtils.createCas(metaData);
    while (reader.hasNext()) {
      cas.reset();
      reader.getNext(cas);
      for (AnalysisEngine engine : pipeline) {
        engine.process(cas);
      }
    }
  }

  /**
   * This method runs "holdout" evaluation on your gold-standard data - i.e. it trains a model on
   * your training data and tests it using the holdout evaluation data.
   * 
   * @param corpusReaderPipeline
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderProvider, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param cleartkPipelineProvider
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderProvider, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param evaluationPipelineProvider
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderProvider, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderProvider, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @throws Exception
   */
  public void runHoldoutEvaluation(
      CorpusReaderProvider corpusReaderPipeline,
      CleartkPipelineProvider cleartkPipelineProvider,
      EvaluationPipelineProvider evaluationPipelineProvider,
      String... trainingArguments) throws Exception {

    String name = "holdout";
    train(
        name,
        corpusReaderPipeline.getTrainReader(),
        corpusReaderPipeline.getPreprocessor(),
        cleartkPipelineProvider,
        trainingArguments);
    cleartkPipelineProvider.trainingComplete();

    evaluate(
        name,
        corpusReaderPipeline.getTestReader(),
        corpusReaderPipeline.getPreprocessor(),
        cleartkPipelineProvider,
        evaluationPipelineProvider);
    cleartkPipelineProvider.classifyingComplete();
    evaluationPipelineProvider.evaluationComplete();
  }

  /**
   * This method builds a model using your entire corpus. You should call this method after you have
   * completed your experimentation and you now want to build a model that will be used in your
   * applications.
   * 
   * @param corpusReaderPipeline
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderProvider, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param cleartkPipelineProvider
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderProvider, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderProvider, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @throws Exception
   */
  public void buildCorpusModel(
      String name,
      CorpusReaderProvider corpusReaderPipeline,
      CleartkPipelineProvider cleartkPipelineProvider,
      String... trainingArguments) throws Exception {
    train(
        name,
        corpusReaderPipeline.getReader(),
        corpusReaderPipeline.getPreprocessor(),
        cleartkPipelineProvider,
        trainingArguments);
    cleartkPipelineProvider.trainingComplete();
  }
}
