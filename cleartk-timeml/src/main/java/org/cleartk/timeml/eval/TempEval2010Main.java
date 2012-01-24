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
package org.cleartk.timeml.eval;

import java.io.File;
import java.util.Arrays;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.cleartk.eval.Evaluation;
import org.cleartk.eval.provider.CleartkPipelineProvider;
import org.cleartk.eval.provider.CorpusReaderProvider;
import org.cleartk.eval.provider.EvaluationPipelineProvider;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class TempEval2010Main {

  enum Command {
    CV, TEST, TRAIN
  }

  private File tempEvalTrainingDir;

  private File tempEvalTestDir;

  private File outputDirectory;

  private CorpusReaderProvider getCorpusReaderProvider() throws Exception {
    return new TempEval2010ReaderProvider(this.tempEvalTrainingDir, this.tempEvalTestDir);
  }

  public void runCrossValidation() throws Exception {
    File modelDir = new File(this.outputDirectory, "cv/model");
    File xmiDir = new File(this.outputDirectory, "cv/xmi");
    File evalDir = new File(this.outputDirectory, "cv");
    CorpusReaderProvider corpus = this.getCorpusReaderProvider();
    CleartkPipelineProvider pipeline = this.getCleartkPipelineProvider(modelDir, xmiDir);
    EvaluationPipelineProvider evaluator = this.getEvaluationPipelineProvider(evalDir);
    Evaluation evaluation = new Evaluation();
    corpus.setNumberOfFolds(5);
    evaluation.runCrossValidation(corpus, pipeline, evaluator, this.getTrainingArguments());
  }

  public void runHoldoutEvaluation() throws Exception {
    File modelDir = new File(this.outputDirectory, "test/model");
    File xmiDir = new File(this.outputDirectory, "test/xmi");
    File evalDir = new File(this.outputDirectory, "test");
    CorpusReaderProvider corpus = this.getCorpusReaderProvider();
    CleartkPipelineProvider pipeline = this.getCleartkPipelineProvider(modelDir, xmiDir);
    EvaluationPipelineProvider evaluator = this.getEvaluationPipelineProvider(evalDir);
    Evaluation evaluation = new Evaluation();
    evaluation.runHoldoutEvaluation(corpus, pipeline, evaluator, this.getTrainingArguments());
  }

  public void buildCorpusModel() throws Exception {
    File modelDir = null; // build to annotator-defined training directory
    File xmiDir = new File(this.outputDirectory, "train/xmi");
    CorpusReaderProvider corpus = this.getCorpusReaderProvider();
    CleartkPipelineProvider pipeline = this.getCleartkPipelineProvider(modelDir, xmiDir);
    Evaluation evaluation = new Evaluation();
    evaluation.buildCorpusModel("model", corpus, pipeline, this.getTrainingArguments());

  }

  protected String[] getTrainingArguments() {
    return new String[0];
  }

  protected abstract CleartkPipelineProvider getCleartkPipelineProvider(
      File modelDirectory,
      File xmiDirectory) throws Exception;

  protected abstract EvaluationPipelineProvider getEvaluationPipelineProvider(File evalDirectory)
      throws Exception;

  public void runCommand(String[] args) throws Exception {
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
    this.tempEvalTrainingDir = new File(args[1]);
    this.tempEvalTestDir = new File(args[2]);
    this.outputDirectory = new File(args[3]);
    switch (command) {
      case CV:
        this.runCrossValidation();
        break;
      case TEST:
        this.runHoldoutEvaluation();
        break;
      case TRAIN:
        this.buildCorpusModel();
        break;
    }
  }

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
      main.runCommand(args);
    }
  }
}
