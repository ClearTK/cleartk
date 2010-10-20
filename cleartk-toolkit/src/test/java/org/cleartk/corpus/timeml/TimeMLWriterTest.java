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
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.ToolkitTestBase;
import org.cleartk.ViewNames;
import org.cleartk.temporal.timeml.corpus.TimeMLGoldAnnotator;
import org.cleartk.temporal.timeml.corpus.TimeMLWriter;
import org.cleartk.util.CleartkComponents;
import org.cleartk.util.FilesCollectionReader;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Before;
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
public class TimeMLWriterTest extends ToolkitTestBase {
	
	private File inputFile;
	private File outputFile;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		inputFile = new File("test/data/corpus/timeml/test.foo");
		outputFile  = new File(outputDirectory, "test.foo.tml");
	}
	@Test
	public void test() throws UIMAException, IOException, JDOMException {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, 
				typeSystemDescription,
				FilesCollectionReader.PARAM_VIEW_NAME,
				ViewNames.TIMEML,
				FilesCollectionReader.PARAM_ROOT_FILE,
				this.inputFile.getPath());
		AnalysisEngine annotator = AnalysisEngineFactory.createPrimitive(
				TimeMLGoldAnnotator.class,
				typeSystemDescription);
		AnalysisEngine writer = AnalysisEngineFactory.createPrimitive(
				TimeMLWriter.class,
				typeSystemDescription,
				TimeMLWriter.PARAM_OUTPUT_DIRECTORY_NAME,
				this.outputDirectory.getPath());
		
		for (JCas jcas: new JCasIterable(reader, annotator, writer)) {
			Assert.assertNotNull(jcas);
		}
		reader.close();
		annotator.collectionProcessComplete();
		writer.collectionProcessComplete();

		String expected = FileUtils.file2String(this.inputFile);
		String actual = FileUtils.file2String(this.outputFile);
		this.assertEquals(this.getRoot(expected), this.getRoot(actual));
	}

	
	@Test
	public void testDescriptor() throws UIMAException, IOException {
		try {
			CleartkComponents.createPrimitive(TimeMLWriter.class);
			Assert.fail("expected failure with no OutputDirectory specified");
		} catch (ResourceInitializationException e) {}
		
		
		AnalysisEngine engine = CleartkComponents.createPrimitive(TimeMLWriter.class, TimeMLWriter.PARAM_OUTPUT_DIRECTORY_NAME, this.outputDirectory.getPath());
		Assert.assertEquals(this.outputDirectory.getPath(), engine.getConfigParameterValue(TimeMLWriter.PARAM_OUTPUT_DIRECTORY_NAME));
		engine.collectionProcessComplete();
	}

	private Element getRoot(String xml) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		builder.setDTDHandler(null);
		return builder.build(new StringReader(xml)).getRootElement();
	}
	
	private void assertEquals(Element element1, Element element2) {
		Assert.assertEquals(element1.getName(), element2.getName());
		Assert.assertEquals(
				this.getAttributes(element1),
				this.getAttributes(element2));
		List<?> children1 = element1.getChildren();
		List<?> children2 = element2.getChildren();
		Assert.assertEquals(children1.size(), children2.size());
		for (int i = 0; i < children1.size(); i ++) {
			Object child1 = children1.get(0);
			Object child2 = children2.get(0);
			if (child1 instanceof Element) {
				this.assertEquals((Element)child1, (Element)child2);
			} else {
				Assert.assertEquals(child1, child2);
			}
		}
	}
	
	private Map<String, String> getAttributes(Element element) {
		Map<String, String> attributes = new HashMap<String, String>();
		for (Object attrObj: element.getAttributes()) {
			Attribute attribute = (Attribute)attrObj;
			attributes.put(attribute.getName(), attribute.getValue());
		}
		return attributes;
	}
}
