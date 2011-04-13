/*
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
package org.cleartk.timeml.corpus;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.timeml.TimeMLTestBase;
import org.cleartk.timeml.TimeMLViewName;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Time;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010ReaderAnnotatorWriterTest extends TimeMLTestBase {

  @Override
  public String[] getTypeSystemDescriptorNames() {
    return new String[] { "org.cleartk.timeml.TypeSystem", "org.cleartk.token.TypeSystem" };
  }

  // @formatter:off
  public static final String WSJ_0032_BASE_SEGMENTATION =
        "wsj_0032	0	0	Italian\n" +
        "wsj_0032	0	1	chemical\n" +
        "wsj_0032	0	2	giant\n" +
        "wsj_0032	0	3	Montedison\n" +
        "wsj_0032	0	4	S.p.A\n" +
        "wsj_0032	0	5	.\n" +
        "wsj_0032	0	6	,\n" +
        "wsj_0032	0	7	through\n" +
        "wsj_0032	0	8	its\n" +
        "wsj_0032	0	9	Montedison\n" +
        "wsj_0032	0	10	Acquisition\n" +
        "wsj_0032	0	11	N.V.\n" +
        "wsj_0032	0	12	indirect\n" +
        "wsj_0032	0	13	unit\n" +
        "wsj_0032	0	14	,\n" +
        "wsj_0032	0	15	began\n" +
        "wsj_0032	0	16	its\n" +
        "wsj_0032	0	17	$37-a-share\n" +
        "wsj_0032	0	18	tender\n" +
        "wsj_0032	0	19	offer\n" +
        "wsj_0032	0	20	for\n" +
        "wsj_0032	0	21	all\n" +
        "wsj_0032	0	22	the\n" +
        "wsj_0032	0	23	common\n" +
        "wsj_0032	0	24	shares\n" +
        "wsj_0032	0	25	outstanding\n" +
        "wsj_0032	0	26	of\n" +
        "wsj_0032	0	27	Erbamont\n" +
        "wsj_0032	0	28	N.V.\n" +
        "wsj_0032	0	29	,\n" +
        "wsj_0032	0	30	a\n" +
        "wsj_0032	0	31	maker\n" +
        "wsj_0032	0	32	of\n" +
        "wsj_0032	0	33	pharmaceuticals\n" +
        "wsj_0032	0	34	incorporated\n" +
        "wsj_0032	0	35	in\n" +
        "wsj_0032	0	36	the\n" +
        "wsj_0032	0	37	Netherlands\n" +
        "wsj_0032	0	38	.\n" +
        "wsj_0032	1	0	The\n" +
        "wsj_0032	1	1	offer\n" +
        "wsj_0032	1	2	,\n" +
        "wsj_0032	1	3	advertised\n" +
        "wsj_0032	1	4	in\n" +
        "wsj_0032	1	5	today\n" +
        "wsj_0032	1	6	's\n" +
        "wsj_0032	1	7	editions\n" +
        "wsj_0032	1	8	of\n" +
        "wsj_0032	1	9	The\n" +
        "wsj_0032	1	10	Wall\n" +
        "wsj_0032	1	11	Street\n" +
        "wsj_0032	1	12	Journal\n" +
        "wsj_0032	1	13	,\n" +
        "wsj_0032	1	14	is\n" +
        "wsj_0032	1	15	scheduled\n" +
        "wsj_0032	1	16	to\n" +
        "wsj_0032	1	17	expire\n" +
        "wsj_0032	1	18	at\n" +
        "wsj_0032	1	19	the\n" +
        "wsj_0032	1	20	the\n" +
        "wsj_0032	1	21	end\n" +
        "wsj_0032	1	22	of\n" +
        "wsj_0032	1	23	November\n" +
        "wsj_0032	1	24	.\n" +
        "wsj_0032	2	0	Montedison\n" +
        "wsj_0032	2	1	currently\n" +
        "wsj_0032	2	2	owns\n" +
        "wsj_0032	2	3	about\n" +
        "wsj_0032	2	4	72%\n" +
        "wsj_0032	2	5	of\n" +
        "wsj_0032	2	6	Erbamont\n" +
        "wsj_0032	2	7	's\n" +
        "wsj_0032	2	8	common\n" +
        "wsj_0032	2	9	shares\n" +
        "wsj_0032	2	10	outstanding\n" +
        "wsj_0032	2	11	.\n" +
        "wsj_0032	3	0	The\n" +
        "wsj_0032	3	1	offer\n" +
        "wsj_0032	3	2	is\n" +
        "wsj_0032	3	3	being\n" +
        "wsj_0032	3	4	launched\n" +
        "wsj_0032	3	5	pursuant\n" +
        "wsj_0032	3	6	to\n" +
        "wsj_0032	3	7	a\n" +
        "wsj_0032	3	8	previously\n" +
        "wsj_0032	3	9	announced\n" +
        "wsj_0032	3	10	agreement\n" +
        "wsj_0032	3	11	between\n" +
        "wsj_0032	3	12	the\n" +
        "wsj_0032	3	13	companies\n" +
        "wsj_0032	3	14	.\n";

  public static final String SJMN91_BASE_SEGMENTATION =
        "SJMN91-06338157	0	0	.\n" +
        "SJMN91-06338157	1	0	.\n";

  public static final String BASE_SEGMENTATION = WSJ_0032_BASE_SEGMENTATION + SJMN91_BASE_SEGMENTATION;

  public static final String EVENT_ATTRIBUTES =
        "wsj_0032	0	15	event	e1	1	polarity	POS\n" +
        "wsj_0032	0	15	event	e1	1	modality	NONE\n" +
        "wsj_0032	0	15	event	e1	1	pos	VERB\n" +
        "wsj_0032	0	15	event	e1	1	tense	PAST\n" +
        "wsj_0032	0	15	event	e1	1	aspect	NONE\n" +
        "wsj_0032	0	15	event	e1	1	class	ASPECTUAL\n" +
        "wsj_0032	0	19	event	e2	1	polarity	POS\n" +
        "wsj_0032	0	19	event	e2	1	modality	NONE\n" +
        "wsj_0032	0	19	event	e2	1	pos	NOUN\n" +
        "wsj_0032	0	19	event	e2	1	tense	NONE\n" +
        "wsj_0032	0	19	event	e2	1	aspect	NONE\n" +
        "wsj_0032	0	19	event	e2	1	class	OCCURRENCE\n" +
        "wsj_0032	1	1	event	e3	1	polarity	POS\n" +
        "wsj_0032	1	1	event	e3	1	modality	NONE\n" +
        "wsj_0032	1	1	event	e3	1	pos	NOUN\n" +
        "wsj_0032	1	1	event	e3	1	tense	NONE\n" +
        "wsj_0032	1	1	event	e3	1	aspect	NONE\n" +
        "wsj_0032	1	1	event	e3	1	class	OCCURRENCE\n" +
        "wsj_0032	1	3	event	e4	1	polarity	POS\n" +
        "wsj_0032	1	3	event	e4	1	modality	NONE\n" +
        "wsj_0032	1	3	event	e4	1	pos	VERB\n" +
        "wsj_0032	1	3	event	e4	1	tense	PASTPART\n" +
        "wsj_0032	1	3	event	e4	1	aspect	NONE\n" +
        "wsj_0032	1	3	event	e4	1	class	OCCURRENCE\n" +
        "wsj_0032	1	15	event	e5	1	polarity	POS\n" +
        "wsj_0032	1	15	event	e5	1	modality	NONE\n" +
        "wsj_0032	1	15	event	e5	1	pos	VERB\n" +
        "wsj_0032	1	15	event	e5	1	tense	PRESENT\n" +
        "wsj_0032	1	15	event	e5	1	aspect	NONE\n" +
        "wsj_0032	1	15	event	e5	1	class	I_ACTION\n" +
        "wsj_0032	1	17	event	e6	1	polarity	POS\n" +
        "wsj_0032	1	17	event	e6	1	modality	NONE\n" +
        "wsj_0032	1	17	event	e6	1	pos	VERB\n" +
        "wsj_0032	1	17	event	e6	1	tense	INFINITIVE\n" +
        "wsj_0032	1	17	event	e6	1	aspect	NONE\n" +
        "wsj_0032	1	17	event	e6	1	class	OCCURRENCE\n" +
        "wsj_0032	2	2	event	e7	1	polarity	POS\n" +
        "wsj_0032	2	2	event	e7	1	modality	NONE\n" +
        "wsj_0032	2	2	event	e7	1	pos	VERB\n" +
        "wsj_0032	2	2	event	e7	1	tense	PRESENT\n" +
        "wsj_0032	2	2	event	e7	1	aspect	NONE\n" +
        "wsj_0032	2	2	event	e7	1	class	STATE\n" +
        "wsj_0032	3	1	event	e8	1	polarity	POS\n" +
        "wsj_0032	3	1	event	e8	1	modality	NONE\n" +
        "wsj_0032	3	1	event	e8	1	pos	NOUN\n" +
        "wsj_0032	3	1	event	e8	1	tense	NONE\n" +
        "wsj_0032	3	1	event	e8	1	aspect	NONE\n" +
        "wsj_0032	3	1	event	e8	1	class	OCCURRENCE\n" +
        "wsj_0032	3	4	event	e9	1	polarity	POS\n" +
        "wsj_0032	3	4	event	e9	1	modality	NONE\n" +
        "wsj_0032	3	4	event	e9	1	pos	VERB\n" +
        "wsj_0032	3	4	event	e9	1	tense	PRESENT\n" +
        "wsj_0032	3	4	event	e9	1	aspect	PROGRESSIVE\n" +
        "wsj_0032	3	4	event	e9	1	class	I_ACTION\n" +
        "wsj_0032	3	9	event	e10	1	polarity	POS\n" +
        "wsj_0032	3	9	event	e10	1	modality	NONE\n" +
        "wsj_0032	3	9	event	e10	1	pos	ADJECTIVE\n" +
        "wsj_0032	3	9	event	e10	1	tense	NONE\n" +
        "wsj_0032	3	9	event	e10	1	aspect	NONE\n" +
        "wsj_0032	3	9	event	e10	1	class	REPORTING\n" +
        "wsj_0032	3	10	event	e11	1	polarity	POS\n" +
        "wsj_0032	3	10	event	e11	1	modality	NONE\n" +
        "wsj_0032	3	10	event	e11	1	pos	NOUN\n" +
        "wsj_0032	3	10	event	e11	1	tense	NONE\n" +
        "wsj_0032	3	10	event	e11	1	aspect	NONE\n" +
        "wsj_0032	3	10	event	e11	1	class	OCCURRENCE\n";

  public static final String EVENT_EXTENTS =
        "wsj_0032	0	15	event	e1	1\n" +
        "wsj_0032	0	19	event	e2	1\n" +
        "wsj_0032	1	1	event	e3	1\n" +
        "wsj_0032	1	3	event	e4	1\n" +
        "wsj_0032	1	15	event	e5	1\n" +
        "wsj_0032	1	17	event	e6	1\n" +
        "wsj_0032	2	2	event	e7	1\n" +
        "wsj_0032	3	1	event	e8	1\n" +
        "wsj_0032	3	4	event	e9	1\n" +
        "wsj_0032	3	9	event	e10	1\n" +
        "wsj_0032	3	10	event	e11	1\n";

  public static final String TIMEX_ATTRIBUTES =
        "wsj_0032	1	5	timex3	t19	1	value	1989-11-02\n" +
        "wsj_0032	1	5	timex3	t19	1	type	DATE\n" +
        "wsj_0032	1	20	timex3	t18	1	value	1989-11\n" +
        "wsj_0032	1	20	timex3	t18	1	type	TIME\n" +
        "wsj_0032	2	1	timex3	t17	1	value	PRESENT_REF\n" +
        "wsj_0032	2	1	timex3	t17	1	type	DATE\n" +
        "wsj_0032	3	8	timex3	t21	1	value	PAST_REF\n" +
        "wsj_0032	3	8	timex3	t21	1	type	TIME\n";

  public static final String TIMEX_EXTENTS =
        "wsj_0032	1	5	timex3	t19	1\n" +
        "wsj_0032	1	20	timex3	t18	1\n" +
        "wsj_0032	1	21	timex3	t18	1\n" +
        "wsj_0032	1	22	timex3	t18	1\n" +
        "wsj_0032	1	23	timex3	t18	1\n" +
        "wsj_0032	2	1	timex3	t17	1\n" +
        "wsj_0032	3	8	timex3	t21	1\n";

  public static final String TLINKS_DCT_EVENT =
        "wsj_0032	e1	t0	BEFORE\n" +
        "wsj_0032	e2	t0	OVERLAP\n" +
        "wsj_0032	e3	t0	OVERLAP\n" +
        "wsj_0032	e8	t0	OVERLAP\n" +
        "wsj_0032	e11	t0	BEFORE\n";

  public static final String TLINKS_MAIN_EVENTS =
        "wsj_0032	e1	e5	BEFORE\n" +
        "wsj_0032	e5	e7	OVERLAP\n" +
        "wsj_0032	e7	e9	OVERLAP\n";

  public static final String TLINKS_SUBORDINATED_EVENTS =
        "wsj_0032	e4	e3	OVERLAP\n" +
        "wsj_0032	e5	e6	BEFORE\n" +
        "wsj_0032	e9	e8	BEFORE-OR-OVERLAP\n" +
        "wsj_0032	e10	e11	OVERLAP\n";

  public static final String TLINKS_TIMEX_EVENT =
        "wsj_0032	e3	t19	OVERLAP\n" +
        "wsj_0032	e8	t21	AFTER\n" +
        "wsj_0032	e11	t21	OVERLAP\n";
    
  public static final String DCT =
        "wsj_0032	19891102\n";
  
  // @formatter:on

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.write("data/base-segmentation.tab", BASE_SEGMENTATION);
    this.write("key/event-attributes.tab", EVENT_ATTRIBUTES);
    this.write("key/event-extents.tab", EVENT_EXTENTS);
    this.write("key/timex-attributes.tab", TIMEX_ATTRIBUTES);
    this.write("key/timex-extents.tab", TIMEX_EXTENTS);
    this.write("key/tlinks-dct-event.tab", TLINKS_DCT_EVENT);
    this.write("key/tlinks-main-events.tab", TLINKS_MAIN_EVENTS);
    this.write("key/tlinks-subordinated-events.tab", TLINKS_SUBORDINATED_EVENTS);
    this.write("key/tlinks-timex-event.tab", TLINKS_TIMEX_EVENT);
    this.write("dct-en.txt", DCT);
  }

  private void write(String filePath, String text) throws IOException {
    FileUtils.writeStringToFile(new File(this.outputDirectory, filePath), text);
  }

  @Test
  public void testTempEval2010CollectionReader() throws Exception {
    CollectionReader reader = TempEval2010CollectionReader.getCollectionReader(this.outputDirectory.getPath());
    Assert.assertTrue(reader.hasNext());
    reader.getNext(this.jCas.getCas());
    assertViewText(WSJ_0032_BASE_SEGMENTATION, TimeMLViewName.TEMPEVAL_BASE_SEGMENTATION);
    assertViewText(EVENT_ATTRIBUTES, TimeMLViewName.TEMPEVAL_EVENT_ATTRIBUTES);
    assertViewText(EVENT_EXTENTS, TimeMLViewName.TEMPEVAL_EVENT_EXTENTS);
    assertViewText(TIMEX_ATTRIBUTES, TimeMLViewName.TEMPEVAL_TIMEX_ATTRIBUTES);
    assertViewText(TIMEX_EXTENTS, TimeMLViewName.TEMPEVAL_TIMEX_EXTENTS);
    assertViewText(TLINKS_DCT_EVENT, TimeMLViewName.TEMPEVAL_TLINK_DCT_EVENT);
    assertViewText(TLINKS_MAIN_EVENTS, TimeMLViewName.TEMPEVAL_TLINK_MAIN_EVENTS);
    assertViewText(TLINKS_SUBORDINATED_EVENTS, TimeMLViewName.TEMPEVAL_TLINK_SUBORDINATED_EVENTS);
    assertViewText(TLINKS_TIMEX_EVENT, TimeMLViewName.TEMPEVAL_TLINK_TIMEX_EVENT);
    assertViewText(DCT, TimeMLViewName.TEMPEVAL_DCT);

    Assert.assertTrue(reader.hasNext());
    this.jCas.reset();
    reader.getNext(this.jCas.getCas());
    assertViewText(SJMN91_BASE_SEGMENTATION, TimeMLViewName.TEMPEVAL_BASE_SEGMENTATION);
    assertViewText("", TimeMLViewName.TEMPEVAL_EVENT_ATTRIBUTES);
    assertViewText("", TimeMLViewName.TEMPEVAL_EVENT_EXTENTS);
    assertViewText("", TimeMLViewName.TEMPEVAL_TIMEX_ATTRIBUTES);
    assertViewText("", TimeMLViewName.TEMPEVAL_TIMEX_EXTENTS);
    assertViewText("", TimeMLViewName.TEMPEVAL_TLINK_DCT_EVENT);
    assertViewText("", TimeMLViewName.TEMPEVAL_TLINK_MAIN_EVENTS);
    assertViewText("", TimeMLViewName.TEMPEVAL_TLINK_SUBORDINATED_EVENTS);
    assertViewText("", TimeMLViewName.TEMPEVAL_TLINK_TIMEX_EVENT);
    assertViewText("", TimeMLViewName.TEMPEVAL_DCT);
  }

  private void assertViewText(String expected, String viewName) throws CASException {
    Assert.assertEquals(expected, this.jCas.getView(viewName).getDocumentText());
  }

  @Test
  public void testTempEval2010GoldAnnotator() throws Exception {
    CollectionReader reader = TempEval2010CollectionReader.getCollectionReader(this.outputDirectory.getPath());
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(TempEval2010GoldAnnotator.getDescription());
    reader.getNext(this.jCas.getCas());
    engine.process(this.jCas);

    String expectedText = "Italian chemical giant Montedison S.p.A . , through its Montedison Acquisition N.V. indirect unit , began its $37-a-share tender offer for all the common shares outstanding of Erbamont N.V. , a maker of pharmaceuticals incorporated in the Netherlands .\n"
        + "The offer , advertised in today 's editions of The Wall Street Journal , is scheduled to expire at the the end of November .\n"
        + "Montedison currently owns about 72% of Erbamont 's common shares outstanding .\n"
        + "The offer is being launched pursuant to a previously announced agreement between the companies .";
    Assert.assertEquals(expectedText, this.jCas.getDocumentText().trim());

    Collection<Sentence> sentences = JCasUtil.select(this.jCas, Sentence.class);
    Assert.assertEquals(4, sentences.size());
    Assert.assertEquals(
        "Montedison currently owns about 72% of Erbamont 's common shares outstanding .",
        itemAtIndex(sentences, 2).getCoveredText());

    Collection<Token> tokens = JCasUtil.select(this.jCas, Token.class);
    Assert.assertEquals(91, tokens.size());
    Assert.assertEquals("Montedison", itemAtIndex(tokens, 3).getCoveredText());

    Collection<Event> events = JCasUtil.select(this.jCas, Event.class);
    Assert.assertEquals(11, events.size());
    Event e10 = itemAtIndex(events, 9);
    Assert.assertEquals("e10", e10.getId());
    Assert.assertEquals("announced", e10.getCoveredText());
    Assert.assertEquals("POS", e10.getPolarity());
    Assert.assertEquals("NONE", e10.getModality());
    Assert.assertEquals("ADJECTIVE", e10.getPos());
    Assert.assertEquals("NONE", e10.getTense());
    Assert.assertEquals("NONE", e10.getAspect());
    Assert.assertEquals("REPORTING", e10.getEventClass());

    Collection<Time> times = JCasUtil.select(this.jCas, Time.class);
    Assert.assertEquals(5, times.size());
    Time t18 = itemAtIndex(times, 2);
    Assert.assertEquals("t18", t18.getId());
    Assert.assertEquals("the end of November", t18.getCoveredText());
    Assert.assertEquals("1989-11", t18.getValue());
    Assert.assertEquals("TIME", t18.getTimeType()); // wrong, but that's what the data says

    Collection<DocumentCreationTime> dcts = JCasUtil.select(this.jCas, DocumentCreationTime.class);
    Assert.assertEquals(1, dcts.size());
    Assert.assertEquals(itemAtIndex(times, 0), itemAtIndex(dcts, 0));
    Time dct = itemAtIndex(dcts, 0);
    Assert.assertEquals("t0", dct.getId());
    Assert.assertEquals("1989-11-02", dct.getValue());
    Assert.assertEquals("CREATION_TIME", dct.getFunctionInDocument());

    Collection<TemporalLink> tlinks = JCasUtil.select(this.jCas, TemporalLink.class);
    Assert.assertEquals(15, tlinks.size());

    this.jCas.reset();
    reader.getNext(this.jCas.getCas());
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    sentences = JCasUtil.select(this.jCas, Sentence.class);
    Assert.assertEquals(2, sentences.size());
    tokens = JCasUtil.select(this.jCas, Token.class);
    Assert.assertEquals(2, tokens.size());
  }

  @Test
  public void testTempEval2010GoldAnnotatorViews() throws Exception {

    CollectionReader reader = TempEval2010CollectionReader.getCollectionReader(this.outputDirectory.getPath());
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        TempEval2010GoldAnnotator.getDescription(),
        TempEval2010GoldAnnotator.PARAM_TEXT_VIEWS,
        new String[] { "DCT", "TE", "TA", "EE", "EA", "E2DCT", "E2SST", "E2SE", "ME2ME" },
        TempEval2010GoldAnnotator.PARAM_DOCUMENT_CREATION_TIME_VIEWS,
        new String[] { "DCT", "E2DCT" },
        TempEval2010GoldAnnotator.PARAM_TIME_EXTENT_VIEWS,
        new String[] { "TE", "TA", "E2SST" },
        TempEval2010GoldAnnotator.PARAM_TIME_ATTRIBUTE_VIEWS,
        new String[] { "TA" },
        TempEval2010GoldAnnotator.PARAM_EVENT_EXTENT_VIEWS,
        new String[] { "EE", "EA", "E2DCT", "E2SST", "E2SE", "ME2ME" },
        TempEval2010GoldAnnotator.PARAM_EVENT_ATTRIBUTE_VIEWS,
        new String[] { "EA" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEWS,
        new String[] { "E2DCT" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEWS,
        new String[] { "E2SST" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEWS,
        new String[] { "E2SE" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEWS,
        new String[] { "ME2ME" });
    reader.getNext(this.jCas.getCas());
    engine.process(this.jCas);

    for (String viewName : new String[] { "DCT", "TE", "EA", "E2SE", "ME2ME" }) {
      JCas view = JCasUtil.getView(this.jCas, viewName, false);
      Collection<Sentence> sentences = JCasUtil.select(view, Sentence.class);
      Assert.assertEquals(viewName, 4, sentences.size());

      Collection<Token> tokens = JCasUtil.select(view, Token.class);
      Assert.assertEquals(viewName, 91, tokens.size());
    }

    for (String viewName : new String[] { "TE", "TA", "E2SST" }) {
      JCas view = JCasUtil.getView(this.jCas, viewName, false);
      Collection<Time> times = JCasUtil.select(view, Time.class);
      Assert.assertEquals(viewName, 4, times.size());
      Time t18 = itemAtIndex(times, 1);
      Assert.assertEquals(viewName, "t18", t18.getId());
      Assert.assertEquals(viewName, "the end of November", t18.getCoveredText());
      if (!viewName.equals("TA")) {
        for (Time time : times) {
          Assert.assertNull(time.getTimeType());
          Assert.assertNull(time.getValue());
        }
      } else {
        Assert.assertEquals(viewName, "1989-11", t18.getValue());
        Assert.assertEquals(viewName, "TIME", t18.getTimeType());
      }
    }

    for (String viewName : new String[] { "EE", "EA", "E2DCT", "E2SST", "E2SE", "ME2ME" }) {
      JCas view = JCasUtil.getView(this.jCas, viewName, false);
      Collection<Event> events = JCasUtil.select(view, Event.class);
      Assert.assertEquals(11, events.size());
      Event e10 = itemAtIndex(events, 9);
      Assert.assertEquals("e10", e10.getId());
      Assert.assertEquals("announced", e10.getCoveredText());
      if (viewName.equals("EA")) {
        Assert.assertEquals(viewName, "POS", e10.getPolarity());
        Assert.assertEquals(viewName, "NONE", e10.getModality());
        Assert.assertEquals(viewName, "ADJECTIVE", e10.getPos());
        Assert.assertEquals(viewName, "NONE", e10.getTense());
        Assert.assertEquals(viewName, "NONE", e10.getAspect());
        Assert.assertEquals(viewName, "REPORTING", e10.getEventClass());
      } else {
        Assert.assertNull(viewName, e10.getPolarity());
        Assert.assertNull(viewName, e10.getModality());
        Assert.assertNull(viewName, e10.getPos());
        Assert.assertNull(viewName, e10.getTense());
        Assert.assertNull(viewName, e10.getAspect());
        Assert.assertNull(viewName, e10.getEventClass());
      }
    }

    for (String viewName : new String[] { "DCT", "E2DCT" }) {
      JCas view = JCasUtil.getView(this.jCas, viewName, false);
      Collection<DocumentCreationTime> dcts = JCasUtil.select(view, DocumentCreationTime.class);
      Assert.assertEquals(1, dcts.size());
      Collection<Time> times = JCasUtil.select(view, Time.class);
      Assert.assertEquals(itemAtIndex(times, 0), itemAtIndex(dcts, 0));
      Time dct = itemAtIndex(dcts, 0);
      Assert.assertEquals("t0", dct.getId());
      Assert.assertEquals("1989-11-02", dct.getValue());
      Assert.assertEquals("CREATION_TIME", dct.getFunctionInDocument());
    }

    Collection<TemporalLink> tlinks;
    tlinks = JCasUtil.select(JCasUtil.getView(this.jCas, "E2DCT", false), TemporalLink.class);
    Assert.assertEquals(5, tlinks.size());
    TemporalLink e1t0 = itemAtIndex(tlinks, 0);
    Assert.assertEquals("BEFORE", e1t0.getRelationType());
    Assert.assertEquals("began", e1t0.getSource().getCoveredText());
    Assert.assertTrue(e1t0.getTarget() instanceof DocumentCreationTime);

    tlinks = JCasUtil.select(JCasUtil.getView(this.jCas, "E2SST", false), TemporalLink.class);
    Assert.assertEquals(3, tlinks.size());
    TemporalLink e8t21 = itemAtIndex(tlinks, 1);
    Assert.assertEquals("AFTER", e8t21.getRelationType());
    Assert.assertEquals("offer", e8t21.getSource().getCoveredText());
    Assert.assertEquals("previously", e8t21.getTarget().getCoveredText());

    tlinks = JCasUtil.select(JCasUtil.getView(this.jCas, "E2SE", false), TemporalLink.class);
    Assert.assertEquals(4, tlinks.size());
    TemporalLink e9e8 = itemAtIndex(tlinks, 2);
    Assert.assertEquals("BEFORE-OR-OVERLAP", e9e8.getRelationType());
    Assert.assertEquals("launched", e9e8.getSource().getCoveredText());
    Assert.assertEquals("offer", e9e8.getTarget().getCoveredText());

    tlinks = JCasUtil.select(JCasUtil.getView(this.jCas, "ME2ME", false), TemporalLink.class);
    Assert.assertEquals(3, tlinks.size());
    TemporalLink e5e7 = itemAtIndex(tlinks, 1);
    Assert.assertEquals("OVERLAP", e5e7.getRelationType());
    Assert.assertEquals("scheduled", e5e7.getSource().getCoveredText());
    Assert.assertEquals("owns", e5e7.getTarget().getCoveredText());
  }

  @Test
  public void testTempEval2010Writer() throws Exception {
    File writerDirectory = new File(this.outputDirectory, "writer");
    CollectionReader reader = TempEval2010CollectionReader.getCollectionReader(this.outputDirectory.getPath());
    AnalysisEngine annotator = AnalysisEngineFactory.createPrimitive(
        TempEval2010GoldAnnotator.getDescription(),
        TempEval2010GoldAnnotator.PARAM_TEXT_VIEWS,
        new String[] { CAS.NAME_DEFAULT_SOFA, "E2DCT", "E2SST", "E2SE", "ME2ME" },
        TempEval2010GoldAnnotator.PARAM_DOCUMENT_CREATION_TIME_VIEWS,
        new String[] { CAS.NAME_DEFAULT_SOFA, "E2DCT" },
        TempEval2010GoldAnnotator.PARAM_TIME_EXTENT_VIEWS,
        new String[] { CAS.NAME_DEFAULT_SOFA, "E2SST" },
        TempEval2010GoldAnnotator.PARAM_TIME_ATTRIBUTE_VIEWS,
        new String[] { CAS.NAME_DEFAULT_SOFA, "E2SST" },
        TempEval2010GoldAnnotator.PARAM_EVENT_EXTENT_VIEWS,
        new String[] { CAS.NAME_DEFAULT_SOFA, "E2DCT", "E2SST", "E2SE", "ME2ME" },
        TempEval2010GoldAnnotator.PARAM_EVENT_ATTRIBUTE_VIEWS,
        new String[] { CAS.NAME_DEFAULT_SOFA, "E2DCT", "E2SST", "E2SE", "ME2ME" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEWS,
        new String[] { "E2DCT" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEWS,
        new String[] { "E2SST" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEWS,
        new String[] { "E2SE" },
        TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEWS,
        new String[] { "ME2ME" });
    AnalysisEngine writer = AnalysisEngineFactory.createPrimitive(
        TempEval2010Writer.getDescription(writerDirectory),
        TempEval2010Writer.PARAM_TEXT_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_DOCUMENT_CREATION_TIME_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_TIME_EXTENT_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_TIME_ATTRIBUTE_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_EVENT_EXTENT_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_EVENT_ATTRIBUTE_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEW,
        "E2DCT",
        TempEval2010Writer.PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEW,
        "E2SST",
        TempEval2010Writer.PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEW,
        "E2SE",
        TempEval2010Writer.PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEW,
        "ME2ME");
    SimplePipeline.runPipeline(reader, annotator, writer);

    this.assertFileText("base-segmentation.tab", "data", "writer");
    this.assertFileText("timex-extents.tab", "key", "writer");
    this.assertFileText("timex-attributes.tab", "key", "writer");
    this.assertFileText("event-extents.tab", "key", "writer");
    this.assertFileText("event-attributes.tab", "key", "writer");
    this.assertFileText("tlinks-dct-event.tab", "key", "writer");
    this.assertFileText("tlinks-timex-event.tab", "key", "writer");
    this.assertFileText("tlinks-subordinated-events.tab", "key", "writer");
    this.assertFileText("tlinks-main-events.tab", "key", "writer");

    File dctInput = new File(this.outputDirectory, "dct-en.txt");
    File dctOutput = new File(this.outputDirectory, "writer/dct.txt");
    this.assertFileText("dct.txt", dctInput, dctOutput);
  }

  @Test
  public void testTempEval2010WriterPartial() throws Exception {
    File writerDirectory = new File(this.outputDirectory, "writer");
    CollectionReader reader = TempEval2010CollectionReader.getCollectionReader(this.outputDirectory.getPath());
    AnalysisEngine annotator = AnalysisEngineFactory.createPrimitive(TempEval2010GoldAnnotator.getDescription());
    AnalysisEngine writer = AnalysisEngineFactory.createPrimitive(
        TempEval2010Writer.getDescription(writerDirectory),
        TempEval2010Writer.PARAM_TEXT_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_TIME_EXTENT_VIEW,
        CAS.NAME_DEFAULT_SOFA,
        TempEval2010Writer.PARAM_EVENT_ATTRIBUTE_VIEW,
        CAS.NAME_DEFAULT_SOFA);
    SimplePipeline.runPipeline(reader, annotator, writer);

    this.assertFileText("base-segmentation.tab", "data", "writer");
    this.assertFileText("timex-extents.tab", "key", "writer");
    this.assertFileMissing("timex-attributes.tab", "writer");
    this.assertFileMissing("event-extents.tab", "writer");
    this.assertFileText("event-attributes.tab", "key", "writer");
    this.assertFileMissing("tlinks-dct-event.tab", "writer");
    this.assertFileMissing("tlinks-timex-event.tab", "writer");
    this.assertFileMissing("tlinks-subordinated-events.tab", "writer");
    this.assertFileMissing("tlinks-main-events.tab", "writer");
    this.assertFileMissing("dct.txt", "writer");
  }

  private void assertFileText(String fileName, String subdir1, String subdir2) throws Exception {
    File file1 = new File(new File(this.outputDirectory, subdir1), fileName);
    File file2 = new File(new File(this.outputDirectory, subdir2), fileName);
    this.assertFileText(fileName, file1, file2);
  }

  private void assertFileText(String fileName, File file1, File file2) throws Exception {
    String file1Text = Files.toString(file1, Charsets.US_ASCII);
    String file2Text = Files.toString(file2, Charsets.US_ASCII);
    Assert.assertEquals(fileName, file1Text, file2Text);
  }

  private void assertFileMissing(String fileName, String subdir) {
    File file = new File(new File(this.outputDirectory, subdir), fileName);
    Assert.assertFalse(file.exists());
  }

  private <T> T itemAtIndex(Collection<T> items, int index) {
    Iterator<T> iter = items.iterator();
    for (int i = 0; i < index; ++i) {
      iter.next();
    }
    return iter.next();
  }
}