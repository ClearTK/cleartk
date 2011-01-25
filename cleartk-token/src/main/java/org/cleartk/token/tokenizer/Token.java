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

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 */

public class Token {
  private int begin;

  private int end;

  private String tokenText;

  public Token(int begin, int end, String tokenText) {
    super();
    if (end < begin)
      throw new IllegalArgumentException(
          String
              .format(
                  "the end of a token must follow the beginning of a token: begin=%1$d, end=%2$d, token text='%3$s'",
                  begin,
                  end,
                  tokenText));
    this.begin = begin;
    this.end = end;
    this.tokenText = tokenText == null ? "" : tokenText;
    if (this.tokenText.length() != (end - begin))
      throw new IllegalArgumentException(
          String
              .format(
                  "the length of the token text must equal the extent specified by begin and end: token text length=%1$d, (end - begin)=%2$d, token text='%3$s'",
                  this.tokenText.length(),
                  (end - begin),
                  tokenText));
  }

  public int getBegin() {
    return begin;
  }

  public int getEnd() {
    return end;
  }

  public String getTokenText() {
    return tokenText;
  }

  @Override
  public int hashCode() {
    return tokenText.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Token) {
      Token token = (Token) obj;
      if (tokenText.equals(token.getTokenText()) && begin == token.getBegin()
          && end == token.getEnd())
        return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return tokenText + "[" + begin + "," + end + "]";
  }

}
