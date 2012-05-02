package org.cleartk.timeml.eval;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.timeml.corpus.TempEval2010CollectionReader;

public abstract class TempEval2010Main {

  public static void main(String[] args) throws Exception {
    for (TempEval2010Main main : Arrays.asList(
        new TempEval2010TaskAAttributes(),
        new TempEval2010TaskAExtents(),
        new TempEval2010TaskBAttributes(),
        new TempEval2010TaskBExtents(),
        new TempEval2010TaskC(),
        new TempEval2010TaskD(),
        new TempEval2010TaskE(),
        new TempEval2010TaskF())) {
      main.runMain(args);
    }
  }

  enum Command {
    CV, TEST, TRAIN
  }

  public void runMain(String[] args) throws Exception {
    String className = this.getClass().getName();
    if (args.length != 4) {
      System.err.printf(
          "usage: java %s {cv,test,train} tempeval-training-dir tempeval-test-dir output-dir",
          className);
      System.exit(1);
    }

    Command command = Command.valueOf(args[0].toUpperCase());
    char[] lineChars = new char[className.length()];
    Arrays.fill(lineChars, '=');
    String line = new String(lineChars);
    String message = String.format("\n%s\n%s\n%s", line, className, line);
    UIMAFramework.getLogger(this.getClass()).log(Level.INFO, message);
    File tempEvalTrainingDir = new File(args[1]);
    File tempEvalTestDir = new File(args[2]);
    File outputDirectory = new File(args[3]);

    TempEval2010Evaluation evaluation = this.getEvaluation(
        tempEvalTrainingDir,
        tempEvalTestDir,
        outputDirectory);

    switch (command) {
      case CV:
        List<Map<ModelInfo<?>, AnnotationStatistics>> foldResults = evaluation.crossValidation(
            TempEval2010CollectionReader.getAnnotatedFileNames(tempEvalTrainingDir),
            5);
        Map<String, AnnotationStatistics> overallResults = new HashMap<String, AnnotationStatistics>();
        for (Map<ModelInfo<?>, AnnotationStatistics> results : foldResults) {
          for (ModelInfo<?> modelInfo : results.keySet()) {
            String key = modelInfo.annotatedFeatureName;
            if (!overallResults.containsKey(key)) {
              overallResults.put(key, new AnnotationStatistics());
            }
            overallResults.get(key).addAll(results.get(modelInfo));
          }
        }
        for (String key : overallResults.keySet()) {
          System.err.println(key);
          System.err.println(overallResults.get(key));
        }
        break;
      case TEST:
        Map<ModelInfo<?>, AnnotationStatistics> results = evaluation.trainAndTest(
            TempEval2010CollectionReader.getAnnotatedFileNames(tempEvalTrainingDir),
            TempEval2010CollectionReader.getAnnotatedFileNames(tempEvalTestDir));
        for (ModelInfo<?> modelInfo : results.keySet()) {
          System.err.println(modelInfo.annotatedFeatureName);
          System.err.println(results.get(modelInfo));
          System.err.println(results.get(modelInfo).confusions());
        }
        break;
      case TRAIN:
        throw new UnsupportedOperationException();
        // break;
    }
  }

  protected abstract TempEval2010Evaluation getEvaluation(
      File trainDir,
      File testDir,
      File outputDir) throws Exception;
}
