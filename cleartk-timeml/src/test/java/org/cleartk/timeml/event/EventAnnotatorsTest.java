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
package org.cleartk.timeml.event;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.timeml.TimeMLTestBase;
import org.cleartk.timeml.type.Event;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Test the TimeML EVENT identification and classification annotators.
 * 
 * @author Steven Bethard
 */
public class EventAnnotatorsTest extends TimeMLTestBase {

  @Test
  public void testModel() throws UIMAException {
    this.tokenBuilder.buildTokens(
        this.jCas,
        "He was thinking he would not have eaten yesterday.",
        "He was thinking he would not have eaten yesterday .",
        "PRP VBD VBG PRP MD RB VBD VBN RB .",
        "he was think he would not have eaten yesterday .");

    List<AnalysisEngineDescription> descs = Arrays.asList(
        EventAnnotator.getAnnotatorDescription(),
        EventTenseAnnotator.getAnnotatorDescription(),
        EventAspectAnnotator.getAnnotatorDescription(),
        EventClassAnnotator.getAnnotatorDescription(),
        EventPolarityAnnotator.getAnnotatorDescription(),
        EventModalityAnnotator.getAnnotatorDescription());

    for (AnalysisEngineDescription desc : descs) {
      AnalysisEngine engine = UIMAFramework.produceAnalysisEngine(desc);
      engine.process(this.jCas);
      engine.collectionProcessComplete();
    }
    List<Event> events = AnnotationRetrieval.getAnnotations(jCas, Event.class);
    Assert.assertEquals(2, events.size());

    Event thinking = events.get(0);
    Assert.assertEquals("e1", thinking.getId());
    Assert.assertEquals("thinking", thinking.getCoveredText());
    Assert.assertEquals("PAST", thinking.getTense());
    Assert.assertEquals("PROGRESSIVE", thinking.getAspect());
    Assert.assertEquals("I_STATE", thinking.getEventClass());
    Assert.assertEquals("POS", thinking.getPolarity());
    Assert.assertEquals("none", thinking.getModality());

    Event eaten = events.get(1);
    Assert.assertEquals("e2", eaten.getId());
    Assert.assertEquals("eaten", eaten.getCoveredText());
    Assert.assertEquals("PRESENT", eaten.getTense());
    Assert.assertEquals("PERFECTIVE", eaten.getAspect());
    Assert.assertEquals("OCCURRENCE", eaten.getEventClass());
    Assert.assertEquals("NEG", eaten.getPolarity());
    Assert.assertEquals("would", eaten.getModality());
  }

  @Test
  public void testDataWriters() throws Exception {
    this.tokenBuilder.buildTokens(
        this.jCas,
        "She ate dinner.",
        "She ate dinner .",
        "PRP VBD NN .",
        "she ate dinner .");
    Event ate = new Event(this.jCas, 4, 7);
    ate.setTense("PAST");
    ate.setEventClass("OCCURRENCE");
    ate.setPolarity("POS");
    ate.addToIndexes();

    String eventDir = this.folder.newFolder("event").getPath();
    String tenseDir = this.folder.newFolder("tense").getPath();
    String aspectDir = this.folder.newFolder("aspect").getPath();
    String classDir = this.folder.newFolder("class").getPath();
    String polarityDir = this.folder.newFolder("polarity").getPath();
    String modalityDir = this.folder.newFolder("modality").getPath();

    List<AnalysisEngineDescription> descs = Arrays.asList(
        EventAnnotator.getWriterDescription(eventDir),
        EventTenseAnnotator.getWriterDescription(tenseDir),
        EventAspectAnnotator.getWriterDescription(aspectDir),
        EventClassAnnotator.getWriterDescription(classDir),
        EventPolarityAnnotator.getWriterDescription(polarityDir),
        EventModalityAnnotator.getWriterDescription(modalityDir));

    for (AnalysisEngineDescription desc : descs) {
      AnalysisEngine engine = UIMAFramework.produceAnalysisEngine(desc);
      engine.process(this.jCas);
      engine.collectionProcessComplete();
    }

    List<String> paths = Arrays.asList(
        eventDir,
        tenseDir,
        aspectDir,
        classDir,
        polarityDir,
        modalityDir);
    for (String path : paths) {
      boolean hasTrainingData = false;
      for (File file : new File(path).listFiles()) {
        if (file.getName().startsWith("training-data")) {
          hasTrainingData = true;
        }
      }
      Assert.assertTrue("no training dat found in " + path, hasTrainingData);
    }
  }

  @Test
  public void testAnnotateMain() throws Exception {
    File textFile = this.folder.newFile("event.annotate.txt");
    File tmlFile = new File(this.outputDirectory, textFile.getName() + ".tml");
    FileUtils.writeStringToFile(textFile, "He said he bought a bed today.");
    EventAnnotate.main(textFile.getPath(), this.outputDirectoryName);
    String text = FileUtils.readFileToString(tmlFile);
    Assert.assertTrue("Text must contain TimeML element", text.contains("<TimeML>"));
    Assert.assertTrue("Text must contain EVENT element", text.contains("<EVENT"));
  }
}
