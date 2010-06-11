 /** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 *  
 *  @author Philip Ogren
 */


public class FilesTest {

	@Test
	public void testSingleFile() {
		Iterable<File> files = Files.getFiles("src/test/resources/data/html/1.html");
		Iterator<File> filesIterator = files.iterator();
		assertTrue(filesIterator.hasNext());
		File file = filesIterator.next();
		assertEquals("1.html", file.getName());
	}
	
	@Test
	public void testNames() {
		Set<String> fileNames = new HashSet<String>();
		fileNames.add("2.1.html");
		fileNames.add("4.1.1.html");
		fileNames.add("X.html");
		
		Set<String> retrievedFileNames = new HashSet<String>();

		Iterable<File> files = Files.getFiles(new File("src/test/resources/data/html"), fileNames);
		for(File file : files) {
			retrievedFileNames.add(file.getName());
		}
		
		assertEquals(2, retrievedFileNames.size());
		assertTrue(retrievedFileNames.contains("2.1.html"));
		assertTrue(retrievedFileNames.contains("4.1.1.html"));
	}
	
	@Test
	public void testPatternFilter() {
		String[] patterns = {"[.]txt", "^abc[.]def$"};
		Set<String> expected = new HashSet<String>();
		expected.add("abc.def");
		expected.add("abc.txt");
		expected.add("abc.txt.def");
		
		
		FileFilter filter = Files.createPatternFilter(patterns);
		Set<String> actual = new HashSet<String>();
		for (File file: Files.getFiles(OUTPUT_DIR, filter)) {
			actual.add(file.getName());
		}
		assertEquals(expected, actual);
	}
	
	@Before
	public void setUp() throws IOException {
		if (!OUTPUT_DIR.exists()) {
			OUTPUT_DIR.mkdirs();
		}
		new File(OUTPUT_DIR, "txt").createNewFile();
		new File(OUTPUT_DIR, "abc.def").createNewFile();
		new File(OUTPUT_DIR, "abc.txt").createNewFile();
		new File(OUTPUT_DIR, "abc.def.ghi").createNewFile();
		new File(OUTPUT_DIR, "abc.txt.def").createNewFile();
	}
	
	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(OUTPUT_DIR);
	}
	
	protected static final File OUTPUT_DIR = new File("src/test/resources/data/html/files");
}
