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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.TypePathFeature;
import org.cleartk.classifier.feature.WindowFeature;
import org.junit.Before;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class ContextValueFeatureEncoderTests {

	ContextValueFeatureEncoder encoder;
	
	@Before
	public void setUp() {
		encoder = new ContextValueFeatureEncoder();
	}
	
	@Test
	public void testEncodeFeature() {
		//test null values
		assertNull(encode(null));
		assertNull(encode(new Feature(null)));
		assertNull(encode(new Feature(null, null)));
		assertNull(encode(new Feature(3.1415f)));
		testCV("hello", 1.0f, encode(new Feature("hello")));
		assertNull(encode(new Feature(null, 3.1415f)));
		testCV("hello", 1.0f, encode(new Feature(null, "hello")));
		testCV("hello", 1.0f, encode(new Feature("hello", null)));
		testCV("3.1415f", 1.0f, encode(new Feature("3.1415f", null)));
		
		//test empty strings
		assertNull(encode(new Feature("")));
		assertNull(encode(new Feature("", "")));
		assertNull(encode(new Feature("", 3.1415f)));
		testCV("hello", 1.0f, encode(new Feature("", "hello")));
		testCV("hello", 1.0f, encode(new Feature("hello", "")));
		testCV("3.1415f", 1.0f, encode(new Feature("3.1415f", "")));

		testCV("hello%U003Dgoodbye_two%U003D2", 1.0f, encode(new Feature("hello=goodbye", "two=2")));

		
		//test nominal features
		testCV("hello_goodbye", 1.0f, encode(new Feature("hello", "goodbye")));
		testCV("hello_PathToTypeFeature_goodbye", 1.0f, encode(new TypePathFeature("hello", "goodbye", "path/to/type/feature")));

		//test numeric features
		testCV("hello", 4.0f, encode(new Feature("hello", 4)));
		testCV("hello", 42.0f, encode(new Feature("hello", 42l)));
		testCV("hello", 5.0f, encode(new Feature("hello", 5f)));
		testCV("hello", 6.0f, encode(new Feature("hello", 6d)));
		testCV("hello", 1.234567f, encode(new Feature("hello", 1.234567d)));
		testCV("hello_PathToTypeFeature", 15.0f, encode(new TypePathFeature("hello", 15.0d, "path/to/type/feature")));

		testCV("hello_MR3OOB2", 0.0020f, encode(new WindowFeature(
				"hello", 0.002d, WindowFeature.ORIENTATION_MIDDLE_REVERSE, 3, null, 2)));

	}
	
	private ContextValue encode(Feature feature) {
		List<ContextValue> cvs = encoder.encode(feature);
		if(cvs == null || cvs.size() == 0)
			return null;
		return cvs.get(0);
	}
	
	private void testCV(String context, float value, ContextValue contextValue) {
		assertEquals(context, contextValue.getContext());
		assertEquals(value, contextValue.getValue(), 0.01d);
	}
}
