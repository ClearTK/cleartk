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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.eval.provider.CorpusReaderProvider;

/**
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * This extension of {@link XmiCorpusFactory} assumes that the folds for a corpus are fixed. The
 * folds are specified files that contain lists of file names (one file name per line.) So, for
 * example, {@link #getTrainFile()} might return a file named "<your-path>/train.txt" that contains
 * a list of those XMI files that belong in the training data set.
 * <p>
 * Please see {@link CorpusReaderProvider} for a description, broadly speaking, of how you should
 * set up your corpus. Here, this is implemented by simply creating files that contain file names
 * for the different sets and folds. The names of the files that you should create might be:
 * <ul>
 * <li>fold-1.txt - consists of the xmi file names for the test set for fold 1</li>
 * <li>fold-2.txt</li>
 * <li>...</li>
 * <li>fold-n.txt</li>
 * <li>test.txt - consists of the xmi file names for the test (i.e. holdout evaluation) set.</li>
 * <li>train.txt - consists of the xmi file names for the training set</li>
 * </ul>
 * 
 * For the list above, the method {@link #getFoldFiles()} would return the paths for the files
 * "fold-1.txt" ... "fold-n.txt", {@link #getTrainFile()} would return the path to the file
 * "train.txt" and {@link #getTestFile()} would return the path to the file "test.txt".
 * 
 * @author Philip Ogren
 * 
 */

public abstract class FixedFoldsXmiCorpusFactory extends XmiCorpusFactory {

  public FixedFoldsXmiCorpusFactory(
      TypeSystemDescription typeSystemDescription,
      String xmiDirectoryName) {
    super(typeSystemDescription, xmiDirectoryName);
  }

  /**
   * 
   * @return a list of path names to the files that contain lists of file names for each fold.
   */
  public abstract List<String> getFoldFiles();

  /**
   * @return a path name to the file that contains a list of the file names for the training set.
   */
  public abstract String getTrainFile();

  /**
   * @return a path name to the file that contains a list of the file names for the test set.
   */
  public abstract String getTestFile();

  @Override
  public int getNumberOfFolds() {
    return getFoldFiles().size();
  }

  @Override
  public void setNumberOfFolds(int numberOfFolds) {
    throw new UnsupportedOperationException(
        "the the number of folds for this corpus factory is fixed to " + getFoldFiles().size());
  }

  @Override
  public String[] getTrainNames(int fold) throws ResourceInitializationException {
    verifyFoldValue(fold);
    List<String> foldFiles = new ArrayList<String>(getFoldFiles());
    foldFiles.remove(fold);

    try {

      List<String> fileNames = new ArrayList<String>();
      for (String foldFile : foldFiles) {
        String[] names;
        names = FileUtil.loadListOfStrings(new File(foldFile));
        fileNames.addAll(Arrays.asList(names));
      }
      return fileNames.toArray(new String[fileNames.size()]);

    } catch (IOException ioe) {
      throw new ResourceInitializationException(ioe);
    }

  }

  @Override
  public String[] getTestNames(int fold) throws ResourceInitializationException {
    verifyFoldValue(fold);
    return loadNames(getFoldFiles().get(fold));
  }

  @Override
  public String[] getTrainNames() throws ResourceInitializationException {
    return loadNames(getTrainFile());
  }

  @Override
  public String[] getTestNames() throws ResourceInitializationException {
    return loadNames(getTestFile());
  }

  private String[] loadNames(String fileName) throws ResourceInitializationException {
    try {
      return FileUtil.loadListOfStrings(new File(fileName));
    } catch (IOException ioe) {
      throw new ResourceInitializationException(ioe);
    }

  }
}
