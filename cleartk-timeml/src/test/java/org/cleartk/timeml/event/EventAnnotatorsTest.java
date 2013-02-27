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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.timeml.TimeMLTestBase;
import org.cleartk.timeml.type.Event;
import org.junit.Test;
import org.uimafit.util.JCasUtil;

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
        EventAnnotator.FACTORY.getAnnotatorDescription(),
        EventTenseAnnotator.FACTORY.getAnnotatorDescription(),
        EventAspectAnnotator.FACTORY.getAnnotatorDescription(),
        EventClassAnnotator.FACTORY.getAnnotatorDescription(),
        EventPolarityAnnotator.FACTORY.getAnnotatorDescription(),
        EventModalityAnnotator.FACTORY.getAnnotatorDescription());

    for (AnalysisEngineDescription desc : descs) {
      AnalysisEngine engine = UIMAFramework.produceAnalysisEngine(desc);
      engine.process(this.jCas);
      engine.collectionProcessComplete();
    }
    Collection<Event> events = JCasUtil.select(jCas, Event.class);
    Assert.assertEquals(2, events.size());

    Iterator<Event> eventsIter = events.iterator();
    Event thinking = eventsIter.next();
    Assert.assertEquals("e1", thinking.getId());
    Assert.assertEquals("thinking", thinking.getCoveredText());
    Assert.assertEquals("PAST", thinking.getTense());
    Assert.assertEquals("PROGRESSIVE", thinking.getAspect());
    Assert.assertEquals("I_STATE", thinking.getEventClass());
    Assert.assertEquals("POS", thinking.getPolarity());
    Assert.assertEquals("none", thinking.getModality());

    Event eaten = eventsIter.next();
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

    File eventDir = this.folder.newFolder("event");
    File tenseDir = this.folder.newFolder("tense");
    File aspectDir = this.folder.newFolder("aspect");
    File classDir = this.folder.newFolder("class");
    File polarityDir = this.folder.newFolder("polarity");
    File modalityDir = this.folder.newFolder("modality");

    List<AnalysisEngineDescription> descs = Arrays.asList(
        EventAnnotator.FACTORY.getWriterDescription(eventDir),
        EventTenseAnnotator.FACTORY.getWriterDescription(tenseDir),
        EventAspectAnnotator.FACTORY.getWriterDescription(aspectDir),
        EventClassAnnotator.FACTORY.getWriterDescription(classDir),
        EventPolarityAnnotator.FACTORY.getWriterDescription(polarityDir),
        EventModalityAnnotator.FACTORY.getWriterDescription(modalityDir));

    for (AnalysisEngineDescription desc : descs) {
      AnalysisEngine engine = UIMAFramework.produceAnalysisEngine(desc);
      engine.process(this.jCas);
      engine.collectionProcessComplete();
    }

    List<File> paths = Arrays.asList(
        eventDir,
        tenseDir,
        aspectDir,
        classDir,
        polarityDir,
        modalityDir);
    for (File path : paths) {
      boolean hasTrainingData = false;
      for (File file : path.listFiles()) {
        if (file.getName().startsWith("training-data")) {
          hasTrainingData = true;
        }
      }
      Assert.assertTrue("no training data found in " + path, hasTrainingData);
    }
  }
}
