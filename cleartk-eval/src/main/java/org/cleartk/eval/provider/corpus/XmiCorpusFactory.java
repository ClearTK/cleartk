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

package org.cleartk.eval.provider.corpus;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.eval.provider.CorpusReaderPipeline;
import org.cleartk.util.cr.FilesCollectionReader;
import org.cleartk.util.cr.XReader;
import org.uimafit.factory.CollectionReaderFactory;

/**
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 *         <p>
 *         A convenient way to manage a corpus of annotations is to use XMI files. If you have a way
 *         to load your annotations into a CAS, then you can create an XMI file - one for each
 *         document. For a very coarse example of creating XMI files see
 *         org.cleartk.evaluation.factory.corpus.XmiCorpusFactoryTest.main(String[]). Once you have
 *         your XMI files, this factory class provides a simple way to split up your XMI files into
 *         a training set, a test set, and folds from your training set. The idea is to create file
 *         name files (files that contain a list of file names) corresponding to these different
 *         sets. Given these files, the XmiCorpusFactory does the rest of the work of serving up the
 *         right collection readers. For a complete example, please see the data found in
 *         /cleartk-ml/src/test/resources/evaluation/factory/corpus/xmi-factory-test-data and
 *         consult the README in that directory.
 *         <p>
 *         Please see {@link CorpusReaderPipeline} for a description, broadly speaking, of how you should
 *         set up your corpus. Here, this is implemented by simply creating files that contain file
 *         names for the different sets and folds. The names of the files that you should create
 *         might be:
 *         <ul>
 *         <li>fold-1.txt - consists of the file names for the test set for fold 1</li>
 *         <li>fold-2.txt</li>
 *         <li>...</li>
 *         <li>fold-n.txt</li>
 *         <li>test.txt - consists of the file names for the test (i.e. holdout evaluation) set.</li>
 *         <li>train.txt - consists of the file names for the training set</li>
 *         </ul>
 * 
 * 
 */

public abstract class XmiCorpusFactory extends CorpusFactory_ImplBase {

  public XmiCorpusFactory(TypeSystemDescription typeSystemDescription) {
    super(typeSystemDescription);
  }

  /**
   * for a given fold, give the file names files (i.e. the paths to files consisting of file names)
   * for the training set.
   * 
   * @param fold
   *          the fold number (between 1 and the number of folds (inclusive))
   * @return an array of path names to files consisting of the file names used for the training set
   *         for this fold.
   */
  public abstract String[] getTrainNames(int fold);

  /**
   * for a given fold, give the file names file (i.e. the path to the file consisting of file names)
   * for the test set.
   * 
   * @param fold
   *          the fold number (between 1 and the number of folds (inclusive))
   * @return an path name to the file consisting of the file names used for the test set for this
   *         fold.
   */
  public abstract String getTestNames(int fold);

  /**
   * This directory contains all of the xmi files in the corpus
   * 
   * @return a path name to the directory containing the xmi files.
   */
  public abstract String getXmiDirectory();

  /**
   * Provides the file names file (i.e. the path to the file consisting of file names) for the
   * training set.
   * 
   * @return an path name to the file consisting of the file names used for the training set for
   *         this corpus.
   */
  public abstract String getTrainNames();

  /**
   * Provides the file names file (i.e. the path to the file consisting of file names) for the test
   * set.
   * 
   * @return an path name to the file consisting of the file names used for the test set for this
   *         corpus.
   */
  public abstract String getTestNames();

  @Override
  public CollectionReader getTrainReader(int fold) throws ResourceInitializationException {
    verifyFoldValue(fold);
    String[] foldNames = getTrainNames(fold);

    String directory = getXmiDirectory();
    CollectionReader collectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        directory,
        FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES,
        foldNames);
    return collectionReader;
  }

  @Override
  public CollectionReader getTestReader(int fold) throws ResourceInitializationException {
    verifyFoldValue(fold);

    String testFoldNames = getTestNames(fold);
    String directory = getXmiDirectory();

    CollectionReader collectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        directory,
        FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES,
        new String[] { testFoldNames });
    return collectionReader;
  }

  @Override
  public CollectionReader getTrainReader() throws ResourceInitializationException {
    String evaluationNames = getTrainNames();
    String directory = getXmiDirectory();
    CollectionReader collectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        directory,
        FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES,
        new String[] { evaluationNames });
    return collectionReader;
  }

  @Override
  public CollectionReader getTestReader() throws ResourceInitializationException {
    String evaluationNames = getTestNames();
    String directory = getXmiDirectory();
    CollectionReader collectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        directory,
        FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES,
        new String[] { evaluationNames });
    return collectionReader;

  }

  @Override
  public CollectionReader getReader() throws ResourceInitializationException {
    String directory = getXmiDirectory();
    return CollectionReaderFactory.createCollectionReader(
        XReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        directory,
        FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES,
        new String[] { getTestNames(), getTrainNames() });
  }

  public CollectionReader createReader(int... folds) throws ResourceInitializationException {
    List<String> foldNames = new ArrayList<String>();
    for (int fold : folds) {
      verifyFoldValue(fold);
      foldNames.add(getTestNames(fold));
    }

    String[] names = foldNames.toArray(new String[foldNames.size()]);

    String directory = getXmiDirectory();
    CollectionReader collectionReader = CollectionReaderFactory.createCollectionReader(
        XReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        directory,
        FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES,
        names);
    return collectionReader;
  }

}
