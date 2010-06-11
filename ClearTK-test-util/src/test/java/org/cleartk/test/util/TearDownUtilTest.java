/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 *  
 *  @author Steven Bethard, Philip Ogren
 */

public class TearDownUtilTest {

	File removeDir;
	File emptyDir;
	
	@Before
	public void setUp() {
		removeDir = new File("test/data/teardown/remove");
		emptyDir = new File("test/data/teardown/empty");
		emptyDir.mkdirs();
	}
	
	@Test
	public void testRemoveDirectory() throws FileNotFoundException {
		File subDir = new File(removeDir, "subdir");
		subDir.mkdirs();
		PrintStream out = new PrintStream(new File(removeDir, "test.txt"));
		out.println("some text goes here");
		out.close();
		
		out = new PrintStream(new File(subDir, "test2.txt"));
		out.println("2 two too to tu tutu.");
		out.close();
	
		TearDownUtil.removeDirectory(removeDir);
		assertFalse(subDir.exists());
		assertFalse(removeDir.exists());
		
		TearDownUtil.removeDirectory(new File("test"));
	}
	
	@Test
	public void testEmptyDirectory() throws FileNotFoundException{
		File subDir = new File(emptyDir, "subdir");
		subDir.mkdir();
		PrintStream out = new PrintStream(new File(emptyDir, "test3.txt"));
		out.println("three 3 thwee free flee!");
		out.close();
		
		out = new PrintStream(new File(subDir, "test4.txt"));
		out.println("4 for four fore foar.");
		out.close();
	
		TearDownUtil.emptyDirectory(emptyDir);
		assertFalse(subDir.exists());
		assertTrue(emptyDir.exists());
		assertEquals(0, emptyDir.list().length);
		
		TearDownUtil.removeDirectory(emptyDir);
		assertFalse(emptyDir.exists());

		TearDownUtil.removeDirectory(new File("test"));

	}
}
