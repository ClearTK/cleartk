package org.cleartk.util.cr.linereader;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;

public class LineReaderXmiWriterTest extends DefaultTestBase {

  private String inputDir = "src/test/resources/linereader";

  @Test
  public void testXmiWriter() throws Exception {
    File outputDir = new File(outputDirectory, "line-reader-xmi");
    outputDir.mkdirs();

    String languageCode = "en-us";
    CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        LineReader.class,
        null,
        LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
        inputDir,
        LineReader.PARAM_LANGUAGE,
        languageCode,
        LineReader.PARAM_SUFFIXES,
        new String[] { ".txt", ".dat" });

    AnalysisEngineDescription xmiWriter = LineReaderXmiWriter.getDescription(outputDir);

    SimplePipeline.runPipeline(reader, xmiWriter);

    assertTrue(new File(outputDir, "a-test1.txt.1.xmi").exists());
    assertTrue(new File(outputDir, "a-test1.txt.2.xmi").exists());
    assertTrue(new File(outputDir, "a-test1.txt.3.xmi").exists());
    assertTrue(new File(outputDir, "a-test1.txt.4.xmi").exists());
    assertTrue(new File(outputDir, "a-test1.txt.5.xmi").exists());
    assertTrue(new File(outputDir, "a-test1.txt.6.xmi").exists());
    assertTrue(new File(outputDir, "a-test1.txt.7.xmi").exists());
    assertTrue(new File(outputDir, "a-test1.txt.8.xmi").exists());
    assertTrue(new File(outputDir, "b-test2.dat.9.xmi").exists());
    assertTrue(new File(outputDir, "b-test2.dat.10.xmi").exists());
    assertTrue(new File(outputDir, "b-test2.dat.11.xmi").exists());
    assertTrue(new File(outputDir, "b-test2.dat.12.xmi").exists());

  }
}
