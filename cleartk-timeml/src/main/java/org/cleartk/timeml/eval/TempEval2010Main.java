/*
 * Copyright (c) 2012, Regents of the University of Colorado 
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

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
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
        List<Map<ModelInfo<?>, AnnotationStatistics<String>>> foldResults = evaluation.crossValidation(
            TempEval2010CollectionReader.getAnnotatedFileNames(tempEvalTrainingDir),
            5);
        Map<String, AnnotationStatistics<String>> overallResults = new HashMap<String, AnnotationStatistics<String>>();
        for (Map<ModelInfo<?>, AnnotationStatistics<String>> results : foldResults) {
          for (ModelInfo<?> modelInfo : results.keySet()) {
            String key = modelInfo.annotatedFeatureName;
            if (!overallResults.containsKey(key)) {
              overallResults.put(key, new AnnotationStatistics<String>());
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
        Map<ModelInfo<?>, AnnotationStatistics<String>> results = evaluation.trainAndTest(
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
