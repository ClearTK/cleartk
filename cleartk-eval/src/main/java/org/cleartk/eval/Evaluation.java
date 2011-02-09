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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.jar.Train;
import org.cleartk.eval.provider.CleartkPipelineProvider;
import org.cleartk.eval.provider.CorpusReaderPipeline;
import org.cleartk.eval.provider.EvaluationPipelineProvider;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;

/**
 * 
 * @author Philip Ogren
 * 
 */

public class Evaluation {

  // private static Logger logger = UIMAFramework.getLogger(Evaluation.class);

  /**
   * This method runs a cross-validation experiment. For each fold, a new model will be created
   * using all of the training data except that which is in the current fold. The model is then
   * evaluated against the data in the fold. After all folds have been evaluated in this way, the
   * evaluation results are aggregated so that overall performance for the experiment is given.
   * 
   * @param corpusFactory
   *          A corpus factory provides methods for creating collection readers corresponding to
   *          training data and testing data for each fold. Please see {@link CorpusReaderPipeline}.
   * @param engineFactory
   *          An engine factory provides methods for creating aggregate analysis engines that
   *          process training data to generate a training data file suitable for the machine
   *          learning library that is being used, train a model using the training data file, and
   *          test the model using the testing data for a given fold. See
   *          {@link CleartkPipelineProvider}.
   * @param evaluationFactory
   *          An evaluation factory provides methods for creating aggregate analysis engines which
   *          perform evaluation. Generally, this involves comparing the contents of the gold view
   *          with the contents of the system view and collecting performance numbers (i.e. true
   *          positives, false positives) and then generating and writing evalOut evaluation results
   *          to the output directory.
   * @param trainingArguments
   *          The training arguments are arguments that are passed directly to the machine learning
   *          library that is being used for learning a model when the model learner is invoked.
   *          Each machine learning library has different training arguments. These are generally
   *          available by running the machine learning libraries learner from the command line
   *          without any arguments which will print evalOut a usage message including the training
   *          arguments specific to that learner.
   * @throws Exception
   */

  public static void runCrossValidation(
      CorpusReaderPipeline corpusFactory,
      CleartkPipelineProvider engineFactory,
      EvaluationPipelineProvider evaluationFactory,
      String... trainingArguments) throws Exception {

    int folds = corpusFactory.numberOfFolds();
    for (int fold = 1; fold <= folds; fold++) {
      String foldName = createFoldName(fold, folds);

      run(
          foldName,
          corpusFactory.getTrainReader(fold),
          corpusFactory.getTestReader(fold),
          corpusFactory.getPreprocessor(),
          engineFactory,
          evaluationFactory,
          trainingArguments);
    }

    evaluationFactory.aggregateResults();
  }

  public static String createFoldName(int fold, int totalFolds) {
    int totalPower = (int) Math.log10(totalFolds); // TOTAL POWERRRRRR!!!
    int foldPower = (int) Math.log10(fold);
    char[] zeros = new char[totalPower - foldPower];
    Arrays.fill(zeros, '0');
    return "fold-" + new String(zeros) + fold;
  }

  public static void run(
      String runName,
      CollectionReader trainingReader,
      CollectionReader testingReader,
      AnalysisEngine preprocessing,
      CleartkPipelineProvider engineFactory,
      EvaluationPipelineProvider evaluationFactory,
      String... trainingArguments) throws Exception {

    List<AnalysisEngine> pipeline = new ArrayList<AnalysisEngine>();
    pipeline.add(preprocessing);

    List<AnalysisEngine> dataWritingPipeline = engineFactory.getTrainingPipeline(runName);
    pipeline.addAll(dataWritingPipeline);
    SimplePipeline.runPipeline(
        trainingReader,
        pipeline.toArray(new AnalysisEngine[pipeline.size()]));

    engineFactory.train(runName, trainingArguments);

    List<AnalysisEngine> classifierPipeline = engineFactory.getClassifierPipeline(runName);
    List<AnalysisEngine> evaluationPipeline = evaluationFactory.getEvaluationPipeline(runName);

    pipeline.clear();
    pipeline.add(preprocessing);
    pipeline.addAll(classifierPipeline);
    pipeline.addAll(evaluationPipeline);

    for (JCas jCas : new JCasIterable(testingReader, pipeline.toArray(new AnalysisEngine[pipeline
        .size()]))) {
      assert jCas != null;
    }
    evaluationFactory.writeResults(runName);

  }

  /**
   * This method runs "holdout" evaluation on your gold-standard data - i.e. it trains a model on
   * your training data and tests it using the holdout evaluation data.
   * 
   * @param corpusFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param engineFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param evaluationFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @throws Exception
   */
  public static void runHoldoutEvaluation(
      CorpusReaderPipeline corpusFactory,
      CleartkPipelineProvider engineFactory,
      EvaluationPipelineProvider evaluationFactory,
      String... trainingArguments) throws Exception {

    run(
        "holdout",
        corpusFactory.getTrainReader(),
        corpusFactory.getTestReader(),
        corpusFactory.getPreprocessor(),
        engineFactory,
        evaluationFactory,
        trainingArguments);

  }

  /**
   * This method builds a model using your entire corpus. You should call this method after you have
   * completed your experimentation and you now want to build a model that will be used in your
   * applications.
   * 
   * @param corpusFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param engineFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusReaderPipeline, CleartkPipelineProvider, EvaluationPipelineProvider, String...)}
   * @throws Exception
   */
  public static void buildCorpusModel(
      File outputDirectory,
      CorpusReaderPipeline corpusFactory,
      CleartkPipelineProvider engineFactory,
      String... trainingArguments) throws Exception {
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
    File modelDirectory = new File(outputDirectory, "model");
    modelDirectory.mkdir();

    CollectionReader reader = corpusFactory.getReader();
    AnalysisEngine preprocessing = corpusFactory.getPreprocessor();
    List<AnalysisEngine> dataWritingPipeline = engineFactory.getTrainingPipeline("model");

    dataWritingPipeline.add(0, preprocessing);
    SimplePipeline.runPipeline(
        reader,
        dataWritingPipeline.toArray(new AnalysisEngine[dataWritingPipeline.size()]));

    engineFactory.train("model", trainingArguments);
  }

  /**
   * This method is provided as a convenience method for training a model. It basically just creates
   * a single string array from the model directory and the training arguments to be passed to
   * {@link Train#main(String...)}.
   * 
   * @param modelDirectory
   *          the directory containing the training data file
   * @param trainingArguments
   *          the training arguments to pass on to the learner
   * @throws Exception
   */
  public static void train(File modelDirectory, String... trainingArguments) throws Exception {
    String[] args;
    if (trainingArguments == null || trainingArguments.length == 0) {
      args = new String[] { modelDirectory.getPath() };
    } else {
      args = new String[trainingArguments.length + 1];
      System.arraycopy(trainingArguments, 0, args, 1, trainingArguments.length);
      args[0] = modelDirectory.getPath();
    }
    System.out.println("training model: " + Arrays.asList(args));
    Train.main(args);
  }

}
