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

package org.cleartk.eval.provider.corpus.xmi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.eval.provider.corpus.CorpusFactory_ImplBase;
import org.cleartk.util.cr.FilesCollectionReader;
import org.cleartk.util.cr.XReader;
import org.uimafit.factory.CollectionReaderFactory;

/**
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <br>
 * A convenient way to manage a corpus of annotations is to use XMI files. If you have a way to load
 * your annotations into a CAS, then you can create an XMI file - one for each document. Once you
 * have your XMI files, this factory class provides a simple way to split up your XMI files into a
 * training set, a test set, and folds from your training set. It assumes that all of your XMI files
 * are in single directory specified by {@link #getXmiDirectory()}. Your job is to serve up the
 * names of the XMI files that belong to the various splits of the data.
 * 
 * @author Philip Ogren
 * 
 */

public abstract class XmiCorpusFactory extends CorpusFactory_ImplBase {

  public XmiCorpusFactory(TypeSystemDescription typeSystemDescription) {
    super(typeSystemDescription);
  }

  /**
   * for a given fold, give the file names for the training set.
   * 
   * @param fold
   *          the fold number
   * @return an array of file names used for the training set for this fold.
   */
  public abstract String[] getTrainNames(int fold) throws ResourceInitializationException;

  /**
   * for a given fold, give the file names for the test set.
   * 
   * @param fold
   *          the fold number
   * @return an array of file names used for the test set for this fold.
   */
  public abstract String[] getTestNames(int fold) throws ResourceInitializationException;

  /**
   * This directory contains all of the xmi files in the corpus
   * 
   * @return a path name to the directory containing the xmi files.
   */
  public abstract String getXmiDirectory() throws ResourceInitializationException;

  /**
   * Provides the file names for the training set.
   * 
   * @return an array of file names used for the training set for this corpus.
   */
  public abstract String[] getTrainNames() throws ResourceInitializationException;

  /**
   * Provides the file names for the test set.
   * 
   * @return an array of file names used for the test set for this corpus.
   */
  public abstract String[] getTestNames() throws ResourceInitializationException;

  private CollectionReader getReader(String[] fileNames) throws ResourceInitializationException {
    String directory = getXmiDirectory();
    CollectionReader collectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        directory,
        FilesCollectionReader.PARAM_FILE_NAMES,
        fileNames);
    return collectionReader;

  }

  @Override
  public CollectionReader getTrainReader(int fold) throws ResourceInitializationException {
    verifyFoldValue(fold);
    String[] fileNames = getTrainNames(fold);
    return getReader(fileNames);
  }

  @Override
  public CollectionReader getTestReader(int fold) throws ResourceInitializationException {
    verifyFoldValue(fold);
    String[] fileNames = getTestNames(fold);
    return getReader(fileNames);
  }

  @Override
  public CollectionReader getTrainReader() throws ResourceInitializationException {
    String[] fileNames = getTrainNames();
    return getReader(fileNames);
  }

  @Override
  public CollectionReader getTestReader() throws ResourceInitializationException {
    String[] fileNames = getTestNames();
    return getReader(fileNames);
  }

  @Override
  public CollectionReader getReader() throws ResourceInitializationException {
    String[] trainNames = getTrainNames();
    String[] testNames = getTestNames();
    String[] fileNames = new String[trainNames.length + testNames.length];
    System.arraycopy(trainNames, 0, fileNames, 0, trainNames.length);
    System.arraycopy(testNames, 0, fileNames, trainNames.length, testNames.length);
    return getReader(fileNames);
  }

  public CollectionReader createReader(int... folds) throws ResourceInitializationException {
    List<String> fileNames = new ArrayList<String>();
    for (int fold : folds) {
      verifyFoldValue(fold);
      fileNames.addAll(Arrays.asList(getTestNames(fold)));
    }

    String[] names = fileNames.toArray(new String[fileNames.size()]);
    return getReader(names);

  }

}
