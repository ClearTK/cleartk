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
package org.cleartk.token.tokenizer.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cleartk.token.tokenizer.Subtokenizer;
import org.cleartk.token.tokenizer.Token;
import org.cleartk.token.tokenizer.Tokenizer;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class SubtokenizerTest {

  @Test
  public void testSubtokens() {
    Tokenizer tokenizer = new Subtokenizer();
    List<Token> tokens;

    tokens = tokenizer.getTokens("Asdf");
    assertEquals(1, tokens.size());
    assertEquals("Asdf", tokens.get(0).getTokenText());

    tokens = tokenizer.getTokens("Asdf123");
    assertEquals(2, tokens.size());
    assertEquals("Asdf", tokens.get(0).getTokenText());
    assertEquals("123", tokens.get(1).getTokenText());

    tokens = tokenizer.getTokens("Asdf-123");
    assertEquals(3, tokens.size());
    assertEquals("Asdf", tokens.get(0).getTokenText());
    assertEquals("-", tokens.get(1).getTokenText());
    assertEquals("123", tokens.get(2).getTokenText());

    tokens = tokenizer.getTokens("B/CD28-responsive");
    assertEquals(6, tokens.size());
    assertEquals("B", tokens.get(0).getTokenText());
    assertEquals("/", tokens.get(1).getTokenText());
    assertEquals("CD", tokens.get(2).getTokenText());
    assertEquals("28", tokens.get(3).getTokenText());
    assertEquals("-", tokens.get(4).getTokenText());
    assertEquals("responsive", tokens.get(5).getTokenText());

    tokens = tokenizer.getTokens("activity,");
    assertEquals(2, tokens.size());
    assertEquals("activity", tokens.get(0).getTokenText());
    assertEquals(",", tokens.get(1).getTokenText());

    tokens = tokenizer.getTokens("(IL-2)");
    assertEquals(5, tokens.size());
    assertEquals("(", tokens.get(0).getTokenText());
    assertEquals("IL", tokens.get(1).getTokenText());
    assertEquals("-", tokens.get(2).getTokenText());
    assertEquals("2", tokens.get(3).getTokenText());
    assertEquals(")", tokens.get(4).getTokenText());

    tokens = tokenizer
        .getTokens("These findings should be useful for therapeutic strategies and the development of immunosuppressants targeting the CD28 costimulatory pathway. ");
    assertEquals(20, tokens.size());

  }

}
