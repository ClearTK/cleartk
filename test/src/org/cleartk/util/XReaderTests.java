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

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.TokenFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.JCasIterable;
import org.uutuc.util.TearDownUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * Unit tests for org.cleartk.readers.DirectoryCollectionReader.
 * 
 * @author Philip Ogren
 */
public class XReaderTests {
	
	@Before
	public void setUp() throws Exception {
		this.inputDir.mkdirs();
	}

	@After
	public void tearDown() throws Exception {
		TearDownUtil.removeDirectory(inputDir);
	}

	/**
	 * The directory containing all the files to be loaded.
	 */
	public final File inputDir = new File("test/data/xmi");

	@Test
	public void testReaderXmi() throws IOException, UIMAException {

		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				XWriter.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				XWriter.PARAM_OUTPUT_DIRECTORY, this.inputDir.getPath());
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"I like\nspam!",
				Token.class, Sentence.class, 
				"I like spam !",
				"PRP VB NN .", null, "org.cleartk.type.Token:pos", null);
		ViewURIUtil.setURI(jCas, "test");
		engine.process(jCas);
		engine.collectionProcessComplete();

		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				XReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY, new File(inputDir, "test.xmi").getPath());
		
		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		jCas = new JCasIterable(reader).next();
		
		String jCasText = jCas.getDocumentText();
		String docText = "I like\nspam!";
		Assert.assertEquals(jCasText, docText);
			
		Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		Assert.assertEquals("I", token.getCoveredText());
		reader.close();
		
	}

	@Test
	public void testReaderXcas() throws IOException, UIMAException {

		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				XWriter.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				XWriter.PARAM_OUTPUT_DIRECTORY, this.inputDir.getPath(),
				XWriter.PARAM_XML_SCHEME, XWriter.XCAS);
		JCas jCas = engine.newJCas();
		TokenFactory.createTokens(jCas,
				"I like\nspam!",
				Token.class, Sentence.class, 
				"I like spam !",
				"PRP VB NN .", null, "org.cleartk.type.Token:pos", null);
		ViewURIUtil.setURI(jCas, "test");
		engine.process(jCas);
		engine.collectionProcessComplete();

		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				XReader.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
				PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY, "test/data/xmi/test.xcas",
				XReader.PARAM_XML_SCHEME, XReader.XCAS);
		
		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		jCas = new JCasIterable(reader).next();
		
		String jCasText = jCas.getDocumentText();
		String docText = "I like\nspam!";
		Assert.assertEquals(jCasText, docText);
			
		Token token = AnnotationRetrieval.get(jCas, Token.class, 0);
		Assert.assertEquals("I", token.getCoveredText());
		reader.close();
		
	}

	
		@Test
	public void testDescriptor() throws UIMAException, IOException {
		try {
			CollectionReaderFactory.createCollectionReader("org.cleartk.util.XReader");
			Assert.fail("expected exception with no file or directory specified");
		} catch (ResourceInitializationException e) {}
		
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				"org.cleartk.util.XReader",
				PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY, inputDir.getPath());
		
		Object fileOrDirectory = reader.getConfigParameterValue(
				PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY);
		Assert.assertEquals(inputDir.getPath(), fileOrDirectory);
		
	}
	
}
