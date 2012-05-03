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

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.eval.Evaluation_ImplBase;
import org.uimafit.component.NoOpAnnotator;

/**
 * CorpusReaderProvider assumes that your corpus is split into two sets - a training set and a
 * testing set. To get the training set call createTrainReader(). To get the testing set call
 * createTestReader(). It further assumes that the training set is split up into n folds which can
 * be used for cross-validation. In this way the testing set is a "holdout" evaluation set not to be
 * polluted/burned by repeated evaluation that occurs with cross-validation. To get a training set
 * for a fold call createTrainReader(int). This should return a fraction of the training set equal
 * to (n-1)/n where n is the number of folds (generally 4/5 or 9/10). To get a testing set for a
 * fold call createTestReader(int). This should return a fraction of the *training* set equal to 1/n
 * (generally 1/5 or 1/10). Remember that we do not want to touch the hold-evalOut evaluation set
 * when performing cross-validation.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 * @deprecated Use {@link Evaluation_ImplBase}
 */
@Deprecated
public interface CorpusReaderProvider {

  /**
   * This method returns the training set for the corpus.
   * 
   * @return a collection reader for the training set from the corpus
   */
  public CollectionReader getTrainReader() throws UIMAException;

  /**
   * This method returns the testing set for the corpus.
   * 
   * @return a collection reader for the tresting set from the corpus
   */
  public CollectionReader getTestReader() throws UIMAException;

  /**
   * This method returns a training set for a given fold from the training set.
   * 
   * @param fold
   *          a number between 1 and the value returned by {@link #getNumberOfFolds()}.
   * @return a collection reader for a training set for the fold from the training set.
   */
  public CollectionReader getTrainReader(int fold) throws UIMAException;

  /**
   * This method returns a testing set for a given fold from the training set.
   * 
   * @param fold
   *          a number between 1 and the value returned by {@link #getNumberOfFolds()}.
   * @return a collection reader for a testing set for the fold from the training set.
   */
  public CollectionReader getTestReader(int fold) throws UIMAException;

  /**
   * This method provides a collection reader for the entire corpus. This is useful if, for example,
   * you want to train a model that includes all data from the corpus for using in a runtime system
   * (i.e. evaluation is not going to be performed.)
   * 
   * @return a collection reader for the entire corpus.
   */
  public CollectionReader getReader() throws UIMAException;

  /**
   * Some corpora will require some preprocessing in order to populate e.g. the gold view with all
   * of the annotated data that is available in the corpus. It is preferable to set up your corpus
   * so that it reads in xmi files such that all the annotation data provided by the corpus is ready
   * to go with no preprocessing. However, this may not be feasible in all situations. This method
   * allows you to provide a preprocessing analysis engine (can be primitive or aggregate) to
   * perform all the necessary work that needs to be done before the data is ready to go. This
   * method should return a valid {@link AnalysisEngineDescription} regardless of whether there is
   * any preprocessing to do. If no preprocessing is required, then return an
   * AnalysisEngineDescription for the {@link NoOpAnnotator} class as is done in
   * {@link GeniaFactory#getPreprocessor()}.
   * 
   * @return an analysis engine description that defines all of the preprocessing that needs to be
   *         done for each document in the corpus
   */
  public AnalysisEngine getPreprocessor() throws UIMAException;

  /**
   * Provides the number of folds in the training set that will be used for cross-validation.
   * 
   * @return the number of folds in the training set
   */
  public int getNumberOfFolds();

  /**
   * Set the number of folds to be used for cross-validation. If the desired number of folds cannot
   * be accommodated by this collection, an {@link IllegalArgumentException} should be thrown.
   * 
   * @param numberOfFolds
   *          The desired number of folds.
   */
  public void setNumberOfFolds(int numberOfFolds);

}
