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
package org.cleartk.classifier.encoder.features.string;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.string.CompressedStringEncoder;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class CompressedFeatureEncoderTests {

	@Test
	public void testCompressedStringFeatureEncoder() throws IOException {
		// add some features
		CompressedStringEncoder encoder = new CompressedStringEncoder();
		for (int i = 0; i < 10; i++) {
			assertEquals(String.valueOf(i), encoder.encode(new Feature(i)).get(0));
		}
		assertEquals("A", encoder.encode(new Feature("foo", "bar")).get(0));
		
		// make sure identical features get the same encoding
		for (int i = 0; i < 10; i++) {
			assertEquals(String.valueOf(i), encoder.encode(new Feature(i)).get(0));
		}
		assertEquals("A", encoder.encode(new Feature("foo", "bar")).get(0));
		
		// make sure new features get new encodings
		assertEquals("B", encoder.encode(new Feature("bar")).get(0));
		assertEquals("C", encoder.encode(new Feature("foo", null)).get(0));
		
		// save the encoding
		StringWriter writer = new StringWriter();
		encoder.save(writer);
		String savedText = writer.toString();
		
		// create a new encoder and load the encoding
		encoder = new CompressedStringEncoder(new StringReader(savedText));
		
		// check that all the old values are still present
		assertEquals("C", encoder.encode(new Feature("foo", null)).get(0));
		assertEquals("B", encoder.encode(new Feature("bar")).get(0));
		assertEquals("A", encoder.encode(new Feature("foo", "bar")).get(0));
		for (int i = 9; i >= 0; i--) {
			assertEquals(String.valueOf(i), encoder.encode(new Feature(i)).get(0));
		}
		
		// check that new values can still be generated
		assertEquals("D", encoder.encode(new Feature(true)).get(0));

		// create a new encoder with a Reader
		encoder = new CompressedStringEncoder(new StringReader(savedText));
		
		// check that all the old values are still present
		assertEquals("D", encoder.encode(new Feature(true)).get(0));
		assertEquals("C", encoder.encode(new Feature("foo", null)).get(0));
		assertEquals("B", encoder.encode(new Feature("bar")).get(0));
		assertEquals("A", encoder.encode(new Feature("foo", "bar")).get(0));
		for (int i = 9; i >= 0; i--) {
			assertEquals(String.valueOf(i), encoder.encode(new Feature(i)).get(0));
		}
	}

}
