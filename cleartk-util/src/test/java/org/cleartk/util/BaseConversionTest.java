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
package org.cleartk.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */
public class BaseConversionTest {

  @Test
  public void testConvertBase() {
    assertEquals("0", BaseConversion.convertBase(0, 1));
    assertEquals("11111", BaseConversion.convertBase(5, 1));
    assertEquals("101", BaseConversion.convertBase(5, 2));
    assertEquals("12", BaseConversion.convertBase(5, 3));
    assertEquals("11", BaseConversion.convertBase(5, 4));
    assertEquals("10", BaseConversion.convertBase(5, 5));
    assertEquals("5", BaseConversion.convertBase(5, 6));

    assertEquals("1111111111111111", BaseConversion.convertBase(16, 1));
    assertEquals("10000", BaseConversion.convertBase(16, 2));
    assertEquals("121", BaseConversion.convertBase(16, 3));
    assertEquals("100", BaseConversion.convertBase(16, 4));
    assertEquals("31", BaseConversion.convertBase(16, 5));
    assertEquals("24", BaseConversion.convertBase(16, 6));
    assertEquals("22", BaseConversion.convertBase(16, 7));
    assertEquals("20", BaseConversion.convertBase(16, 8));
    assertEquals("17", BaseConversion.convertBase(16, 9));
    assertEquals("16", BaseConversion.convertBase(16, 10));
    assertEquals("15", BaseConversion.convertBase(16, 11));
    assertEquals("14", BaseConversion.convertBase(16, 12));
    assertEquals("13", BaseConversion.convertBase(16, 13));
    assertEquals("12", BaseConversion.convertBase(16, 14));
    assertEquals("11", BaseConversion.convertBase(16, 15));
    assertEquals("10", BaseConversion.convertBase(16, 16));
    assertEquals("G", BaseConversion.convertBase(16, 17));

    assertEquals("4000", BaseConversion.convertBase(953312, 62));
    assertEquals("C00", BaseConversion.convertBase(46128, 62));
    assertEquals("90", BaseConversion.convertBase(558, 62));
    assertEquals("2", BaseConversion.convertBase(2, 62));
    assertEquals("4C92", BaseConversion.convertBase(1000000, 62));

  }
}
