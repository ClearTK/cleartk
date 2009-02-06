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
package org.cleartk.util.linereader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.type.Document;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.TestsUtil;
import org.cleartk.util.TestsUtil.JCasIterable;
import org.cleartk.util.linereader.LineReader;
import org.cleartk.util.linereader.SimpleLineHandler;
import org.junit.Assert;
import org.junit.Test;


/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */
public class LineReaderTests {

	@Test
	public void test1() throws Exception {
		String languageCode = "en-us";
		CollectionReader reader = TestsUtil.getCollectionReader(LineReader.class, TestsUtil
				.getTypeSystem(Document.class), LineReader.PARAM_FILE_OR_DIRECTORY, "test/data/docs/linereader",
				LineReader.PARAM_LANGUAGE, languageCode);

		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		JCasIterable jCasIterable = new TestsUtil.JCasIterable(reader);

		test(jCasIterable, "# this file was created by Philip Ogren on Monday 10/27/2008", "1", File.separator
				+ "test1.txt");
		test(jCasIterable, "# for more files like this please make your own", "2", File.separator + "test1.txt");
		test(jCasIterable, "A|This is the first sentence.", "3", File.separator + "test1.txt");
		test(jCasIterable, "B|This is the second sentence.  ", "4", File.separator + "test1.txt");
		test(jCasIterable, "C|You are likely completely absorbed by the narrative at this point.", "5", File.separator
				+ "test1.txt");
		test(jCasIterable, "D|... but too bad!        	", "6", File.separator + "test1.txt");
		test(jCasIterable, "EEEK|will it ever end?  yes - very soon...", "7", File.separator + "test1.txt");
		test(jCasIterable, "Z|Fin", "8", File.separator + "test1.txt");
		test(jCasIterable, "//this file was also created on Monday 10/27/2008", "9", File.separator + "test2.dat");
		test(jCasIterable, "//please see test1.txt for an introduction to the material contained in this file.", "10",
				File.separator + "test2.dat");
		test(jCasIterable, "// another comment", "11", File.separator + "test2.dat");
		test(jCasIterable, "1234|a bc def ghij klmno pqrstu vwxyz", "12", File.separator + "test2.dat");
		assertFalse(jCasIterable.hasNext());
	}

	@Test
	public void test2() throws Exception {
		CollectionReader reader = TestsUtil.getCollectionReader(LineReader.class, TestsUtil
				.getTypeSystem(Document.class), LineReader.PARAM_FILE_OR_DIRECTORY, "test/data/docs/linereader",
				LineReader.PARAM_LINE_HANDLER, "org.cleartk.util.linereader.SimpleLineHandler",
				SimpleLineHandler.PARAM_DELIMITER, "|", LineReader.PARAM_SUFFIXES, new String[] { ".txt", ".dat" },
				LineReader.PARAM_COMMENT_SPECIFIER, new String[] { "#", "//" });

		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		JCasIterable jCasIterable = new TestsUtil.JCasIterable(reader);

		test(jCasIterable, "This is the first sentence.", "A", File.separator + "test1.txt");
		test(jCasIterable, "This is the second sentence.  ", "B", File.separator + "test1.txt");
		test(jCasIterable, "You are likely completely absorbed by the narrative at this point.", "C", File.separator
				+ "test1.txt");
		test(jCasIterable, "... but too bad!        	", "D", File.separator + "test1.txt");
		test(jCasIterable, "will it ever end?  yes - very soon...", "EEEK", File.separator + "test1.txt");
		test(jCasIterable, "Fin", "Z", File.separator + "test1.txt");
		test(jCasIterable, "a bc def ghij klmno pqrstu vwxyz", "1234", File.separator + "test2.dat");
		assertFalse(jCasIterable.hasNext());
	}

	@Test
	public void test3() throws Exception {
		File file = new File("test/data/docs/linereader/test2.dat");

		CollectionReader reader = TestsUtil.getCollectionReader(LineReader.class, TestsUtil
				.getTypeSystem(Document.class), LineReader.PARAM_FILE_OR_DIRECTORY,
				file.getPath(), LineReader.PARAM_COMMENT_SPECIFIER, new String[] { "//" },
				LineReader.PARAM_SKIP_BLANK_LINES, false);

		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		
		JCasIterable jCasIterable = new TestsUtil.JCasIterable(reader);

		test(jCasIterable, "", "1", file.getPath());
		test(jCasIterable, "", "2", file.getPath());
		test(jCasIterable, "", "3", file.getPath());
		test(jCasIterable, "1234|a bc def ghij klmno pqrstu vwxyz", "4", file.getPath());
		test(jCasIterable, "", "5", file.getPath());
		test(jCasIterable, "    	", "6", file.getPath());
		assertFalse(jCasIterable.hasNext());
	}

	private void test(JCasIterable jCasIterable, String text, String id, String path) {
		JCas jCas = jCasIterable.next();
		Document doc = DocumentUtil.getDocument(jCas);
		assertEquals(text, jCas.getDocumentText());
		assertEquals(id, doc.getIdentifier());
		assertEquals(path, doc.getPath());

	}
	
	
	@Test
	public void testDescriptor() throws UIMAException, IOException {
		File inputDir = new File("test/data/docs/linereader/");
		
		try {
			TestsUtil.getCollectionReader("org.cleartk.util.linereader.LineReader");
			Assert.fail("expected exception with no file or directory specified");
		} catch (ResourceInitializationException e) {}
		
		CollectionReader reader = TestsUtil.getCollectionReader(
				"org.cleartk.util.linereader.LineReader",
				LineReader.PARAM_FILE_OR_DIRECTORY, inputDir.getPath());
		
		Object fileOrDirectory = reader.getConfigParameterValue(
				LineReader.PARAM_FILE_OR_DIRECTORY);
		Assert.assertEquals(inputDir.getPath(), fileOrDirectory);
		
		Object viewName = reader.getConfigParameterValue(
				LineReader.PARAM_VIEW_NAME);
		Assert.assertEquals(null, viewName);
		
		Object encoding = reader.getConfigParameterValue(
				LineReader.PARAM_ENCODING);
		Assert.assertEquals(null, encoding);

		Object language = reader.getConfigParameterValue(
				LineReader.PARAM_LANGUAGE);
		Assert.assertEquals(null, language);
		
		Object suffixes = reader.getConfigParameterValue(
				LineReader.PARAM_SUFFIXES);
		Assert.assertEquals(null, suffixes);

		Object comments = reader.getConfigParameterValue(
				LineReader.PARAM_COMMENT_SPECIFIER);
		Assert.assertEquals(null, comments);

	}

}
