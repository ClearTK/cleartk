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

import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.test.util.CleartkTestBase;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.util.cr.FilesCollectionReader;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 * 
 */
public class TimeMlGoldAnnotatorTest extends CleartkTestBase {

  @Test
  public void testTimeBank() throws Exception {
    CollectionReader reader = FilesCollectionReader.getCollectionReaderWithView(
        "src/test/resources/data/timeml/wsj_0106.tml",
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME);
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(TimeMlGoldAnnotator.getDescription());
    reader.getNext(this.jCas.getCas());
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // <EVENT eid="e1" class="REPORTING">said</EVENT>
    // <MAKEINSTANCE eventID="e1" eiid="ei128" tense="PAST" aspect="NONE"
    // polarity="POS" pos="VERB"/>
    Event event = JCasUtil.select(this.jCas, Event.class).iterator().next();
    Assert.assertEquals("said", event.getCoveredText());
    Assert.assertEquals("e1", event.getId());
    Assert.assertEquals("ei128", event.getEventInstanceID());
    Assert.assertEquals("REPORTING", event.getEventClass());
    Assert.assertEquals("PAST", event.getTense());
    Assert.assertEquals("NONE", event.getAspect());
    Assert.assertEquals("POS", event.getPolarity());
    Assert.assertEquals("VERB", event.getPos());
    Assert.assertEquals(null, event.getStem());
    Assert.assertEquals(null, event.getModality());
    Assert.assertEquals(null, event.getCardinality());

    // <TIMEX3 tid="t26" type="DATE" value="1989-11-02"
    // temporalFunction="false"
    // functionInDocument="CREATION_TIME">11/02/89</TIMEX3>
    Time docTime = JCasUtil.select(this.jCas, Time.class).iterator().next();
    Assert.assertEquals("11/02/89", docTime.getCoveredText());
    Assert.assertEquals("t26", docTime.getId());
    Assert.assertEquals("DATE", docTime.getTimeType());
    Assert.assertEquals("1989-11-02", docTime.getValue());
    Assert.assertEquals("false", docTime.getTemporalFunction());
    Assert.assertEquals("CREATION_TIME", docTime.getFunctionInDocument());
    Assert.assertTrue(docTime instanceof DocumentCreationTime);

    // <TLINK lid="l1" relType="BEFORE" eventInstanceID="ei128"
    // relatedToTime="t26"/>
    Iterator<TemporalLink> tlinks = JCasUtil.select(this.jCas, TemporalLink.class).iterator();
    TemporalLink tlink0 = tlinks.next();
    Assert.assertEquals("l1", tlink0.getId());
    Assert.assertEquals("BEFORE", tlink0.getRelationType());
    Assert.assertEquals("e1", tlink0.getSource().getId());
    Assert.assertEquals("t26", tlink0.getTarget().getId());
    Assert.assertEquals(event, tlink0.getSource());

    // <TLINK lid="l2" relType="SIMULTANEOUS" eventInstanceID="ei131"
    // relatedToEventInstance="ei130"/>
    TemporalLink tlink1 = tlinks.next();
    Assert.assertEquals("l2", tlink1.getId());
    Assert.assertEquals("SIMULTANEOUS", tlink1.getRelationType());
    Assert.assertEquals("e5", tlink1.getSource().getId());
    Assert.assertEquals("e4", tlink1.getTarget().getId());
  }

  @Test
  public void testTempEval() throws Exception {
    CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        FilesCollectionReader.class,
        FilesCollectionReader.PARAM_VIEW_NAME,
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME,
        FilesCollectionReader.PARAM_ROOT_FILE,
        "src/test/resources/data/timeml/AP900815-0044.tml");
    AnalysisEngineDescription engine = AnalysisEngineFactory.createEngineDescription(TimeMlGoldAnnotator.class);
    JCas jcas = new JCasIterable(reader, engine).iterator().next();

    // <EVENT eid="e5" class="STATE" stem="face" aspect="NONE"
    // tense="PRESPART" polarity="POS" pos="VERB">facing</EVENT>
    Event event = JCasUtil.select(jcas, Event.class).iterator().next();
    Assert.assertEquals("facing", event.getCoveredText());
    Assert.assertEquals("e5", event.getId());
    Assert.assertEquals(null, event.getEventInstanceID());
    Assert.assertEquals("STATE", event.getEventClass());
    Assert.assertEquals("PRESPART", event.getTense());
    Assert.assertEquals("NONE", event.getAspect());
    Assert.assertEquals("POS", event.getPolarity());
    Assert.assertEquals("VERB", event.getPos());
    Assert.assertEquals("face", event.getStem());
    Assert.assertEquals(null, event.getModality());
    Assert.assertEquals(null, event.getCardinality());

    // <TIMEX3 tid="t3" type="TIME" value="1990-08-15T13:37"
    // temporalFunction="false" functionInDocument="CREATION_TIME">08-15-90
    // 1337EDT</TIMEX3>
    Time docTime = JCasUtil.select(jcas, Time.class).iterator().next();
    Assert.assertEquals("08-15-90 1337EDT", docTime.getCoveredText());
    Assert.assertEquals("t3", docTime.getId());
    Assert.assertEquals("TIME", docTime.getTimeType());
    Assert.assertEquals("1990-08-15T13:37", docTime.getValue());
    Assert.assertEquals("false", docTime.getTemporalFunction());
    Assert.assertEquals("CREATION_TIME", docTime.getFunctionInDocument());

    // <TLINK lid="l6" relType="OVERLAP" eventID="e54" relatedToTime="t56"
    // task="A"/>
    TemporalLink tlink = JCasUtil.selectByIndex(jcas, TemporalLink.class, 5);
    Assert.assertEquals("l6", tlink.getId());
    Assert.assertEquals("OVERLAP", tlink.getRelationType());
    Assert.assertEquals("e54", tlink.getSource().getId());
    Assert.assertEquals("t56", tlink.getTarget().getId());
  }

  @Test
  public void testNoTLINKs() throws Exception {
    CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
        FilesCollectionReader.class,
        FilesCollectionReader.PARAM_VIEW_NAME,
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME,
        FilesCollectionReader.PARAM_ROOT_FILE,
        "src/test/resources/data/timeml",
        FilesCollectionReader.PARAM_SUFFIXES,
        new String[] { ".tml" });
    AnalysisEngineDescription engine = AnalysisEngineFactory.createEngineDescription(
        TimeMlGoldAnnotator.class,
        TimeMlGoldAnnotator.PARAM_LOAD_TLINKS,
        false);
    for (JCas jcas : new JCasIterable(reader, engine)) {
      Assert.assertTrue(JCasUtil.select(jcas, Event.class).size() > 0);
      Assert.assertTrue(JCasUtil.select(jcas, Time.class).size() > 0);
      Assert.assertEquals(0, JCasUtil.select(jcas, TemporalLink.class).size());
    }
  }

  @Test
  public void testAnchorTime() throws Exception {
    JCas view = this.jCas.createView(TimeMlGoldAnnotator.TIMEML_VIEW_NAME);
    view.setDocumentText("<TimeML>\n"
        + "<TIMEX3 tid='t1' anchorTimeID='t0' value='2010-05-27'>One year ago</TIMEX3>...\n"
        + "<TIMEX3 tid='t0' value='2011-05-27'>May 27, 2011</TIMEX3>\n</TimeML>");
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(TimeMlGoldAnnotator.getDescription());
    engine.process(this.jCas);
    Iterator<Time> times = JCasUtil.select(this.jCas, Time.class).iterator();
    Time time1 = times.next();
    Time time0 = times.next();
    Assert.assertFalse(times.hasNext());
    Assert.assertEquals("2011-05-27", time0.getValue());
    Assert.assertEquals("2010-05-27", time1.getValue());
    Assert.assertEquals(time0, time1.getAnchorTime());
  }
}
