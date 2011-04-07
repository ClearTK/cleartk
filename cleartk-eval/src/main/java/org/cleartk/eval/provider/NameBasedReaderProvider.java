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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceConfigurationException;
import org.uimafit.component.JCasAnnotatorAdapter;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Steven Bethard
 */
public abstract class NameBasedReaderProvider implements CorpusReaderProvider {

  protected List<String> trainNames;

  protected List<String> testNames;

  protected int folds;

  protected CollectionReader reader;

  public NameBasedReaderProvider(
      List<String> trainNames,
      List<String> testNames,
      CollectionReader reader) {
    this.folds = 0;
    this.trainNames = trainNames;
    this.testNames = testNames;
    this.reader = reader;
  }

  protected abstract void configureReader(List<String> names) throws ResourceConfigurationException;

  @Override
  public AnalysisEngine getPreprocessor() throws UIMAException {
    return AnalysisEngineFactory.createPrimitive(JCasAnnotatorAdapter.class);
  }

  @Override
  public int getNumberOfFolds() {
    if (this.folds == 0) {
      throw new RuntimeException("Number of folds has not been set");
    }
    return this.folds;
  }

  @Override
  public void setNumberOfFolds(int numberOfFolds) {
    if (numberOfFolds > this.trainNames.size()) {
      String message = "Expected %d or fewer folds, found %d";
      throw new RuntimeException(String.format(message, this.trainNames.size(), numberOfFolds));
    }
    this.folds = numberOfFolds;
  }

  @Override
  public CollectionReader getTrainReader(int fold) throws UIMAException {
    int totalFolds = this.getNumberOfFolds();

    // train files for this fold are all files *except for* every Nth file
    List<String> names = new ArrayList<String>();
    for (int i = 0; i < this.trainNames.size(); ++i) {
      if (i % totalFolds != fold) {
        names.add(this.trainNames.get(i));
      }
    }
    // set the reader files and return the reader
    this.configureReader(names);
    return this.reader;
  }

  @Override
  public CollectionReader getTestReader(int fold) throws UIMAException {
    int totalFolds = this.getNumberOfFolds();

    // test files for this fold are every Nth file
    List<String> names = new ArrayList<String>();
    for (int i = 0; i < this.trainNames.size(); ++i) {
      if (i % totalFolds == fold) {
        names.add(this.trainNames.get(i));
      }
    }
    // set the reader files and return the reader
    this.configureReader(names);
    return this.reader;
  }

  @Override
  public CollectionReader getTrainReader() throws UIMAException {
    this.configureReader(this.trainNames);
    return this.reader;
  }

  @Override
  public CollectionReader getTestReader() throws UIMAException {
    this.configureReader(this.testNames);
    return this.reader;
  }

  @Override
  public CollectionReader getReader() throws UIMAException {
    List<String> names = new ArrayList<String>();
    names.addAll(this.trainNames);
    names.addAll(this.testNames);
    this.configureReader(names);
    return this.reader;
  }
}
