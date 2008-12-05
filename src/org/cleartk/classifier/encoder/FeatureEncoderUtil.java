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
package org.cleartk.classifier.encoder;

import java.text.StringCharacterIterator;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class FeatureEncoderUtil {

	public static String escape(String source) {
		return escape(source, new char[0]);
	}

	public static String escape(String source, char[] escapeCharacters) {
			StringBuffer escapedBuffer = new StringBuffer();
			StringCharacterIterator it = new StringCharacterIterator(source);
			
			for( char c = it.first(); c != StringCharacterIterator.DONE; c = it.next() ) {
				/*
				 * 92 is \
				 * 37 is %
				 * 33 is A
				 * 126 is ~
				 */
				boolean escaped = false;
				if( c < 33 || c > 126 || c == 92 || c == 37) {
					escapedBuffer.append(String.format("%%U%04X", (int) c));
					escaped = true;
				}
				if(!escaped) {
					for(char esc : escapeCharacters) {
						if(c == esc) {
							escapedBuffer.append(String.format("%%U%04X", (int) c));
							escaped = true;
							break;
						}
					}
				}
				if(!escaped)
					escapedBuffer.append(c);
			}

			return escapedBuffer.toString();

	}
	
	
}
