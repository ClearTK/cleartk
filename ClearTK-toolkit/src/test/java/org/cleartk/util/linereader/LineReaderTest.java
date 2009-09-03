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

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ViewURIUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.JCasIterable;


/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */
public class LineReaderTest {

	@Test
	public void test1() throws Exception {
		String languageCode = "en-us";
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(LineReader.class, null, LineReader.PARAM_FILE_OR_DIRECTORY_NAME, "test/data/docs/linereader",
				LineReader.PARAM_LANGUAGE, languageCode, LineReader.PARAM_SUFFIXES, new String[] {".txt"});

		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		JCasIterable jCasIterable = new JCasIterable(reader);

		test(jCasIterable, "# this file was created by Philip Ogren on Monday 10/27/2008", "1", File.separator
				+ "a-test1.txt");
		test(jCasIterable, "# for more files like this please make your own", "2", File.separator + "a-test1.txt");
		test(jCasIterable, "A|This is the first sentence.", "3", File.separator + "a-test1.txt");
		test(jCasIterable, "B|This is the second sentence.  ", "4", File.separator + "a-test1.txt");
		test(jCasIterable, "C|You are likely completely absorbed by the narrative at this point.", "5", File.separator
				+ "a-test1.txt");
		test(jCasIterable, "D|... but too bad!        	", "6", File.separator + "a-test1.txt");
		test(jCasIterable, "EEEK|will it ever end?  yes - very soon...", "7", File.separator + "a-test1.txt");
		test(jCasIterable, "Z|Fin", "8", File.separator + "a-test1.txt");

		reader = CollectionReaderFactory.createCollectionReader(LineReader.class, null, LineReader.PARAM_FILE_OR_DIRECTORY_NAME, "test/data/docs/linereader",
				LineReader.PARAM_LANGUAGE, languageCode, LineReader.PARAM_SUFFIXES, new String[] {".dat"});

		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		jCasIterable = new JCasIterable(reader);

		test(jCasIterable, "//this file was also created on Monday 10/27/2008", "1", File.separator + "b-test2.dat");
		test(jCasIterable, "//please see a-test1.txt for an introduction to the material contained in this file.", "2",
				File.separator + "b-test2.dat");
		test(jCasIterable, "// another comment", "3", File.separator + "b-test2.dat");
		test(jCasIterable, "1234|a bc def ghij klmno pqrstu vwxyz", "4", File.separator + "b-test2.dat");
		assertFalse(jCasIterable.hasNext());
	}

	@Test
	public void test2() throws Exception {
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(LineReader.class, null, LineReader.PARAM_FILE_OR_DIRECTORY_NAME, "test/data/docs/linereader",
				LineReader.PARAM_LINE_HANDLER_CLASS_NAME, SimpleLineHandler.class.getName(),
				SimpleLineHandler.PARAM_DELIMITER, "|", LineReader.PARAM_SUFFIXES, new String[] { ".txt", ".dat" },
				LineReader.PARAM_COMMENT_SPECIFIERS, new String[] { "#", "//" });

		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		JCasIterable jCasIterable = new JCasIterable(reader);

		test(jCasIterable, "This is the first sentence.", "A", File.separator + "a-test1.txt");
		test(jCasIterable, "This is the second sentence.  ", "B", File.separator + "a-test1.txt");
		test(jCasIterable, "You are likely completely absorbed by the narrative at this point.", "C", File.separator
				+ "a-test1.txt");
		test(jCasIterable, "... but too bad!        	", "D", File.separator + "a-test1.txt");
		test(jCasIterable, "will it ever end?  yes - very soon...", "EEEK", File.separator + "a-test1.txt");
		test(jCasIterable, "Fin", "Z", File.separator + "a-test1.txt");
		test(jCasIterable, "a bc def ghij klmno pqrstu vwxyz", "1234", File.separator + "b-test2.dat");
		assertFalse(jCasIterable.hasNext());
	}

	@Test
	public void test3() throws Exception {
		File file = new File("test/data/docs/linereader/b-test2.dat");

		CollectionReader reader = CollectionReaderFactory.createCollectionReader(LineReader.class, null, LineReader.PARAM_FILE_OR_DIRECTORY_NAME,
				file.getPath(), LineReader.PARAM_COMMENT_SPECIFIERS, new String[] { "//" },
				LineReader.PARAM_SKIP_BLANK_LINES, false);

		Assert.assertEquals(0, reader.getProgress()[0].getCompleted());

		
		JCasIterable jCasIterable = new JCasIterable(reader);

		test(jCasIterable, "", "1", file.getPath());
		test(jCasIterable, "", "2", file.getPath());
		test(jCasIterable, "", "3", file.getPath());
		test(jCasIterable, "1234|a bc def ghij klmno pqrstu vwxyz", "4", file.getPath());
		test(jCasIterable, "", "5", file.getPath());
		test(jCasIterable, "    	", "6", file.getPath());
		assertFalse(jCasIterable.hasNext());
	}

	private void test(JCasIterable jCasIterable, String text, String id, String path) throws Exception {
		JCas jCas = jCasIterable.next();
		assertEquals(text, jCas.getDocumentText());
		assertEquals(String.format("%s#%s", path, id), ViewURIUtil.getURI(jCas));
	}
	
}
