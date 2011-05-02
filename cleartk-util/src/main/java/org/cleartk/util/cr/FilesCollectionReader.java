/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.util.cr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * A CollectionReader that loads all files in a directory tree.
 * 
 * Files are loaded as plain text and stored in the JCas view selected by the user. ClearTK Document
 * objects are added to the same JCas view to record the file IDs and paths.
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
@SofaCapability(outputSofas = ViewURIUtil.URI)
public class FilesCollectionReader extends JCasCollectionReader_ImplBase {

  public static CollectionReader getCollectionReader(String fileOrDir)
      throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        PARAM_ROOT_FILE,
        fileOrDir);
  }

  public static CollectionReader getCollectionReaderWithView(String dir, String viewName)
      throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        PARAM_ROOT_FILE,
        dir,
        PARAM_VIEW_NAME,
        viewName);
  }

  public static CollectionReader getCollectionReaderWithPatterns(
      String dir,
      String viewName,
      String... patterns) throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        PARAM_ROOT_FILE,
        dir,
        PARAM_VIEW_NAME,
        viewName,
        PARAM_PATTERNS,
        patterns);
  }

  public static CollectionReader getCollectionReaderWithSuffixes(
      String dir,
      String viewName,
      String... suffixes) throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        PARAM_ROOT_FILE,
        dir,
        PARAM_VIEW_NAME,
        viewName,
        PARAM_SUFFIXES,
        suffixes);
  }

  public static final String PARAM_ROOT_FILE = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "rootFile");

  @ConfigurationParameter(
      mandatory = true,
      description = "takes either the name of a single file or the root directory containing all the files to be processed.")
  protected File rootFile;

  public static final String PARAM_VIEW_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "viewName");

  @ConfigurationParameter(
      description = "takes the the name that should be given to the JCas view that the document texts should be set to.",
      defaultValue = CAS.NAME_DEFAULT_SOFA)
  private String viewName;

  public static final String PARAM_LANGUAGE = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "language");

  @ConfigurationParameter(
      description = "takes the language code corresponding to the language of the documents being examined.  The value of this parameter "
          + "is simply passed on to JCas.setDocumentLanguage(String).")
  private String language;

  public static final String PARAM_ENCODING = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "encoding");

  @ConfigurationParameter(
      description = "takes the encoding of the text files (e.g. \"UTF-8\").  See javadoc for java.nio.charset.Charset for a list of encoding names.")
  private String encoding;

  public static final String PARAM_SUFFIXES = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "suffixes");

  @ConfigurationParameter(
      description = "takes suffixes (e.g. .txt) of the files that should be read in.")
  private String[] suffixes;

  public static final String PARAM_PATTERNS = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "patterns");

  @ConfigurationParameter(
      description = "	takes regular expressions for matching the files that should be read in. Note that these will be searched for"
          + " using java.util. regex.Matcher.find, so if you want to make sure the entire file name matches a pattern, you should start the string with ^ and end the"
          + " string with $.")
  private String[] patterns;

  public static final String PARAM_NAME_FILES_FILE_NAMES = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "nameFilesFileNames");

  @ConfigurationParameter(
      description = "names files which contain lists of file names. For example, if the value 'mydata/mylist.txt' is provided, "
          + "then the file 'mylist.txt' should contain a line delimited list of file names.  The file names in the list should not have directory information "
          + "but should just be the names of the files. The directory is determined by 'rootFile' and the files that are processed result from "
          + "traversing the directory structure provided and looking for files with a name found in the lists of file names. That is, no exception will be "
          + "thrown if a file name in the list does not actually correspond to a file.")
  private String[] nameFilesFileNames;

  public static final String PARAM_FILE_NAMES = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "fileNames");

  @ConfigurationParameter(
      description = "provides a list of file names that should be read in. The directory of the file names is determined by "
          + "'rootFile' and the files that are processed result from traversing the directory structure provided and looking for files with a name found in the list of file names. "
          + "That is, no exception will be thrown if a file name in the list does not actually correspond to a file.")
  private String[] fileNames;

  public static final String PARAM_IGNORE_SYSTEM_FILES = ConfigurationParameterFactory.createConfigurationParameterName(
      FilesCollectionReader.class,
      "ignoreSystemFiles");

  @ConfigurationParameter(
      description = "This parameter provides a flag that determines whether file iteration will traverse into directories that begin with a period '.' - to loosely correspond to 'system' files.  Setting this parameter to true will not cause file names that begin with a period to be ignored - just directories. ")
  private boolean ignoreSystemFiles = true;

  protected Iterator<File> files;

  protected File currentFile;

  protected int completed = 0;

  protected int filesCount = 0;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    // raise an exception if the root file does not exist
    if (!this.rootFile.exists()) {
      String format = "file or directory %s does not exist";
      String message = String.format(format, rootFile.getPath());
      throw new ResourceInitializationException(new IOException(message));
    }

    if (rootFile.isFile()) {
      files = Arrays.asList(rootFile).iterator();
      filesCount = 1;
    } else {

      files = createFileIterator();
      filesCount = countFiles(createFileIterator());
    }
  }

  protected Iterator<File> createFileIterator() throws ResourceInitializationException {
    IOFileFilter fileFilter = TrueFileFilter.INSTANCE;

    if (suffixes != null) {
      fileFilter = new AndFileFilter(fileFilter, new SuffixFileFilter(suffixes));
    }

    if (patterns != null && patterns.length > 0) {

      IOFileFilter patternFilter = new RegexFileFilter(Pattern.compile(patterns[0]));
      if (patterns.length > 1) {
        for (int i = 1; i < patterns.length; i++) {
          patternFilter = new OrFileFilter(patternFilter, new RegexFileFilter(patterns[i]));
        }
      }
      fileFilter = new AndFileFilter(fileFilter, patternFilter);

    }

    if (nameFilesFileNames != null) {
      List<String> fileNamesFromLists = new ArrayList<String>();
      try {
        for (String fileNamesList : nameFilesFileNames) {
          fileNamesFromLists.addAll(Arrays.asList(FileUtil.loadListOfStrings(new File(fileNamesList))));
        }
        fileFilter = new AndFileFilter(fileFilter, new NameFileFilter(fileNamesFromLists));
      } catch (IOException ioe) {
        throw new ResourceInitializationException(ioe);
      }
    }

    if (fileNames != null) {
      fileFilter = new AndFileFilter(fileFilter, new NameFileFilter(fileNames));
    }

    IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;

    if (ignoreSystemFiles) {
      directoryFilter = new RegexFileFilter("^[^\\.].*$");
      fileFilter = new AndFileFilter(fileFilter, new RegexFileFilter("^[^\\.].*$"));
    }

    return org.apache.commons.io.FileUtils.iterateFiles(rootFile, fileFilter, directoryFilter);

  }

  public void getNext(JCas jCas) throws IOException, CollectionException {
    if (!hasNext()) {
      throw new RuntimeException("getNext(jCas) was called but hasNext() returns false");
    }
    // get a JCas object
    JCas view;
    try {
      view = ViewCreatorAnnotator.createViewSafely(jCas, this.viewName);
    } catch (AnalysisEngineProcessException e) {
      throw new CollectionException(e);
    }

    // set the document's text
    String text = FileUtils.file2String(currentFile, this.encoding);
    view.setSofaDataString(text, "text/plain");

    // set language if it was specified
    if (this.language != null) {
      view.setDocumentLanguage(this.language);
    }

    // set the document URI
    ViewURIUtil.setURI(jCas, currentFile.toURI());

    completed++;
    currentFile = null;
  }

  protected int countFiles(Iterator<File> tempFiles) {
    int count = 0;
    while (tempFiles.hasNext()) {
      File file = tempFiles.next();
      if (file.isFile())
        count++;
    }
    return count;
  }

  public Progress[] getProgress() {
    Progress progress = new ProgressImpl(completed, filesCount, Progress.ENTITIES);
    return new Progress[] { progress };
  }

  public boolean hasNext() throws IOException, CollectionException {
    if (currentFile != null) {
      return true;
    }
    while (this.files.hasNext()) {
      currentFile = files.next();
      if (currentFile.isFile()) {
        return true;
      }
    }
    return false;
  }

  public void close() throws IOException {
  }
}
