/*
 * This file was copied from edu.umn.biomedicus.evaluation.CorpusFactory in the Biomedicus project (see http://biomedicus.googlecode.com).
 * The original file is made available under the ASL 2.0 with the following text:
 * 
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

package org.cleartk.eval.provider;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotatorAdapter;

/**
 * CorpusFactory assumes that your corpus is split into two sets - a training set and a testing set.
 * To get the training set call createTrainReader(). To get the testing set call createTestReader().
 * It further assumes that the training set is split up into n folds which can be used for
 * cross-validation. In this way the testing set is a "holdout" evaluation set not to be
 * polluted/burned by repeated evaluation that occurs with cross-validation. To get a training set
 * for a fold call createTrainReader(int). This should return a fraction of the training set equal
 * to (n-1)/n where n is the number of folds (generally 4/5 or 9/10). To get a testing set for a
 * fold call createTestReader(int). This should return a fraction of the *training* set equal to 1/n
 * (generally 1/5 or 1/10). Remember that we do not want to touch the hold-evalOut evaluation set
 * when performing cross-validation.
 * 
 * @author Philip Ogren
 * 
 */
public interface CorpusReaderPipeline {

  /**
   * This method returns the training set for the corpus.
   * 
   * @return a collection reader for the training set from the corpus
   * @throws ResourceInitializationException
   */
  public CollectionReader getTrainReader() throws ResourceInitializationException;

  /**
   * This method returns the testing set for the corpus.
   * 
   * @return a collection reader for the tresting set from the corpus
   * @throws ResourceInitializationException
   */
  public CollectionReader getTestReader() throws ResourceInitializationException;

  /**
   * This method returns a training set for a given fold from the training set.
   * 
   * @param fold
   *          a number between 1 and the value returned by {@link #getNumberOfFolds()}.
   * @return a collection reader for a training set for the fold from the training set.
   * @throws ResourceInitializationException
   */
  public CollectionReader getTrainReader(int fold) throws ResourceInitializationException;

  /**
   * This method returns a testing set for a given fold from the training set.
   * 
   * @param fold
   *          a number between 1 and the value returned by {@link #getNumberOfFolds()}.
   * @return a collection reader for a testing set for the fold from the training set.
   * @throws ResourceInitializationException
   */
  public CollectionReader getTestReader(int fold) throws ResourceInitializationException;

  /**
   * This method provides a collection reader for the entire corpus. This is useful if, for example,
   * you want to train a model that includes all data from the corpus for using in a runtime system
   * (i.e. evaluation is not going to be performed.)
   * 
   * @return a collection reader for the entire corpus.
   * @throws ResourceInitializationException
   */
  public CollectionReader getReader() throws ResourceInitializationException;

  /**
   * Some corpora will require some preprocessing in order to populate e.g. the gold view with all
   * of the annotated data that is available in the corpus. It is preferable to set up your corpus
   * so that it reads in xmi files such that all the annotation data provided by the corpus is ready
   * to go with no preprocessing. However, this may not be feasible in all situations. This method
   * allows you to provide a preprocessing analysis engine (can be primitive or aggregate) to
   * perform all the necessary work that needs to be done before the data is ready to go. This
   * method should return a valid {@link AnalysisEngineDescription} regardless of whether there is
   * any preprocessing to do. If no preprocessing is required, then return an
   * AnalysisEngineDescription for the {@link JCasAnnotatorAdapter} class as is done in
   * {@link GeniaFactory#getPreprocessor()}.
   * 
   * @return an analysis engine description that defines all of the preprocessing that needs to be
   *         done for each document in the corpus
   * @throws ResourceInitializationException
   */
  public AnalysisEngine getPreprocessor() throws ResourceInitializationException;

  /**
   * Provides the number of folds in the training set that can be used for cross-validation. This
   * number is generally 10 or 5 for 10-fold and 5-fold cross-validation, respectively. See
   * class-level javadoc comment above.
   * 
   * @return the number of folds in the training set
   */
  public int getNumberOfFolds();

  public void setNumberOfFolds(int numberOfFolds);

}
