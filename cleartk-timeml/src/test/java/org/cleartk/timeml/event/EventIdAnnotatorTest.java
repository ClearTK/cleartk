/** 
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
package org.cleartk.timeml.event;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.timeml.TimeMLTestBase;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class EventIdAnnotatorTest extends TimeMLTestBase {
  @Test
  public void test() throws UIMAException {
    this.tokenBuilder.buildTokens(
        this.jCas,
        "John thought he would buy shoes for hiking.",
        "John thought he would buy shoes for hiking .");

    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(EventIdAnnotator.class);
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // should be no EVENTS
    List<Event> events = AnnotationRetrieval.getAnnotations(this.jCas, Event.class);
    Assert.assertEquals(0, events.size());

    // add an event
    Event thought = new Event(this.jCas, 5, 12);
    thought.setId("e2");
    thought.addToIndexes();
    Assert.assertEquals("thought", thought.getCoveredText());

    // re-run annotator
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // event should not have changed
    events = AnnotationRetrieval.getAnnotations(this.jCas, Event.class);
    Assert.assertEquals(1, events.size());
    Assert.assertEquals("e2", events.get(0).getId());

    // add an event with no id
    Event buy = new Event(this.jCas, 22, 25);
    buy.addToIndexes();
    Assert.assertEquals("buy", buy.getCoveredText());

    // re-run annotator
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // first event should be the same, second event should have ID
    events = AnnotationRetrieval.getAnnotations(this.jCas, Event.class);
    Assert.assertEquals(2, events.size());
    Assert.assertEquals("e2", events.get(0).getId());
    Assert.assertEquals("e1", events.get(1).getId());

    // add an event and a TLINK
    Event hiking = new Event(this.jCas, 36, 42);
    hiking.addToIndexes();
    Assert.assertEquals("hiking", hiking.getCoveredText());
    TemporalLink tlink = new TemporalLink(this.jCas);
    tlink.setSource(buy);
    tlink.setTarget(hiking);
    tlink.setRelationType("BEFORE");
    tlink.addToIndexes();

    // re-run annotator
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // third event should have ID
    events = AnnotationRetrieval.getAnnotations(this.jCas, Event.class);
    Assert.assertEquals(3, events.size());
    Assert.assertEquals("e2", events.get(0).getId());
    Assert.assertEquals("e1", events.get(1).getId());
    Assert.assertEquals("e3", events.get(2).getId());

    // TLINK IDs should match current EVENT IDs
    List<TemporalLink> tlinks = AnnotationRetrieval.getAnnotations(this.jCas, TemporalLink.class);
    Assert.assertEquals(1, tlinks.size());
    tlink = tlinks.get(0);
    Assert.assertEquals("e1", tlink.getSource().getId());
    Assert.assertEquals("e1", tlink.getEventID());
    Assert.assertEquals("e3", tlink.getTarget().getId());
    Assert.assertEquals("e3", tlink.getRelatedToEvent());
    Assert.assertEquals("BEFORE", tlink.getRelationType());

    // clear all EVENT IDs
    thought.setId(null);
    buy.setId(null);
    hiking.setId(null);

    // re-run annotator
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // EVENT IDs should be in order
    events = AnnotationRetrieval.getAnnotations(this.jCas, Event.class);
    Assert.assertEquals(3, events.size());
    Assert.assertEquals("e1", events.get(0).getId());
    Assert.assertEquals("e2", events.get(1).getId());
    Assert.assertEquals("e3", events.get(2).getId());

    // TLINK IDs should match current EVENT IDs
    tlinks = AnnotationRetrieval.getAnnotations(this.jCas, TemporalLink.class);
    Assert.assertEquals(1, tlinks.size());
    tlink = tlinks.get(0);
    Assert.assertEquals("e2", tlink.getSource().getId());
    Assert.assertEquals("e2", tlink.getEventID());
    Assert.assertEquals("e3", tlink.getTarget().getId());
    Assert.assertEquals("e3", tlink.getRelatedToEvent());
    Assert.assertEquals("BEFORE", tlink.getRelationType());
  }

}
