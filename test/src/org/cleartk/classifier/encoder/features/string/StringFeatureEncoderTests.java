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

import org.apache.uima.UIMAException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.TypePathFeature;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.util.TestsUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class StringFeatureEncoderTests {

	StringEncoder encoder;
	
	@Before
	public void setUp() {
		encoder = new StringEncoder();	
	}
	
	
	@Test
	public void testStringFeatureEncoder() throws UIMAException, IOException {
		this.testEncode("", new Feature(null));
		this.testEncode("3.1415", new Feature(3.1415));
		this.testEncode("true", new Feature(true));
		this.testEncode("aaaaaa", new Feature(null, "aaaaaa"));
		this.testEncode("Ccccccc_aaaaaa", new Feature("Ccccccc", "aaaaaa"));

		Feature pathFeature = new TypePathFeature("bbbbbbb", "aaaaaa", "black/belt/in/karate");
		this.testEncode("bbbbbbb_BlackBeltInKarate_aaaaaa", pathFeature);
		pathFeature = new TypePathFeature(null, null, "black/belt/in/karate");
		this.testEncode("TypePath_BlackBeltInKarate", pathFeature);
		pathFeature = new TypePathFeature(null, null, null);
		this.testEncode("", pathFeature);
		pathFeature = new TypePathFeature(null, "aaaaaa", null);
		this.testEncode("aaaaaa", pathFeature);
		pathFeature = new TypePathFeature(null, new Integer(12345), null);
		this.testEncode("12345", pathFeature);
		pathFeature = new TypePathFeature(null, new Double(12345.6789d), null);
		this.testEncode("12345.6789", pathFeature);
		pathFeature = new TypePathFeature("bbbbbbb", null, null);
		this.testEncode("", pathFeature);

		pathFeature = new TypePathFeature(null, "aaaaaa", "black/belt/in/karate");
		this.testEncode("TypePath_BlackBeltInKarate_aaaaaa", pathFeature);

		this.testEncode("Ccccccc_L0_TypePath_BlackBeltInKarate_aaaaaa", new WindowFeature(
				"Ccccccc", "aaaaaa", WindowFeature.ORIENTATION_LEFT, 0, pathFeature, 0));
		this.testEncode("Window_L0_TypePath_BlackBeltInKarate_aaaaaa", new WindowFeature(
				null, "aaaaaa", WindowFeature.ORIENTATION_LEFT, 0, pathFeature, 0));
		this.testEncode("Ccccccc_L0_aaaaaa", new WindowFeature(
				"Ccccccc", "aaaaaa", WindowFeature.ORIENTATION_LEFT, 0, null, 0));
		this.testEncode("Ccccccc_MR3OOB2_aaaaaa", new WindowFeature(
				"Ccccccc", "aaaaaa", WindowFeature.ORIENTATION_MIDDLE_REVERSE, 3, null, 2));
		this.testEncode("Window_MR3OOB2", new WindowFeature(
				null, null, WindowFeature.ORIENTATION_MIDDLE_REVERSE, 3, null, 2));
		this.testEncode("Window_MR3OOB2_123456", new WindowFeature(
				null, 123456, WindowFeature.ORIENTATION_MIDDLE_REVERSE, 3, null, 2));
	}

	@Test
	public void testTicket23() throws UIMAException, IOException {
		TestsUtil.process("aaaaaaa bbbbbb cc ddddd e ffffff.  Ggggggg hhhhhh iiiiii.");

		Feature typePathFeature = new TypePathFeature(null, null, "pos");
		this.testEncode("Window_L2OOB2_TypePath_Pos", new WindowFeature(
				null, null, WindowFeature.ORIENTATION_LEFT, 2, typePathFeature, 2));
		this.testEncode("Window_L2OOB2_TypePath_Pos", new WindowFeature(
				null, null, WindowFeature.ORIENTATION_LEFT, 2, typePathFeature, 2));
		this.testEncode("Window_L2_TypePath_Pos", new WindowFeature(
				null, null, WindowFeature.ORIENTATION_LEFT, 2, typePathFeature));
		this.testEncode("Window_L2", new WindowFeature(
				null, null, WindowFeature.ORIENTATION_LEFT, 2, null, 0));
	}

	
	private void testEncode(String expected, Feature feature) {
		assertEquals(expected, encoder.encode(feature).get(0));
	}

}
