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
package org.cleartk.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;

/**
 * A base class for training, testing and cross-validation of models.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @param <ITEM_TYPE>
 *          The type of items that define the train and test data in
 *          {@link #trainAndTest(List, List)} and {@link #crossValidation(List, int)}, and that are
 *          used to create {@link CollectionReader}s in {@link #getCollectionReader(List)}. A common
 *          choice for this parameter when working with data on a filesystem is {@link File}.
 * @param <STATS_TYPE>
 *          The type of statistics object that will be returned by testing methods. A common choice
 *          for this parameter is {@link AnnotationStatistics}.
 * @author Steven Bethard
 */
public abstract class Evaluation_ImplBase<ITEM_TYPE, STATS_TYPE> {
  protected File baseDirectory;

  /**
   * Create an evaluation that will write all auxiliary files to the given directory.
   * 
   * @param baseDirectory
   *          The directory for all evaluation files.
   */
  public Evaluation_ImplBase(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  /**
   * Train a model on one set of items and test it on another.
   * 
   * @param trainItems
   *          The items on which to train.
   * @param testItems
   *          The items on which to test.
   * @return The statistics that result from testing the model.
   */
  public STATS_TYPE trainAndTest(List<ITEM_TYPE> trainItems, List<ITEM_TYPE> testItems)
      throws Exception {
    File subDirectory = new File(this.baseDirectory, "train_and_test");
    subDirectory.mkdirs();
    this.train(this.getCollectionReader(trainItems), subDirectory);
    return this.test(this.getCollectionReader(testItems), subDirectory);
  }

  /**
   * Run a cross-validation.
   * 
   * Splits the items into nFolds approximately equal subsets, and for each subset, training a
   * classifier on all the remaining data and testing on the subset.
   * 
   * @param items
   *          The items on which to train and test. Good machine learning practice requires that
   *          these items come only from the training data, and not from the test data.
   * @param nFolds
   *          The number of subsets into which the items should be split. Note that the number of
   *          folds may not be larger than the number of items.
   * @return The statistics that result from testing the model, one for each fold.
   */
  public List<STATS_TYPE> crossValidation(List<ITEM_TYPE> items, int nFolds) throws Exception {
    if (nFolds > items.size()) {
      String message = "Cannot have %d folds with only %d items";
      throw new IllegalArgumentException(String.format(message, nFolds, items.size()));
    }

    List<STATS_TYPE> stats = new ArrayList<STATS_TYPE>();
    for (int fold = 0; fold < nFolds; ++fold) {
      File subDirectory = new File(this.baseDirectory, "fold_" + fold);
      subDirectory.mkdirs();
      List<ITEM_TYPE> trainItems = this.selectFoldTrainItems(items, nFolds, fold);
      List<ITEM_TYPE> testItems = this.selectFoldTestItems(items, nFolds, fold);
      this.train(this.getCollectionReader(trainItems), subDirectory);
      stats.add(this.test(this.getCollectionReader(testItems), subDirectory));
    }
    return stats;
  }

  /**
   * Determines which items should be used for training in one fold of a cross-validation.
   * 
   * The default implementation includes all items except for every (nFolds)th item, but this may be
   * overridden in subclasses.
   * 
   * @param items
   *          The full list of training items.
   * @param nFolds
   *          The total number of folds in this cross validation.
   * @param fold
   *          The index of the fold (0 <= fold < nFolds) whose training items are to be selected.
   * @return The items that should be used for training.
   */
  protected List<ITEM_TYPE> selectFoldTrainItems(List<ITEM_TYPE> items, int nFolds, int fold) {
    List<ITEM_TYPE> trainItems = new ArrayList<ITEM_TYPE>();
    for (int i = 0; i < items.size(); ++i) {
      if (i % nFolds != fold) {
        trainItems.add(items.get(i));
      }
    }
    return trainItems;
  }

  /**
   * Determines which items should be used for testing in one fold of a cross-validation.
   * 
   * The default implementation includes every (nFolds)th item, but this may be overridden in
   * subclasses.
   * 
   * @param items
   *          The full list of training items.
   * @param nFolds
   *          The total number of folds in this cross validation.
   * @param fold
   *          The index of the fold (0 <= fold < nFolds) whose test items are to be selected.
   * @return The items that should be used for testing.
   */
  protected List<ITEM_TYPE> selectFoldTestItems(List<ITEM_TYPE> items, int nFolds, int fold) {
    List<ITEM_TYPE> testItems = new ArrayList<ITEM_TYPE>();
    for (int i = 0; i < items.size(); ++i) {
      if (i % nFolds == fold) {
        testItems.add(items.get(i));
      }
    }
    return testItems;
  }

  /**
   * Creates a {@link CollectionReader} from the given items.
   * 
   * This method is called in {@link #trainAndTest(List, List)} and
   * {@link #crossValidation(List, int)} to create readers both for the training data and for the
   * testing data.
   * 
   * @param items
   *          Items from the training, test or cross-validation sets.
   * @return A {@link CollectionReader} that produces {@link CAS}es for the items.
   */
  protected abstract CollectionReader getCollectionReader(List<ITEM_TYPE> items) throws Exception;

  /**
   * Trains a model on a set of training data.
   * 
   * @param collectionReader
   *          The data on which the model should be trained.
   * @param directory
   *          The directory in which any model files should be written.
   */
  protected abstract void train(CollectionReader collectionReader, File directory) throws Exception;

  /**
   * Evaluates a model on a set of testing data.
   * 
   * @param collectionReader
   *          The data on which the model should be tested.
   * @param directory
   *          The directory in which any model files should be written. This method may safely
   *          assume that {@link #train(CollectionReader, File)} was called on this same directory
   *          before {@link #test(CollectionReader, File)} was called.
   * @return The statistics that result from testing the model
   */
  protected abstract STATS_TYPE test(CollectionReader collectionReader, File directory)
      throws Exception;

}
