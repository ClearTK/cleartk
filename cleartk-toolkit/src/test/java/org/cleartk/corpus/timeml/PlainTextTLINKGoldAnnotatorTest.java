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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ToolkitTestBase;
import org.cleartk.temporal.timeml.PlainTextTLINKGoldAnnotator;
import org.cleartk.temporal.timeml.TimeMLGoldAnnotator;
import org.cleartk.temporal.timeml.type.TemporalLink;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.ViewNames;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.JCasIterable;



/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Steven Bethard
 *
 */
public class PlainTextTLINKGoldAnnotatorTest extends ToolkitTestBase{
	
	private final String webUrl = "http://verbs.colorado.edu/~bethard/data/timebank-verb-clause.txt";
	private final String fileUrl = String.format("file:///%s",
			new File("test/data/corpus/timeml/wsj_0106.tlinks").getAbsolutePath());
			

	@Test
	@Ignore
	public void test_wsj_0106() throws UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, 
				typeSystemDescription,
				FilesCollectionReader.PARAM_VIEW_NAME,
				ViewNames.TIMEML,
				FilesCollectionReader.PARAM_ROOT_FILE,
				"test/data/corpus/timeml/wsj_0106.tml");
		AnalysisEngine timemlEngine = AnalysisEngineFactory.createPrimitive(
				TimeMLGoldAnnotator.class,
				typeSystemDescription,
				TimeMLGoldAnnotator.PARAM_LOAD_TLINKS, false);
		AnalysisEngine plainTextEngine = AnalysisEngineFactory.createPrimitive(
				PlainTextTLINKGoldAnnotator.class,
				typeSystemDescription,
				PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL,
				this.webUrl);
		JCas jcas = new JCasIterable(reader, timemlEngine, plainTextEngine).next();

		List<TemporalLink> tlinks = AnnotationRetrieval.getAnnotations(jcas, TemporalLink.class);
		Assert.assertEquals(6, tlinks.size());
		Map<String, TemporalLink> tlinkMap = new HashMap<String, TemporalLink>();
		for (TemporalLink tlink: tlinks) {
			tlinkMap.put(tlink.getSource().getId(), tlink);
		}
		
		// wsj_0106 ei128 ei129 OVERLAP
		TemporalLink tlink = tlinkMap.get("e1");
		Assert.assertEquals(null, tlink.getId());
		Assert.assertEquals("OVERLAP", tlink.getRelationType());
		Assert.assertEquals("ei128", tlink.getEventInstanceID());
		Assert.assertEquals("e1", tlink.getEventID());
		Assert.assertEquals(null, tlink.getTimeID());
		Assert.assertEquals(null, tlink.getRelatedToTime());
		Assert.assertEquals("e2", tlink.getRelatedToEvent());
		Assert.assertEquals("ei129", tlink.getRelatedToEventInstance());
		Assert.assertEquals("e1", tlink.getSource().getId());
		Assert.assertEquals("e2", tlink.getTarget().getId());

		// wsj_0106 ei129 ei130 BEFORE
		tlink = tlinkMap.get("e2");
		Assert.assertEquals(null, tlink.getId());
		Assert.assertEquals("BEFORE", tlink.getRelationType());
		Assert.assertEquals("ei129", tlink.getEventInstanceID());
		Assert.assertEquals("e2", tlink.getEventID());
		Assert.assertEquals(null, tlink.getTimeID());
		Assert.assertEquals(null, tlink.getRelatedToTime());
		Assert.assertEquals("e4", tlink.getRelatedToEvent());
		Assert.assertEquals("ei130", tlink.getRelatedToEventInstance());
		Assert.assertEquals("e2", tlink.getSource().getId());
		Assert.assertEquals("e4", tlink.getTarget().getId());
	}
	
	@Test
	public void test_wsj_0106_alternate() throws UIMAException, IOException {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, 
				typeSystemDescription,
				FilesCollectionReader.PARAM_VIEW_NAME,
				ViewNames.TIMEML,
				FilesCollectionReader.PARAM_ROOT_FILE,
				"test/data/corpus/timeml/wsj_0106.tml");
		AnalysisEngine timemlEngine = AnalysisEngineFactory.createPrimitive(
				TimeMLGoldAnnotator.class,
				typeSystemDescription,
				TimeMLGoldAnnotator.PARAM_LOAD_TLINKS, false);
		AnalysisEngine plainTextEngine = AnalysisEngineFactory.createPrimitive(
				PlainTextTLINKGoldAnnotator.class,
				typeSystemDescription,
				PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL,
				this.fileUrl);
		JCas jcas = new JCasIterable(reader, timemlEngine, plainTextEngine).next();

		List<TemporalLink> tlinks = AnnotationRetrieval.getAnnotations(jcas, TemporalLink.class);
		Assert.assertEquals(2, tlinks.size());
		Map<String, TemporalLink> tlinkMap = new HashMap<String, TemporalLink>();
		for (TemporalLink tlink: tlinks) {
			tlinkMap.put(tlink.getSource().getId(), tlink);
		}
		
		// wsj_0106 ei128 t26 BEFORE
		TemporalLink tlink = tlinkMap.get("e1");
		Assert.assertEquals(null, tlink.getId());
		Assert.assertEquals("BEFORE", tlink.getRelationType());
		Assert.assertEquals("ei128", tlink.getEventInstanceID());
		Assert.assertEquals("e1", tlink.getEventID());
		Assert.assertEquals(null, tlink.getTimeID());
		Assert.assertEquals("t26", tlink.getRelatedToTime());
		Assert.assertEquals(null, tlink.getRelatedToEvent());
		Assert.assertEquals(null, tlink.getRelatedToEventInstance());
		Assert.assertEquals("e1", tlink.getSource().getId());
		Assert.assertEquals("t26", tlink.getTarget().getId());

		// wsj_0106 t26 ei132 AFTER
		tlink = tlinkMap.get("t26");
		Assert.assertEquals(null, tlink.getId());
		Assert.assertEquals("AFTER", tlink.getRelationType());
		Assert.assertEquals(null, tlink.getEventInstanceID());
		Assert.assertEquals(null, tlink.getEventID());
		Assert.assertEquals("t26", tlink.getTimeID());
		Assert.assertEquals(null, tlink.getRelatedToTime());
		Assert.assertEquals("e7", tlink.getRelatedToEvent());
		Assert.assertEquals("ei132", tlink.getRelatedToEventInstance());
		Assert.assertEquals("t26", tlink.getSource().getId());
		Assert.assertEquals("e7", tlink.getTarget().getId());
	}
	
	@Test
	@Ignore
	public void testAnnotatorDescriptor() throws UIMAException, IOException {
		try {
			AnalysisEngineFactory.createAnalysisEngine(
					"org.cleartk.corpus.timeml.PlainTextTLINKGoldAnnotator");
			Assert.fail("expected failure with no TlinkFileUrl specified");
		} catch (ResourceInitializationException e) {}
		
		
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.corpus.timeml.PlainTextTLINKGoldAnnotator",
				PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL, this.webUrl);
		Assert.assertEquals(this.webUrl, engine.getConfigParameterValue(
				PlainTextTLINKGoldAnnotator.PARAM_TLINK_FILE_URL));
		engine.collectionProcessComplete();
	}
}
