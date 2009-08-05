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
package org.cleartk.util;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.TearDownUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

@author Philip Ogren, Steven Bethard
*/

public class XWriterTests {
	
	public final File outputDir = new File("test/data/xmi");

	@Before
	public void setUp() throws Exception {
		this.outputDir.mkdirs();
	}

	@After
	public void tearDown() throws Exception {
		TearDownUtil.removeDirectory(outputDir);
	}

	@Test
	public void testXmi() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				XWriter.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				XWriter.PARAM_OUTPUT_DIRECTORY, this.outputDir.getPath());
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"I like\nspam!",
				Token.class, Sentence.class, 
				"I like spam !",
				"PRP VB NN .", null, "org.cleartk.type.Token:pos", null);
		ViewURIUtil.setURI(jCas, "..\\ClearTK Data\\data\\treebank\\identifier");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		File outputFile = new File(this.outputDir, "identifier.xmi");

		SAXBuilder builder = new SAXBuilder();
		builder.setDTDHandler(null);
		Element root = null;
		try {
			Document doc = builder.build(new StringReader(FileUtils.file2String(outputFile)));
			root = doc.getRootElement();
		} catch (JDOMException e) {
			throw new AnalysisEngineProcessException(e);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

		List<?> elements = root.getChildren("Sentence", root.getNamespace("type"));
		Assert.assertEquals(1, elements.size());
		elements = root.getChildren("Token", root.getNamespace("type"));
		Assert.assertEquals(4, elements.size());
		
	}

	@Test
	public void testXcas() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				XWriter.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				XWriter.PARAM_OUTPUT_DIRECTORY, this.outputDir.getPath(),
				XWriter.PARAM_XML_SCHEME, XWriter.XCAS);
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"I like\nspam!",
				Token.class, Sentence.class, 
				"I like spam !",
				"PRP VB NN .", null, "org.cleartk.type.Token:pos", null);
		ViewURIUtil.setURI(jCas, "identifier");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		File outputFile = new File(this.outputDir, "identifier.xcas");

		SAXBuilder builder = new SAXBuilder();
		builder.setDTDHandler(null);
		Element root = null;
		try {
			Document doc = builder.build(new StringReader(FileUtils.file2String(outputFile)));
			root = doc.getRootElement();
		} catch (JDOMException e) {
			throw new AnalysisEngineProcessException(e);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

		List<?> elements = root.getChildren("org.cleartk.type.Sentence");
		Assert.assertEquals(1, elements.size());
		elements = root.getChildren("org.cleartk.type.Token");
		Assert.assertEquals(4, elements.size());
		
	}
	
	
	
	@Test
	public void testDescriptor() throws UIMAException, IOException {
		try {
			AnalysisEngineFactory.createAnalysisEngine("org.cleartk.util.XWriter");
			Assert.fail("expected error when no output directory was specified");
		} catch (ResourceInitializationException e) {}
		
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.util.XWriter",
				XWriter.PARAM_OUTPUT_DIRECTORY, this.outputDir.getPath());
		Object dir = engine.getConfigParameterValue(XWriter.PARAM_OUTPUT_DIRECTORY);
		Assert.assertEquals(this.outputDir.getPath(), dir);
		
		engine.collectionProcessComplete();
	}
}
