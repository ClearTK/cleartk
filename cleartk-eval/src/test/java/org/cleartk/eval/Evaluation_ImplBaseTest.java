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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.util.Progress;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class Evaluation_ImplBaseTest extends EvaluationTestBase {

  @Test
  public void testTrainAndTest() throws Exception {
    TestableEvaluation evaluation = new TestableEvaluation(this.outputDirectory);

    List<String> trainItems = Arrays.asList("a", "b", "c");
    List<String> testItems = Arrays.asList("d", "e", "f");
    evaluation.trainAndTest(trainItems, testItems);

    Assert.assertEquals(1, evaluation.trainDirectories.size());
    Assert.assertEquals(1, evaluation.testDirectories.size());
    Assert.assertEquals(1, evaluation.trainDirectoryItems.size());
    Assert.assertEquals(1, evaluation.testDirectoryItems.size());
    Assert.assertEquals(Collections.singletonList(trainItems), evaluation.trainDirectoryItems);
    Assert.assertEquals(Collections.singletonList(testItems), evaluation.testDirectoryItems);

    for (File dir : evaluation.trainDirectories) {
      Assert.assertTrue(dir.getPath().startsWith(this.outputDirectory.getPath()));
    }

    for (File dir : evaluation.testDirectories) {
      Assert.assertTrue(dir.getPath().startsWith(this.outputDirectory.getPath()));
    }
  }

  @Test
  public void testCrossValidation() throws Exception {
    TestableEvaluation evaluation = new TestableEvaluation(this.outputDirectory);

    List<String> trainItems = Arrays.asList("a", "b", "c", "d", "e");

    List<List<String>> trainFolds = new ArrayList<List<String>>();
    trainFolds.add(Arrays.asList("b", "c", "e"));
    trainFolds.add(Arrays.asList("a", "c", "d"));
    trainFolds.add(Arrays.asList("a", "b", "d", "e"));

    List<List<String>> testFolds = new ArrayList<List<String>>();
    testFolds.add(Arrays.asList("a", "d"));
    testFolds.add(Arrays.asList("b", "e"));
    testFolds.add(Arrays.asList("c"));

    evaluation.crossValidation(trainItems, 3);

    Assert.assertEquals(3, evaluation.trainDirectories.size());
    Assert.assertEquals(3, evaluation.testDirectories.size());
    Assert.assertEquals(3, evaluation.trainDirectoryItems.size());
    Assert.assertEquals(3, evaluation.testDirectoryItems.size());
    Assert.assertEquals(trainFolds, evaluation.trainDirectoryItems);
    Assert.assertEquals(testFolds, evaluation.testDirectoryItems);

    for (File dir : evaluation.trainDirectories) {
      Assert.assertTrue(dir.getPath().startsWith(this.outputDirectory.getPath()));
    }

    for (File dir : evaluation.testDirectories) {
      Assert.assertTrue(dir.getPath().startsWith(this.outputDirectory.getPath()));
    }

    try {
      evaluation.crossValidation(trainItems, 6);
      Assert.fail("evaluation should fail with more folds than items");
    } catch (IllegalArgumentException e) {
      // success - proper exception was thrown
    }
  }

  private static class TestableEvaluation extends Evaluation_ImplBase<String, TestableEvaluation> {

    public List<File> trainDirectories = new ArrayList<File>();

    public List<File> testDirectories = new ArrayList<File>();

    public List<List<String>> trainDirectoryItems = new ArrayList<List<String>>();

    public List<List<String>> testDirectoryItems = new ArrayList<List<String>>();

    public TestableEvaluation(File baseDirectory) {
      super(baseDirectory);
    }

    @Override
    protected CollectionReader getCollectionReader(List<String> items) throws Exception {
      return new FakeCollectionReader(items);
    }

    @Override
    protected void train(CollectionReader collectionReader, File directory) throws Exception {
      this.trainDirectories.add(directory);
      this.trainDirectoryItems.add(((FakeCollectionReader) collectionReader).items);
    }

    @Override
    protected TestableEvaluation test(CollectionReader collectionReader, File directory)
        throws Exception {
      this.testDirectories.add(directory);
      this.testDirectoryItems.add(((FakeCollectionReader) collectionReader).items);
      return this;
    }
  }

  private static class FakeCollectionReader extends CollectionReader_ImplBase {

    public List<String> items;

    public FakeCollectionReader(List<String> items) {
      this.items = items;
    }

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException {
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
      return false;
    }

    @Override
    public Progress[] getProgress() {
      return null;
    }

    @Override
    public void close() throws IOException {
    }
  }
}
