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
package org.cleartk.corpus.timeml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.util.cr.FilesCollectionReader;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.uima.fit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 * 
 */
public class TempEval2007WriterTest extends CleartkTestBase {

  private File inputFile;

  private File outputFile;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    inputFile = new File("src/test/resources/data/timeml/test.foo");
    outputFile = new File(outputDirectory, "test.foo.tml");
  }

  @Test
  public void test() throws Exception, JDOMException {
    CollectionReader reader = FilesCollectionReader.getCollectionReaderWithView(
        this.inputFile.getPath(),
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME);
    AnalysisEngine annotator = AnalysisEngineFactory.createEngine(TimeMlGoldAnnotator.getDescription());
    AnalysisEngine writer = AnalysisEngineFactory.createEngine(TempEval2007Writer.getDescription(this.outputDirectory.getPath()));

    reader.getNext(this.jCas.getCas());
    annotator.process(this.jCas);
    writer.process(this.jCas);
    reader.close();
    annotator.collectionProcessComplete();
    writer.collectionProcessComplete();

    String expected = FileUtils.file2String(this.inputFile);
    String actual = FileUtils.file2String(this.outputFile);
    this.assertEquals(this.getRoot(expected), this.getRoot(actual));
  }

  @Test
  public void testToTimeML() throws Throwable {
    this.jCas.setDocumentText("\nHe bought milk yesterday.\n\n\n");
    Event event = new Event(this.jCas, 4, 10);
    event.setId("e1");
    event.setEventInstanceID("ei1");
    event.addToIndexes();
    Assert.assertEquals("bought", event.getCoveredText());

    Time time = new Time(this.jCas, 16, 25);
    time.setId("t1");
    time.addToIndexes();
    Assert.assertEquals("yesterday", time.getCoveredText());

    TemporalLink tlink1 = new TemporalLink(this.jCas, 27, 27);
    tlink1.setSource(event);
    tlink1.setTarget(time);
    tlink1.setRelationType("OVERLAP");
    tlink1.addToIndexes();

    TemporalLink tlink2 = new TemporalLink(this.jCas, 28, 28);
    tlink2.setSource(time);
    tlink2.setTarget(event);
    tlink2.setRelationType("OVERLAP");
    tlink2.addToIndexes();

    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TimeML>\n"
        + "He <EVENT eid=\"e1\">bought</EVENT> milk <TIMEX3 tid=\"t1\">yesterday</TIMEX3>.\n"
        + "<TLINK relType=\"OVERLAP\" eventID=\"e1\" relatedToTime=\"t1\"/>\n"
        + "<TLINK relType=\"OVERLAP\" timeID=\"t1\" relatedToEvent=\"e1\"/>\n"
        + "</TimeML>";
    Assert.assertEquals(expected, TempEval2007Writer.toTimeML(this.jCas).replaceAll("\r", ""));
  }

  @Test
  public void testDescriptor() throws UIMAException {
    try {
      AnalysisEngineFactory.createEngine(TempEval2007Writer.class);
      Assert.fail("expected failure with no OutputDirectory specified");
    } catch (ResourceInitializationException e) {
    }

    AnalysisEngine engine = AnalysisEngineFactory.createEngine(
        TempEval2007Writer.class,
        TempEval2007Writer.PARAM_OUTPUT_DIRECTORY_NAME,
        this.outputDirectory.getPath());
    Assert.assertEquals(
        this.outputDirectory.getPath(),
        engine.getConfigParameterValue(TempEval2007Writer.PARAM_OUTPUT_DIRECTORY_NAME));
    engine.collectionProcessComplete();
  }

  private Element getRoot(String xml) throws JDOMException, IOException {
    SAXBuilder builder = new SAXBuilder();
    builder.setDTDHandler(null);
    return builder.build(new StringReader(xml)).getRootElement();
  }

  private void assertEquals(Element element1, Element element2) {
    Assert.assertEquals(element1.getName(), element2.getName());
    Assert.assertEquals(this.getAttributes(element1), this.getAttributes(element2));
    List<Content> children1 = element1.getContent();
    List<Content> children2 = element2.getContent();
    Assert.assertEquals(children1.size(), children2.size());
    for (int i = 0; i < children1.size(); i++) {
      Content child1 = children1.get(0);
      Content child2 = children2.get(0);
      if (child1 instanceof Element) {
        this.assertEquals((Element) child1, (Element) child2);
      } else {
        Assert.assertEquals(child1.getValue(), child2.getValue());
      }
    }
  }

  private Map<String, String> getAttributes(Element element) {
    Map<String, String> attributes = new HashMap<String, String>();
    for (Object attrObj : element.getAttributes()) {
      Attribute attribute = (Attribute) attrObj;
      attributes.put(attribute.getName(), attribute.getValue());
    }
    return attributes;
  }
}
