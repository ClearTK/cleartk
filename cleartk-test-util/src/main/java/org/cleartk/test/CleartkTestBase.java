/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
package org.cleartk.test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public abstract class CleartkTestBase {

  /**
   * A {@link Logger} instance associated with the class, intended for use by subclasses.
   */
  protected Logger logger;

  /**
   * The set of names for tests that should be disabled.
   */
  private Set<String> skippedTestNames;

  public CleartkTestBase() {
    super();
    this.logger = Logger.getLogger(this.getClass().getName());
    this.skippedTestNames = new HashSet<String>();
    String skipTests = System.getProperty(SKIP_TESTS_PROPERTY);
    if (skipTests != null) {
      this.skippedTestNames.addAll(Arrays.asList(skipTests.split("\\s*[,]\\s*")));
    }
  }

  /**
   * The name of the property that can be set to skip some tests. The property value will be parsed
   * as a comma-separated list of values, where each value can designate one type of test to skip,
   * e.g. {@value #LONG_TESTS_PROPERTY_VALUE} or {@value #BIG_MEMORY_TESTS_PROPERTY_VALUE}. Example
   * usage: <br/>
   * <code>-Dcleartk.skipTests=long,bigMem</code>
   */
  public static final String SKIP_TESTS_PROPERTY = "cleartk.skipTests";

  /**
   * Value for the {@link #SKIP_TESTS_PROPERTY} property that indicates that commonly skipped tests
   * should be disabled. Currently, this will disable at least {@value #LONG_TESTS_PROPERTY_VALUE}
   * and {@value #BIG_MEMORY_TESTS_PROPERTY_VALUE} tests. It will also typically disable tests that
   * require binaries that are hard to build (e.g. tk-svmlight, crfsuite). Current value:
   * {@value #COMMON_TESTS_PROPERTY_VALUE}.
   */
  public static final String COMMON_TESTS_PROPERTY_VALUE = "common";

  /**
   * Value for the {@link #SKIP_TESTS_PROPERTY} property that indicates that all tests that can be
   * skipped are disabled. Current value: {@value #ALL_TESTS_PROPERTY_VALUE}.
   */
  public static final String ALL_TESTS_PROPERTY_VALUE = "all";

  /**
   * Subclasses should call this method at the beginning of a test to check whether a specific type
   * of test is enabled by checking whether the name of that test is contained in the
   * {@value #SKIP_TESTS_PROPERTY} property. If the name is present, then the test will be skipped.
   * 
   * Typically, immediately after calling {@link #assumeTestsEnabled}, a test should log a message
   * created by {@link #createTestEnabledMessage} to explain to a user how to disable the test if
   * they wish.
   * 
   * Note that if {@value #SKIP_TESTS_PROPERTY} contains {@value #ALL_TESTS_PROPERTY_VALUE} then the
   * test will be skipped even if {@link #ALL_TESTS_PROPERTY_VALUE} was not passed as an argument to
   * this method.
   */
  protected void assumeTestsEnabled(String... testNames) {
    Assume.assumeTrue(!this.skippedTestNames.contains(ALL_TESTS_PROPERTY_VALUE));
    for (String testName : testNames) {
      Assume.assumeTrue(!this.skippedTestNames.contains(testName));
    }
  }

  /**
   * Subclasses should call this method to create a message that explains how to disable a test.
   * Typically such a message should be logged immediately after calling
   * {@link #assumeTestsEnabled(String...)}.
   * 
   * @param testName
   *          The name of the test (i.e. the value that should be present in the
   *          {@value #SKIP_TESTS_PROPERTY} property)
   * @param message
   *          A message describing the requirements of the tests that are run when the named test is
   *          enabled.
   * @return A message that contains both the given description and an explanation of how to disable
   *         the test.
   */
  public static String createTestEnabledMessage(String testName, String message) {
    String format = "%s. To skip this test, set -D%s=%s";
    return String.format(format, message.replaceAll("[.]$", ""), SKIP_TESTS_PROPERTY, testName);
  }

  /**
   * Value for the {@link #SKIP_TESTS_PROPERTY} property that indicates that long-running tests
   * should be disabled. Current value: {@value #LONG_TESTS_PROPERTY_VALUE}.
   */
  public static final String LONG_TESTS_PROPERTY_VALUE = "long";

  /**
   * Subclasses should call this method at the beginning of a test that will take a long time to
   * run. Immediately after calling this method, they should also call
   * <code>this.logger.info({@link #LONG_TEST_MESSAGE})</code>.
   */
  protected void assumeLongTestsEnabled() {
    // note that we can't log the message here as well, or it the log will display the wrong method
    this.assumeTestsEnabled(COMMON_TESTS_PROPERTY_VALUE, LONG_TESTS_PROPERTY_VALUE);
  }

  /**
   * A message indicating that the current test runs for a long time, and giving instructions how to
   * skip it. Should be logged immediately after calling {@link #assumeLongTestsEnabled()}.
   */
  protected static final String LONG_TEST_MESSAGE = createTestEnabledMessage(
      LONG_TESTS_PROPERTY_VALUE,
      "This test takes a long time to run");

  /**
   * Value for the {@link #SKIP_TESTS_PROPERTY} property that indicates that tests requiring a lot
   * of memory should be disabled. Current value: {@value #BIG_MEMORY_TESTS_PROPERTY_VALUE}.
   */
  public static final String BIG_MEMORY_TESTS_PROPERTY_VALUE = "bigMem";

  /**
   * Subclasses should call this method at the beginning of a test that will take a long time to
   * run. Immediately after calling this method, they should also call
   * <code>this.logger.info({@link #BIG_MEMORY_TEST_MESSAGE})</code>.
   */
  protected void assumeBigMemoryTestsEnabled() {
    // note that we can't log the message here as well, or it the log will display the wrong method
    this.assumeTestsEnabled(COMMON_TESTS_PROPERTY_VALUE, BIG_MEMORY_TESTS_PROPERTY_VALUE);
  }

  /**
   * A message indicating that the current test requires a lot of memory, and giving instructions
   * how to skip it. Should be logged immediately after calling
   * {@link #assumeBigMemoryTestsEnabled()}.
   */
  public static final String BIG_MEMORY_TEST_MESSAGE = createTestEnabledMessage(
      BIG_MEMORY_TESTS_PROPERTY_VALUE,
      "This test requires a lot of memory");

  protected JCas jCas;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  protected File outputDirectory;

  protected String outputDirectoryName;

  @Before
  public void setUp() throws Exception {
    jCas = JCasFactory.createJCas();
    outputDirectory = folder.newFolder("output");
    outputDirectoryName = outputDirectory.getPath();
  }
}
