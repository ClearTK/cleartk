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

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkComponents;
import org.cleartk.ViewNames;
import org.cleartk.corpus.timeml.type.Event;
import org.cleartk.corpus.timeml.type.TemporalLink;
import org.cleartk.corpus.timeml.type.Time;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.FilesCollectionReader;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.JCasIterable;



/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Steven Bethard
 *
 */
public class TimeMLGoldAnnotatorTests {
	
	@Test
	public void testTimeBank() throws UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				FilesCollectionReader.PARAM_VIEW_NAME,
				ViewNames.TIMEML,
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY,
				"test/data/corpus/timeml/wsj_0106.tml");
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				TimeMLGoldAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
		JCas jCas = new JCasIterable(reader, engine).next();

		// <EVENT eid="e1" class="REPORTING">said</EVENT>
		// <MAKEINSTANCE eventID="e1" eiid="ei128" tense="PAST" aspect="NONE" polarity="POS" pos="VERB"/>
		List<Event> events = AnnotationRetrieval.getAnnotations(jCas, Event.class);
		Assert.assertEquals("said", events.get(0).getCoveredText());
		Assert.assertEquals("e1", events.get(0).getId());
		Assert.assertEquals("ei128", events.get(0).getEventInstanceID());
		Assert.assertEquals("REPORTING", events.get(0).getEventClass());
		Assert.assertEquals("PAST", events.get(0).getTense());
		Assert.assertEquals("NONE", events.get(0).getAspect());
		Assert.assertEquals("POS", events.get(0).getPolarity());
		Assert.assertEquals("VERB", events.get(0).getPos());
		Assert.assertEquals(null, events.get(0).getStem());
		Assert.assertEquals(null, events.get(0).getModality());
		Assert.assertEquals(null, events.get(0).getCardinality());

		// <TIMEX3 tid="t26" type="DATE" value="1989-11-02" temporalFunction="false" functionInDocument="CREATION_TIME">11/02/89</TIMEX3>
		List<Time> times = AnnotationRetrieval.getAnnotations(jCas, Time.class);
		Time docTime = times.get(0);
		Assert.assertEquals("11/02/89", docTime.getCoveredText());
		Assert.assertEquals("t26", docTime.getId());
		Assert.assertEquals("DATE", docTime.getTimeType());
		Assert.assertEquals("1989-11-02", docTime.getValue());
		Assert.assertEquals("false", docTime.getTemporalFunction());
		Assert.assertEquals("CREATION_TIME", docTime.getFunctionInDocument());
		
		// <TLINK lid="l1" relType="BEFORE" eventInstanceID="ei128" relatedToTime="t26"/>
		List<TemporalLink> tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class);
		Assert.assertEquals("l1", tlinks.get(0).getId());
		Assert.assertEquals("BEFORE", tlinks.get(0).getRelationType());
		Assert.assertEquals("ei128", tlinks.get(0).getEventInstanceID());
		Assert.assertEquals("e1", tlinks.get(0).getEventID());
		Assert.assertEquals(null, tlinks.get(0).getTimeID());
		Assert.assertEquals("t26", tlinks.get(0).getRelatedToTime());
		Assert.assertEquals(null, tlinks.get(0).getRelatedToEvent());
		Assert.assertEquals(null, tlinks.get(0).getRelatedToEventInstance());
		Assert.assertEquals("e1", tlinks.get(0).getSource().getId());
		Assert.assertEquals("t26", tlinks.get(0).getTarget().getId());
		Assert.assertEquals(events.get(0), tlinks.get(0).getSource());

		// <TLINK lid="l2" relType="SIMULTANEOUS" eventInstanceID="ei131" relatedToEventInstance="ei130"/>
		Assert.assertEquals("l2", tlinks.get(1).getId());
		Assert.assertEquals("SIMULTANEOUS", tlinks.get(1).getRelationType());
		Assert.assertEquals("ei131", tlinks.get(1).getEventInstanceID());
		Assert.assertEquals("e5", tlinks.get(1).getEventID());
		Assert.assertEquals(null, tlinks.get(1).getTimeID());
		Assert.assertEquals(null, tlinks.get(1).getRelatedToTime());
		Assert.assertEquals("e4", tlinks.get(1).getRelatedToEvent());
		Assert.assertEquals("ei130", tlinks.get(1).getRelatedToEventInstance());
		Assert.assertEquals("e5", tlinks.get(1).getSource().getId());
		Assert.assertEquals("e4", tlinks.get(1).getTarget().getId());
	}
	
	@Test
	public void testTempEval() throws UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				FilesCollectionReader.PARAM_VIEW_NAME,
				ViewNames.TIMEML,
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY,
				"test/data/corpus/timeml/AP900815-0044.tml");
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				TimeMLGoldAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
		JCas jCas = new JCasIterable(reader, engine).next();

		// <EVENT eid="e5" class="STATE" stem="face" aspect="NONE" tense="PRESPART" polarity="POS" pos="VERB">facing</EVENT>
		List<Event> events = AnnotationRetrieval.getAnnotations(jCas, Event.class);
		Assert.assertEquals("facing", events.get(0).getCoveredText());
		Assert.assertEquals("e5", events.get(0).getId());
		Assert.assertEquals(null, events.get(0).getEventInstanceID());
		Assert.assertEquals("STATE", events.get(0).getEventClass());
		Assert.assertEquals("PRESPART", events.get(0).getTense());
		Assert.assertEquals("NONE", events.get(0).getAspect());
		Assert.assertEquals("POS", events.get(0).getPolarity());
		Assert.assertEquals("VERB", events.get(0).getPos());
		Assert.assertEquals("face", events.get(0).getStem());
		Assert.assertEquals(null, events.get(0).getModality());
		Assert.assertEquals(null, events.get(0).getCardinality());

		// <TIMEX3 tid="t3" type="TIME" value="1990-08-15T13:37" temporalFunction="false" functionInDocument="CREATION_TIME">08-15-90 1337EDT</TIMEX3>
		List<Time> times = AnnotationRetrieval.getAnnotations(jCas, Time.class);
		Time docTime = times.get(0);
		Assert.assertEquals("08-15-90 1337EDT", docTime.getCoveredText());
		Assert.assertEquals("t3", docTime.getId());
		Assert.assertEquals("TIME", docTime.getTimeType());
		Assert.assertEquals("1990-08-15T13:37", docTime.getValue());
		Assert.assertEquals("false", docTime.getTemporalFunction());
		Assert.assertEquals("CREATION_TIME", docTime.getFunctionInDocument());
		
		// <TLINK lid="l6" relType="OVERLAP" eventID="e54" relatedToTime="t56" task="A"/>
		List<TemporalLink> tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class);
		Assert.assertEquals("l6", tlinks.get(5).getId());
		Assert.assertEquals("OVERLAP", tlinks.get(5).getRelationType());
		Assert.assertEquals(null, tlinks.get(5).getEventInstanceID());
		Assert.assertEquals("e54", tlinks.get(5).getEventID());
		Assert.assertEquals(null, tlinks.get(5).getTimeID());
		Assert.assertEquals("t56", tlinks.get(5).getRelatedToTime());
		Assert.assertEquals(null, tlinks.get(5).getRelatedToEvent());
		Assert.assertEquals(null, tlinks.get(5).getRelatedToEventInstance());
		Assert.assertEquals("e54", tlinks.get(5).getSource().getId());
		Assert.assertEquals("t56", tlinks.get(5).getTarget().getId());
	}
	
	@Test
	public void testNoTLINKs() throws UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, 
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				FilesCollectionReader.PARAM_VIEW_NAME,
				ViewNames.TIMEML,
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY,
				"test/data/corpus/timeml",
				FilesCollectionReader.PARAM_SUFFIXES,
				new String[]{".tml"});
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				TimeMLGoldAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				TimeMLGoldAnnotator.PARAM_LOAD_TLINKS, false);
		for (JCas jCas: new JCasIterable(reader, engine)) {
			List<Event> events = AnnotationRetrieval.getAnnotations(jCas, Event.class); 
			Assert.assertTrue(events.size() > 0);
			List<Time> times = AnnotationRetrieval.getAnnotations(jCas, Time.class); 
			Assert.assertTrue(times.size() > 0);
			List<TemporalLink> tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class); 
			Assert.assertEquals(0, tlinks.size());
		}
	}
	
	@Test
	public void testAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = CleartkComponents.createPrimitive(TimeMLGoldAnnotator.class);
		Boolean useTLINKs = (Boolean)engine.getConfigParameterValue(TimeMLGoldAnnotator.PARAM_LOAD_TLINKS);
		Assert.assertEquals(true, useTLINKs);
		engine.collectionProcessComplete();

		engine = CleartkComponents.createPrimitive(TimeMLGoldAnnotator.class, TimeMLGoldAnnotator.PARAM_LOAD_TLINKS, false);
		useTLINKs = (Boolean)engine.getConfigParameterValue(TimeMLGoldAnnotator.PARAM_LOAD_TLINKS);
		Assert.assertEquals(false, useTLINKs);
		engine.collectionProcessComplete();
	}
	

}
