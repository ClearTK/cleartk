package org.cleartk.eval;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class EvaluationTestBase {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  protected File outputDirectory;

  @Before
  public void setUp() throws Exception {
    outputDirectory = folder.newFolder("output");
  }

}
