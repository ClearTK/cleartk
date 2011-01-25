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

package org.cleartk.evaluation;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.classifier.jar.Train;
import org.cleartk.evaluation.factory.CorpusFactory;
import org.cleartk.evaluation.factory.EngineFactory;
import org.cleartk.evaluation.factory.EvaluationFactory;
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
   * @param outputDirectory
   *          the top level directory of the experiment's output. A good name of the output
   *          directory will include the task that the tested engine performs (e.g. 'pos'), the
   *          corpus name that the experiment was run on ('genia'), the learner that is being used
   *          (e.g. 'maxent'), and any configuration parameters of interest. For example, a
   *          reasonably good directory might be: <my-experiment-home>/pos/genia/maxent. The output
   *          directory will be used to write output files and directories. There will generally be
   *          one folder corresponding to each fold in the corpus and a holdout directory each of
   *          which will contain a 'model' directory and an 'evaluation' directory. cross-validation
   *          results across all of the folds will be aggregated into an 'evaluation' directory
   *          which will sit directly under the output directory.
   * @param corpusFactory
   *          A corpus factory provides methods for creating collection readers corresponding to
   *          training data and testing data for each fold. Please see {@link CorpusFactory}.
   * @param engineFactory
   *          An engine factory provides methods for creating aggregate analysis engines that
   *          process training data to generate a training data file suitable for the machine
   *          learning library that is being used, train a model using the training data file, and
   *          test the model using the testing data for a given fold. See {@link EngineFactory}.
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
      File outputDirectory,
      CorpusFactory corpusFactory,
      EngineFactory engineFactory,
      EvaluationFactory evaluationFactory,
      String... trainingArguments) throws Exception {
    PrintStream evaluationLog = new PrintStream(new File(outputDirectory, "evaluation.log"));
    log1(
        evaluationLog,
        outputDirectory,
        corpusFactory,
        engineFactory,
        evaluationFactory,
        trainingArguments);
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
    int folds = corpusFactory.numberOfFolds();
    List<File> evaluationDirectories = new ArrayList<File>();
    for (int fold = 1; fold <= folds; fold++) {
      evaluationLog.println(String.format("fold = %d", fold));
      File foldDirectory;
      if (fold < 10) {
        foldDirectory = new File(outputDirectory, "fold0" + fold);
      } else {
        foldDirectory = new File(outputDirectory, "fold" + fold);
      }
      foldDirectory.mkdir();
      File modelDirectory = new File(foldDirectory, "model");
      modelDirectory.mkdir();
      File evaluationDirectory = new File(foldDirectory, "evaluation");
      evaluationDirectory.mkdir();
      evaluationDirectories.add(evaluationDirectory);

      evaluationLog
          .println(String.format("creating training data in %s.", modelDirectory.getPath()));
      CollectionReader trainingReader = corpusFactory.createTrainReader(fold);
      AnalysisEngineDescription preprocessing = corpusFactory.createPreprocessor();
      AnalysisEngineDescription dataWritingAggregate = engineFactory
          .createDataWritingAggregate(modelDirectory);
      SimplePipeline.runPipeline(trainingReader, preprocessing, dataWritingAggregate);

      evaluationLog.println(String.format("training model in %s.", modelDirectory.getPath()));
      engineFactory.train(modelDirectory, trainingArguments);

      evaluationLog.println(String.format(
          "testing model.  Writing evaluation results to %s.",
          evaluationDirectory.getPath()));
      CollectionReader testingReader = corpusFactory.createTestReader(fold);
      AnalysisEngineDescription classifierAggregate = engineFactory
          .createClassifierAggregate(modelDirectory);
      AnalysisEngineDescription evaluationAggregate = evaluationFactory
          .createEvaluationAggregate(evaluationDirectory);
      SimplePipeline.runPipeline(
          testingReader,
          preprocessing,
          classifierAggregate,
          evaluationAggregate);
    }
    File evaluationDirectory = new File(outputDirectory, "evaluation");
    evaluationDirectory.mkdirs();
    evaluationLog.println(String.format(
        "Aggregating evaluation results to %s.",
        evaluationDirectory.getPath()));
    evaluationFactory.aggregateEvaluationResults(evaluationDirectories, evaluationDirectory);
    evaluationLog.println("Finished running cross-validation");
  }

  private static void log1(
      PrintStream evaluationLog,
      File outputDirectory,
      CorpusFactory corpusFactory,
      EngineFactory engineFactory,
      EvaluationFactory evaluationFactory,
      String[] trainingArguments) {
    evaluationLog
        .println(String
            .format(
                "running cross-validation in directory %s.  corpus factory=%s, engine factory=%s, evaluation factory=%s, training arguments=%s",
                outputDirectory.getPath(),
                corpusFactory.getClass().getName(),
                engineFactory.getClass().getName(),
                evaluationFactory.getClass().getName(),
                Arrays.asList(trainingArguments)));
  }

  /**
   * This method runs "holdout" evaluation on your gold-standard data - i.e. it trains a model on
   * your training data and tests it using the holdout evaluation data.
   * 
   * @param outputDirectory
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @param corpusFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @param engineFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @param evaluationFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @throws Exception
   */
  public static void runHoldoutEvaluation(
      File outputDirectory,
      CorpusFactory corpusFactory,
      EngineFactory engineFactory,
      EvaluationFactory evaluationFactory,
      String... trainingArguments) throws Exception {
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
    File foldDirectory = new File(outputDirectory, "holdout");
    foldDirectory.mkdir();
    File modelDirectory = new File(foldDirectory, "model");
    modelDirectory.mkdir();
    File evaluationDirectory = new File(foldDirectory, "evaluation");
    evaluationDirectory.mkdir();

    CollectionReader trainingReader = corpusFactory.createTrainReader();
    AnalysisEngineDescription preprocessing = corpusFactory.createPreprocessor();
    AnalysisEngineDescription dataWritingAggregate = engineFactory
        .createDataWritingAggregate(modelDirectory);
    SimplePipeline.runPipeline(trainingReader, preprocessing, dataWritingAggregate);

    engineFactory.train(modelDirectory, trainingArguments);

    CollectionReader testingReader = corpusFactory.createTestReader();
    AnalysisEngineDescription classifierAggregate = engineFactory
        .createClassifierAggregate(modelDirectory);
    AnalysisEngineDescription evaluationAggregate = evaluationFactory
        .createEvaluationAggregate(evaluationDirectory);

    SimplePipeline.runPipeline(
        testingReader,
        preprocessing,
        classifierAggregate,
        evaluationAggregate);

  }

  /**
   * This method builds a model using your entire corpus. You should call this method after you have
   * completed your experimentation and you now want to build a model that will be used in your
   * applications.
   * 
   * @param outputDirectory
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @param corpusFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @param engineFactory
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @param trainingArguments
   *          see
   *          {@link #runCrossValidation(File, CorpusFactory, EngineFactory, EvaluationFactory, String...)}
   * @throws Exception
   */
  public static void buildCorpusModel(
      File outputDirectory,
      CorpusFactory corpusFactory,
      EngineFactory engineFactory,
      String... trainingArguments) throws Exception {
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
    File modelDirectory = new File(outputDirectory, "model");
    modelDirectory.mkdir();

    CollectionReader reader = corpusFactory.createReader();
    AnalysisEngineDescription preprocessing = corpusFactory.createPreprocessor();
    AnalysisEngineDescription dataWritingAggregate = engineFactory
        .createDataWritingAggregate(modelDirectory);
    SimplePipeline.runPipeline(reader, preprocessing, dataWritingAggregate);

    engineFactory.train(modelDirectory, trainingArguments);
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
