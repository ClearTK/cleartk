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
package org.cleartk.corpus.timeml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010CollectionReader extends JCasCollectionReader_ImplBase {

  public static final String BASE_SEGMENTATION_VIEW_NAME = "base-segmentation.tab";

  public static final String DCT_VIEW_NAME = "dct.txt";

  public static final String EVENT_EXTENTS_VIEW_NAME = "event-extents.tab";

  public static final String EVENT_ATTRIBUTES_VIEW_NAME = "event-attributes.tab";

  public static final String TIMEX_EXTENTS_VIEW_NAME = "timex-extents.tab";

  public static final String TIMEX_ATTRIBUTES_VIEW_NAME = "timex-attributes.tab";

  public static final String TLINK_DCT_EVENT_VIEW_NAME = "tlinks-dct-event.tab";

  public static final String TLINK_MAIN_EVENTS_VIEW_NAME = "tlinks-main-events.tab";

  public static final String TLINK_SUBORDINATED_EVENTS_VIEW_NAME = "tlinks-subordinated-events.tab";

  public static final String TLINK_TIMEX_EVENT_VIEW_NAME = "tlinks-timex-event.tab";

  public static CollectionReader getCollectionReader(String... dataPaths)
      throws ResourceInitializationException {
    List<File> dirs = new ArrayList<File>();
    for (String path : dataPaths) {
      dirs.add(new File(path));
    }
    return getCollectionReader(dirs);
  }

  public static CollectionReader getCollectionReader(List<File> dataDirectories)
      throws ResourceInitializationException {
    return getCollectionReader(dataDirectories, null);
  }

  public static CollectionReader getCollectionReader(
      List<File> dataDirectories,
      Set<String> selectedFileNames) throws ResourceInitializationException {
    // workaround UimaFIT limitation
    List<String> dirsList = new ArrayList<String>();
    for (File dir : dataDirectories) {
      dirsList.add(dir.getPath());
    }
    String[] dirs = dirsList.toArray(new String[dirsList.size()]);
    String[] names = selectedFileNames == null
        ? null
        : selectedFileNames.toArray(new String[selectedFileNames.size()]);
    return CollectionReaderFactory.createCollectionReader(
        TempEval2010CollectionReader.class,
        null,
        PARAM_DATA_DIRECTORIES,
        dirs,
        PARAM_SELECTED_FILE_NAMES,
        names);
  }

  @ConfigurationParameter(mandatory = true, description = "The directories containing the TempEval "
      + "2010 data, e.g. \"tempeval-training-2/english\" and \"tempeval2-test/english\"")
  protected List<File> dataDirectories;

  public static final String PARAM_DATA_DIRECTORIES = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010CollectionReader.class,
      "dataDirectories");

  @ConfigurationParameter(description = "The names of files that should be included when reading, "
      + "e.g \"ABC19980108.1830.0711\". If null, then all files in the dataset will be included.")
  protected Set<String> selectedFileNames;

  public static final String PARAM_SELECTED_FILE_NAMES = ConfigurationParameterFactory.createConfigurationParameterName(
      TempEval2010CollectionReader.class,
      "selectedFileNames");

  protected List<URI> uris;

  protected int uriIndex;

  private Map<String, Map<String, String>> viewFileTexts;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    try {
      // assemble URIs for each file in the data
      this.uriIndex = 0;
      this.uris = new ArrayList<URI>();
      for (File dataDirectory : this.dataDirectories) {
        URI dataURI = dataDirectory.toURI();
        for (String fileName : getAnnotatedFileNames(dataDirectory)) {
          if (this.selectedFileNames == null || this.selectedFileNames.contains(fileName)) {
            URI uri = new URI(dataURI.getScheme(), dataURI.getHost(), dataURI.getPath(), fileName);
            this.uris.add(uri);
          }
        }
      }

      // group lines by filename
      this.viewFileTexts = new HashMap<String, Map<String, String>>();
      for (String viewName : Arrays.asList(
          BASE_SEGMENTATION_VIEW_NAME,
          DCT_VIEW_NAME,
          EVENT_EXTENTS_VIEW_NAME,
          EVENT_ATTRIBUTES_VIEW_NAME,
          TIMEX_EXTENTS_VIEW_NAME,
          TIMEX_ATTRIBUTES_VIEW_NAME,
          TLINK_DCT_EVENT_VIEW_NAME,
          TLINK_MAIN_EVENTS_VIEW_NAME,
          TLINK_SUBORDINATED_EVENTS_VIEW_NAME,
          TLINK_TIMEX_EVENT_VIEW_NAME)) {
        // assumes view names are the same as the .tab file names
        this.viewFileTexts.put(viewName, this.textByFileName(viewName));
      }
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    } catch (URISyntaxException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return this.uriIndex < this.uris.size();
  }

  @Override
  public void getNext(JCas jCas) throws IOException, CollectionException {
    URI uri = this.uris.get(this.uriIndex);
    this.uriIndex += 1;
    ViewURIUtil.setURI(jCas, uri);

    String fileName = uri.getFragment();
    for (String viewName : this.viewFileTexts.keySet()) {
      JCas view;
      try {
        view = jCas.createView(viewName);
      } catch (CASException e) {
        throw new CollectionException(e);
      }
      String text = this.viewFileTexts.get(viewName).get(fileName);
      view.setDocumentText(text == null ? "" : text);
    }
  }

  @Override
  public Progress[] getProgress() {
    return new Progress[] { new ProgressImpl(this.uriIndex, this.uris.size(), Progress.ENTITIES) };
  }

  private Map<String, String> textByFileName(String tabFileName) throws IOException {
    // get all variants of the file under all subdirectories
    List<File> files = new ArrayList<File>();
    for (File dir : this.dataDirectories) {
      files.addAll(getTempEvalFiles(dir, tabFileName));
    }

    // map each file name to its lines
    ListMultimap<String, String> fileLines = ArrayListMultimap.create();
    for (File file : files) {
      for (String line : Files.readLines(file, Charsets.US_ASCII)) {
        String fileName = getAnnotatedFileName(line);
        fileLines.put(fileName, line);
      }
    }

    // convert lists of lines back into text
    Map<String, String> fileTexts = new HashMap<String, String>();
    for (String fileName : fileLines.keySet()) {
      StringBuilder builder = new StringBuilder();
      for (String line : fileLines.get(fileName)) {
        builder.append(line).append('\n');
      }
      fileTexts.put(fileName, builder.toString());
    }
    return fileTexts;
  }

  private static List<File> getTempEvalFiles(File dataDirectory, String tabFileName)
      throws FileNotFoundException {

    // subdirectory is "data" for training, and both "entities" and "relations" for testing
    List<File> files = new ArrayList<File>();
    for (String subDir : Arrays.asList("data", "key")) {
      files.add(new File(new File(dataDirectory, subDir), tabFileName));
    }
    // weird special case: dct.txt is dct-en.txt in testing base directory
    files.add(new File(dataDirectory, tabFileName.replaceAll("\\.txt", "-en.txt")));

    // filter existing files
    List<File> existingFiles = new ArrayList<File>();
    for (File file : files) {
      if (file.exists()) {
        existingFiles.add(file);
      }
    }

    // error if we didn't find at least one
    if (existingFiles.size() == 0) {
      throw new FileNotFoundException("Could not find any of " + files);
    }
    return existingFiles;
  }

  protected static String getAnnotatedFileName(String line) {
    // the filename is the first column
    String[] parts = line.split("\t", 2);
    if (parts.length != 2) {
      throw new IllegalArgumentException("Expected <filename>\t..., found " + line);
    }
    return parts[0];
  }

  public static List<String> getAnnotatedFileNames(File dataDirectory) throws IOException {
    // look for file names in all the base segmentation files
    List<String> fileNames = new ArrayList<String>();
    Set<String> seenFileNames = new HashSet<String>();
    for (File tabFile : getTempEvalFiles(dataDirectory, "base-segmentation.tab")) {
      for (String line : Files.readLines(tabFile, Charsets.US_ASCII)) {

        // add the filename to the list if we haven't already seen it
        String fileName = getAnnotatedFileName(line);
        if (!seenFileNames.contains(fileName)) {
          seenFileNames.add(fileName);
          fileNames.add(fileName);
        }
      }
    }
    return fileNames;
  }
}
