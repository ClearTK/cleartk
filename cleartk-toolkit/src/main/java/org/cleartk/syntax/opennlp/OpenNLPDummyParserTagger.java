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
package org.cleartk.syntax.opennlp;

import java.util.List;

import opennlp.tools.parser.ParserTagger;
import opennlp.tools.util.Sequence;

import org.cleartk.type.Token;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 *
 * @author Philipp Wetzler
 */

public class OpenNLPDummyParserTagger implements ParserTagger {

	private Token[] tokens;
	
	public OpenNLPDummyParserTagger() {
	}
	
	public void setTokens(Token[] tokens) {
		this.tokens = tokens;
	}
	
	@SuppressWarnings("rawtypes")
	public Sequence[] topKSequences(List sentence) {
		throw new UnsupportedOperationException();
	}

	public Sequence[] topKSequences(String[] sentence) {
		Sequence[] s = new Sequence[1];
		
		s[0] = new Sequence();
		for( Token token : this.tokens ) {
			String pos = token.getPos();
			if (pos == null) {
				throw new RuntimeException(
						"no part of speech for token: " + token.getCoveredText());
			}
			s[0].add(token.getPos(), 1.0);
		}

		return s;
	}

	@SuppressWarnings({"rawtypes"})
	public List<?> tag(List sentence) {
		throw new UnsupportedOperationException();
	}

	public String[] tag(String[] sentence) {
		throw new UnsupportedOperationException();
	}

	public String tag(String sentence) {
		throw new UnsupportedOperationException();
	}

}
