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
package org.cleartk.token.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cleartk.token.tokenizer.PennTreebankTokenizer;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class PennTreebankTokenizerTest {
	@Test
	public void testHyphens() {
		PennTreebankTokenizer tokenizer = new PennTreebankTokenizer();
		List<org.cleartk.token.tokenizer.Token> tokens = tokenizer.getTokens("10-1234");
		assertEquals(1, tokens.size());
		assertEquals("10-1234", tokens.get(0).getTokenText());

		tokens = tokenizer.getTokens("ASDF-1234");
		assertEquals(1, tokens.size());
		assertEquals("ASDF-1234", tokens.get(0).getTokenText());
	}

	@Test
	public void testBar() {
		PennTreebankTokenizer tokenizer = new PennTreebankTokenizer();
		List<org.cleartk.token.tokenizer.Token> tokens = tokenizer.getTokens("10|1234");
		assertEquals(3, tokens.size());
		assertEquals("10", tokens.get(0).getTokenText());
		assertEquals("|", tokens.get(1).getTokenText());
		assertEquals("1234", tokens.get(2).getTokenText());
	}
	
	@Test
	public void testSingleToken() {
		PennTreebankTokenizer tokenizer = new PennTreebankTokenizer();
		List<org.cleartk.token.tokenizer.Token> tokens = tokenizer.getTokens("asdf");
		assertEquals(1, tokens.size());
		assertEquals("asdf", tokens.get(0).getTokenText());

		tokens = tokenizer.getTokens(" ");
		assertEquals(0, tokens.size());

		tokens = tokenizer.getTokens("   asdf  ");
		assertEquals(1, tokens.size());
		assertEquals("asdf", tokens.get(0).getTokenText());

		tokens = tokenizer.getTokens("     \t");
		assertEquals(0, tokens.size());

	}
}
