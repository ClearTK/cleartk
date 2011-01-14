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
package org.cleartk.token.tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 */
public abstract class Tokenizer_ImplBase implements Tokenizer {

  public abstract String[] getTokenTexts(String text);

  /**
   * performs tokenization on the input text and returns the character offsets corresponding to the
   * begining and end of each token w.r.t. the input text.
   * 
   * @param text
   *          the text that you want tokenized.
   * @return offsets corresponding to the begining and end of each token. The dimensions of the
   *         returned array will be number of tokens returned by getTokens by 2 (one for token begin
   *         and one for token end).
   * 
   *         <pre>
   * 
   *         <code> int[][] offsets = getTokenOffsets("example text"); int
   *         beginFirstToken = offsets[0][0]; int endFirstToken =
   *         offsets[0][1]; int begin4thToken = offsets[3][0]; int end4thToken
   *         = offsets[3][1];
   * 
   * </pre>
   * 
   *         </code>
   * 
   */
  public List<Token> getTokens(String text) {
    List<Token> returnValues = new ArrayList<Token>();
    String[] tokenTexts = getTokenTexts(text);
    if (text.length() > 0) {
      int offset = 0;
      for (String tokenText : tokenTexts) {
        while (text.charAt(offset) != tokenText.charAt(0))
          offset++;

        Token token = new Token(offset, offset + tokenText.length(), tokenText);
        returnValues.add(token);
        offset += tokenText.length();
      }
    }

    return returnValues;
  }

}
