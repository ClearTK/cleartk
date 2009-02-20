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
package org.cleartk.classifier.encoder.features.contextvalue;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.cleartk.classifier.Feature;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class ContextValueFeatureEncoderTests2 {

	
	@Test
	public void testencode() throws IOException {
		ContextValueFeatureEncoder encoder = new ContextValueFeatureEncoder(true, true);
		ContextValue contextValue = encoder.encode(new Feature("Hello")).get(0);
		testCV("0", 1.0f, contextValue);
		
		contextValue = encoder.encode(new Feature("Goodbye")).get(0);
		testCV("1", 1.0f, contextValue);

		contextValue = encoder.encode(new Feature("Goodbye")).get(0);
		testCV("1", 1.0f, contextValue);

		contextValue = encoder.encode(new Feature("Please")).get(0);
		testCV("2", 1.0f, contextValue);

		contextValue = encoder.encode(new Feature("don't")).get(0);
		testCV("3", 1.0f, contextValue);

		contextValue = encoder.encode(new Feature("go")).get(0);
		testCV("4", 1.0f, contextValue);

		contextValue = encoder.encode(new Feature("!", 15)).get(0);
		testCV("5", 15.0f, contextValue);

		
		FileWriter writer = new FileWriter("test/data/opennlp/test-feature-map-1.txt");
		encoder.writeKeys(writer);
		
		BufferedReader reader = new BufferedReader(new FileReader("test/data/opennlp/test-feature-map-1.txt"));
		assertEquals("6", reader.readLine());
		assertEquals("!\t5", reader.readLine());
		assertEquals("Goodbye\t1", reader.readLine());
		assertEquals("Hello\t0", reader.readLine());
		assertEquals("Please\t2", reader.readLine());
		assertEquals("don't\t3", reader.readLine());
		assertEquals("go\t4", reader.readLine());
		
	}
	
	private void testCV(String context, float value, ContextValue contextValue) {
		assertEquals(context, contextValue.getContext());
		assertEquals(value, contextValue.getValue(), 0.01d);
	}
}
