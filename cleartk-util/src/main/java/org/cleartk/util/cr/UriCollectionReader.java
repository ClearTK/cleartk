/** 
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
package org.cleartk.util.cr;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.ViewUriUtil;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.CollectionReaderFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * A CollectionReader that populates the default sofa with URI. This can accept a Collection of
 * Files, Collection of URIs or a single directory. If given a directory it will create a jCas for
 * each file within the directory. Recursion is controlled using the directoryFilter parameter. By
 * default this will reject system files and recurse into subdirectories.
 * <p>
 * This should be used in conjunction with UriToDocumentTextAnnotator or UriToXmiCasAnnotator
 * 
 * @author Lee Becker
 * 
 */
public class UriCollectionReader extends JCasCollectionReader_ImplBase {

  public static class RejectSystemFiles implements IOFileFilter {
    FileFilter f = FileFilterUtils.fileFileFilter();

    @Override
    public boolean accept(File file) {
      return FileFilterUtils.fileFileFilter().accept(file) && HiddenFileFilter.VISIBLE.accept(file);
    }

    @Override
    public boolean accept(File dir, String name) {
      File file = new File(dir, name);
      return FileFilterUtils.directoryFileFilter().accept(file)
          && HiddenFileFilter.VISIBLE.accept(file) && this.accept(file);
    }
  }

  public static class RejectSystemDirectories implements IOFileFilter {

    @Override
    public boolean accept(File file) {
      return FileFilterUtils.directoryFileFilter().accept(file)
          && HiddenFileFilter.VISIBLE.accept(file);
    }

    @Override
    public boolean accept(File dir, String name) {
      File file = new File(dir, name);
      return FileFilterUtils.directoryFileFilter().accept(file)
          && HiddenFileFilter.VISIBLE.accept(file) && this.accept(file);
    }
  }

  public static CollectionReaderDescription getDescriptionFromDirectory(File directory)
      throws ResourceInitializationException {
    return CollectionReaderFactory.createDescription(
        UriCollectionReader.class,
        null,
        PARAM_DIRECTORY,
        directory);
  }

  public static CollectionReaderDescription getDescriptionFromDirectory(
      File directory,
      Class<? extends IOFileFilter> fileFilterClass,
      Class<? extends IOFileFilter> dirFilterClass) throws ResourceInitializationException {
    return CollectionReaderFactory.createDescription(
        UriCollectionReader.class,
        null,
        PARAM_DIRECTORY,
        directory,
        PARAM_FILE_FILTER_CLASS,
        fileFilterClass,
        PARAM_DIRECTORY_FILTER_CLASS,
        dirFilterClass);
  }

  public static CollectionReader getCollectionReaderFromDirectory(File directory)
      throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(getDescriptionFromDirectory(directory));
  }

  public static CollectionReader getCollectionReaderFromDirectory(
      File directory,
      Class<? extends IOFileFilter> fileFilterClass,
      Class<? extends IOFileFilter> dirFilterClass) throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(getDescriptionFromDirectory(
        directory,
        fileFilterClass,
        dirFilterClass));
  }

  public static CollectionReaderDescription getDescriptionFromFiles(Collection<File> files)
      throws ResourceInitializationException {

    return CollectionReaderFactory.createDescription(
        UriCollectionReader.class,
        null,
        PARAM_FILES,
        files);
  }

  public static CollectionReader getCollectionReaderFromFiles(Collection<File> files)
      throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(getDescriptionFromFiles(files));
  }

  public static CollectionReaderDescription getDescriptionFromUris(Collection<URI> uris)
      throws ResourceInitializationException {

    return CollectionReaderFactory.createDescription(
        UriCollectionReader.class,
        null,
        PARAM_URIS,
        uris);
  }

  public static CollectionReader getCollectionReaderFromUris(Collection<URI> uris)
      throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(getDescriptionFromUris(uris));
  }

  public static final String PARAM_FILES = "files";

  @ConfigurationParameter(
      name = PARAM_FILES,
      description = "provides a list of files whose URI should be written to the default sofa within the CAS")
  private Collection<File> files = new ArrayList<File>();

  public static final String PARAM_DIRECTORY = "directory";

  @ConfigurationParameter(
      name = PARAM_DIRECTORY,
      description = "provids a directory containing files whose URIs should be written to the defaul sofa within the CAS")
  private File directory = null;

  public static final String PARAM_URIS = "uris";

  @ConfigurationParameter(
      name = PARAM_URIS,
      description = "This parameter provides a list of URIs that should be written to the default sofa within the CAS.  Proper URI construction is the responsibility of the caller")
  private Collection<URI> uris = new ArrayList<URI>();

  public static final String PARAM_FILE_FILTER_CLASS = "fileFilterClass";

  @ConfigurationParameter(
      name = PARAM_FILE_FILTER_CLASS,
      defaultValue = "org.cleartk.util.cr.UriCollectionReader.RejectSystemFiles",
      mandatory = false,
      description = "The class used for filtering files when PARAM_DIRECTORY is set")
  private Class<? extends IOFileFilter> fileFilterClass;

  public static final String PARAM_DIRECTORY_FILTER_CLASS = "directoryFilterClass";

  @ConfigurationParameter(
      name = PARAM_DIRECTORY_FILTER_CLASS,
      defaultValue = "org.cleartk.util.cr.UriCollectionReader.RejectSystemDirectories",
      mandatory = false,
      description = "The class used for filtering sub-directories when PARAM_DIRECTORY is set.  To disable recursion, pass in a directory filter that rejects all directory files")
  private Class<? extends IOFileFilter> directoryFilterClass;

  protected Iterator<URI> uriIter;

  protected int numUrisCompleted = 0;

  protected int uriCount = 0;

  protected Function<String, URI> stringToUri = new Function<String, URI>() {
    @Override
    public URI apply(String input) {
      try {
        return new URI(input);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
  };

  protected Function<File, URI> fileToUri = new Function<File, URI>() {
    @Override
    public URI apply(File input) {
      return input.toURI();
    }
  };

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {

    // Convert list of files to URIs
    // Iterable<File> filteredFiles = Iterables.filter(this.files, this.directoryFilesFilter);
    this.uriCount += this.files.size();
    Iterable<URI> urisFromFiles = Iterables.transform(this.files, this.fileToUri);

    // Read file names from directory and convert list of files to URI
    Iterable<URI> urisFromDirectory = new ArrayList<URI>();
    if (this.isDirectoryValid()) {
      IOFileFilter fileFilter;
      IOFileFilter directoryFilter;

      try {
        fileFilter = this.fileFilterClass.newInstance();
        directoryFilter = this.directoryFilterClass.newInstance();
      } catch (InstantiationException e) {
        throw new ResourceInitializationException(e);
      } catch (IllegalAccessException e) {
        throw new ResourceInitializationException(e);
      }

      Collection<File> filesInDir = FileUtils.listFiles(this.directory, fileFilter, directoryFilter);
      urisFromDirectory = Iterables.transform(filesInDir, this.fileToUri);
      this.uriCount += filesInDir.size();
    }

    // Combine URI iterables from all conditions and initialize iterator
    this.uriIter = Iterables.concat(this.uris, urisFromFiles, urisFromDirectory).iterator();
  }

  private boolean isDirectoryValid() throws ResourceInitializationException {
    if (this.directory == null) {
      return false;
    }

    if (!this.directory.exists()) {
      String format = "Directory %s does not exist";
      String message = String.format(format, directory.getPath());
      throw new ResourceInitializationException(new IOException(message));
    }

    if (!this.directory.isDirectory()) {
      String format = "Directory %s is not a directory.  For specific files set PARAM_FILES instead of PARAM_DIRECTORY.";
      String message = String.format(format, directory.getPath());
      throw new ResourceInitializationException(new IOException(message));
    }
    return true;
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return this.uriIter.hasNext();
  }

  @Override
  public Progress[] getProgress() {
    Progress progress = new ProgressImpl(numUrisCompleted, uriCount, Progress.ENTITIES);
    return new Progress[] { progress };
  }

  @Override
  public void getNext(JCas jCas) throws IOException, CollectionException {
    if (!this.hasNext()) {
      throw new RuntimeException("getNext(jCas) was called but hasNext() returns false");
    }

    ViewUriUtil.setURI(jCas, this.uriIter.next());
  }

}
