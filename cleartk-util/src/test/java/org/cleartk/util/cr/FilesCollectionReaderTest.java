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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.util.ViewURIUtil;
import org.junit.Test;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * Unit tests for org.cleartk.readers.DirectoryCollectionReader.
 * 
 * @author Steven Bethard
 */
public class FilesCollectionReaderTest extends DefaultTestBase {

  /**
   * The directory containing all the files to be loaded.
   */
  private final String inputDir = "src/test/resources/html";

  private String toURI(String relativePath) {
    return new File(inputDir, relativePath).toURI().toString();
  }

  /**
   * The paths for the files in the input directory.
   */
  private final String[] paths = new String[] {
      toURI("1.html"),
      toURI("/2/2.1.html"),
      toURI("2/2.2.html"),
      toURI("3.html"),
      toURI("4/1/4.1.1.html"), };

  private final String[] pathsSuffix1 = new String[] {
      toURI("1.html"),
      toURI("2/2.1.html"),
      toURI("4/1/4.1.1.html"), };

  private final String[] pathsSuffix2 = new String[] {
      toURI("1.html"),
      toURI("2/2.1.html"),
      toURI("2/2.2.html"),
      toURI("4/1/4.1.1.html"), };

  /**
   * Test that the text loaded into the CAS by the CollectionReader matches the text in the files on
   * disk.
   * 
   * @throws IOException
   * @throws UIMAException
   */
  @Test
  public void testText() throws Exception {

    // create the PlainTextCollectionReader with the HTML input directory
    String languageCode = "en-us";
    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        this.inputDir,
        FilesCollectionReader.PARAM_LANGUAGE,
        languageCode);
    assertEquals(0, reader.getProgress()[0].getCompleted());

    // check that each document in the CAS matches the document on disk
    for (JCas jc : new JCasIterable(reader)) {
      assertEquals(languageCode, jc.getDocumentLanguage());

      String jCasText = jc.getDocumentText();
      String docText = this.getFileText(jc);
      assertEquals(jCasText, docText);
    }
    reader.close();
    assertEquals(this.paths.length, reader.getProgress()[0].getCompleted());
  }

  /**
   * Test that that the CollectionReader can load the text into different CAS views when requested.
   * 
   * @throws IOException
   * @throws UIMAException
   */
  @Test
  public void testViewText() throws Exception {

    // check texts when different views are used
    for (String viewName : new String[] { "TestView", "OtherTestView" }) {

      // create the PlainTextCollectionReader with the current view name
      CollectionReader reader = CollectionReaderFactory.createCollectionReader(
          FilesCollectionReader.class,
          null,
          FilesCollectionReader.PARAM_ROOT_FILE,
          this.inputDir,
          FilesCollectionReader.PARAM_VIEW_NAME,
          viewName);

      // check that each document in the JCas views matches the document on disk
      for (JCas jc : new JCasIterable(reader)) {
        JCas view = jc.getView(viewName);
        String jCasText = view.getDocumentText();
        String docText = this.getFileText(view);
        assertEquals(jCasText, docText);
      }
      reader.close();
    }
  }

  /**
   * Test that all files in the directory (and subdirectories) are loaded into the CAS.
   * 
   * @throws IOException
   * @throws UIMAException
   */
  @Test
  public void testFilePaths() throws IOException, UIMAException {

    // create the PlainTextCollectionReader with the HTML input directory
    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        this.inputDir);

    // check that each path in the CAS matches a path on disk
    Set<String> pathsSet = new HashSet<String>();
    pathsSet.addAll(Arrays.asList(this.paths));
    for (JCas jc : new JCasIterable(reader)) {
      String docPath = ViewURIUtil.getURI(jc);
      assertTrue(pathsSet.contains(docPath));
      pathsSet.remove(docPath);
    }

    reader.close();

    // check that all documents were seen
    assertTrue(pathsSet.isEmpty());
  }

  @Test
  public void testSuffixes() throws IOException, UIMAException {

    // create the PlainTextCollectionReader with the HTML input directory
    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        this.inputDir,
        FilesCollectionReader.PARAM_SUFFIXES,
        new String[] { "1.html" });

    // check that each path in the CAS matches a path on disk
    Set<String> pathsSet = new HashSet<String>();
    pathsSet.addAll(Arrays.asList(this.pathsSuffix1));
    for (JCas jc : new JCasIterable(reader)) {
      String docPath = ViewURIUtil.getURI(jc);
      assertTrue(pathsSet.contains(docPath));
      pathsSet.remove(docPath);
    }
    reader.close();

    // check that all documents were seen
    assertTrue(pathsSet.isEmpty());
  }

  @Test
  public void testSuffixes2() throws IOException, UIMAException {

    // create the PlainTextCollectionReader with the HTML input directory
    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        this.inputDir,
        FilesCollectionReader.PARAM_SUFFIXES,
        new String[] { "1.html", "2.html" });

    // check that each path in the CAS matches a path on disk
    Set<String> pathsSet = new HashSet<String>();
    pathsSet.addAll(Arrays.asList(this.pathsSuffix2));
    for (JCas jc : new JCasIterable(reader)) {
      String docPath = ViewURIUtil.getURI(jc);
      assertTrue(pathsSet.contains(docPath));
      pathsSet.remove(docPath);
    }
    reader.close();

    // check that all documents were seen
    assertTrue(pathsSet.isEmpty());
  }

  @Test
  public void testPatterns() throws IOException, UIMAException {

    // create the PlainTextCollectionReader with the HTML input directory
    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        this.inputDir,
        FilesCollectionReader.PARAM_PATTERNS,
        new String[] { "^.*[.]1[.].*$", "^.*3.*$" });

    // the expected files
    Set<String> expected = new HashSet<String>();
    expected.add(toURI("2/2.1.html"));
    expected.add(toURI("3.html"));
    expected.add(toURI("4/1/4.1.1.html"));

    // collect paths from the CAS
    Set<String> actual = new HashSet<String>();
    for (JCas jc : new JCasIterable(reader)) {
      actual.add(ViewURIUtil.getURI(jc).replace('\\', '/'));
    }
    reader.close();

    // check that the expected paths were in the CAS
    assertEquals(expected, actual);

    reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        this.inputDir,
        FilesCollectionReader.PARAM_PATTERNS,
        new String[] { "^.*[.]1[.].*$" },
        FilesCollectionReader.PARAM_SUFFIXES,
        new String[] { ".1.1.html" });

    // the expected files
    expected = new HashSet<String>();
    expected.add(toURI("4/1/4.1.1.html"));

    // collect paths from the CAS
    actual = new HashSet<String>();
    for (JCas jc : new JCasIterable(reader)) {
      actual.add(ViewURIUtil.getURI(jc).replace('\\', '/'));
    }
    reader.close();

    // check that the expected paths were in the CAS
    assertEquals(expected, actual);

  }

  private final String[] fileNames = new String[] {
      "huckfinn.txt",
      "1.html",
      "a-test1.txt",
      "PlainTextFileNames.txt" };

  @Test
  public void testFileNames() throws IOException, UIMAException {

    // create the PlainTextCollectionReader with the HTML input directory
    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        "src/test/resources",
        FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES,
        new String[] { "src/test/resources/docs/PlainTextFileNames.txt" });

    // check that each path in the CAS matches a path on disk
    Set<String> fileNamesSet = new HashSet<String>();
    fileNamesSet.addAll(Arrays.asList(this.fileNames));
    int i = 0;
    for (JCas jc : new JCasIterable(reader)) {
      String fileName = ViewURIUtil.getURI(jc).replace('\\', '/');
      fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
      assertTrue(fileNamesSet.contains(fileName));
      fileNamesSet.remove(fileName);
      i++;
    }

    assertEquals(4, reader.getProgress()[0].getTotal());

    reader.close();

    assertEquals(4, i);
    // check that all documents were seen
    assertTrue(fileNamesSet.isEmpty());

  }

  /**
   * Check that the reader works with just a single file.
   * 
   * @throws IOException
   * @throws UIMAException
   */
  @Test
  public void testSingleFile() throws IOException, UIMAException {
    String path = "src/test/resources/html/1.html";
    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        path);

    List<String> pathsList = new ArrayList<String>();
    for (JCas jc : new JCasIterable(reader)) {
      pathsList.add(ViewURIUtil.getURI(jc).replace('\\', '/'));
    }
    reader.close();

    assertEquals(1, pathsList.size());
    assertTrue(pathsList.get(0).endsWith(path));
  }

  /**
   * Check that the reader gives an error with an invalid file.
   * 
   * @throws IOException
   * @throws UIMAException
   */
  @Test(expected = UIMAException.class)
  public void testBadFileException() throws IOException, UIMAException {
    CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        null,
        FilesCollectionReader.PARAM_ROOT_FILE,
        "data/hmtl");
  }

  /**
   * Get the text of the file referred to by this ClearTK Document.
   * 
   * @param jCas
   *          The JCas for this document.
   * @return The text of the file.
   * @throws IOException
   */
  private String getFileText(JCas jc) throws Exception {
    File docFile = new File(new URI(ViewURIUtil.getURI(jc)));
    return FileUtils.file2String(docFile);
  }

  @Test
  public void testDescriptor() throws UIMAException {
    try {
      CollectionReaderFactory.createCollectionReader(
          FilesCollectionReader.class,
          typeSystemDescription);
      fail("expected exception with no file or directory specified");
    } catch (ResourceInitializationException e) {
    }

    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        typeSystemDescription,
        FilesCollectionReader.PARAM_ROOT_FILE,
        this.inputDir);

    Object fileOrDirectory = reader.getConfigParameterValue(FilesCollectionReader.PARAM_ROOT_FILE);
    assertEquals(this.inputDir, fileOrDirectory);

    Object viewName = reader.getConfigParameterValue(FilesCollectionReader.PARAM_VIEW_NAME);
    assertEquals(CAS.NAME_DEFAULT_SOFA, viewName);

    Object encoding = reader.getConfigParameterValue(FilesCollectionReader.PARAM_ENCODING);
    assertEquals(null, encoding);

    Object language = reader.getConfigParameterValue(FilesCollectionReader.PARAM_LANGUAGE);
    assertEquals(null, language);
  }

  @Test
  public void testXOR() {
    assertTrue(true ^ false ^ false);
    assertFalse(true ^ true ^ false);
    assertTrue(false ^ false ^ true);
    assertFalse(true ^ false ^ true);
    assertFalse(false ^ false ^ false);
  }

}
