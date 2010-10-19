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
package org.cleartk;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.util.FileUtils;
import org.cleartk.util.io.Files;


/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <br>
 * @author Philip Ogren
 */


public class SummarizeTestSuite {

	public static void main(String[] args) throws IOException {
		
		Iterable<File> files = Files.getFiles("test/src/org/cleartk", new String[] {".java"});
		int count = 0;
		int assertions = 0;
		String assertRegex = "(assertArrayEquals\\()|" +
							 "(assertEquals\\()|" +
							 "(assertFalse\\()|" +
							 "(assertNotNull\\()|" +
							 "(assertNotSame\\()|" +
							 "(assertNull\\()|" +
							 "(assertSame\\()|" +
							 "(assertThat\\()|" +
							 "(assertTrue\\()|" +
							 "(fail\\()";

		Pattern assertPattern = Pattern.compile(assertRegex);
		for(File file : files) {
			count++;
			
			String fileContents = FileUtils.file2String(file);
			Matcher matcher = assertPattern.matcher(fileContents);
			while(matcher.find()) {
				assertions++;
				System.out.println(matcher.group());
			}
		}
		
		count--; //we don't want to count this class;
		System.out.println("number of java classes in test/src: "+count);
		System.out.println("number of assertions in test/src: "+assertions);
	}
}
