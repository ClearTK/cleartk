/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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

package org.cleartk.classifier.feature.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class CaseUtilTest {

  @Test
  public void testIsInitialUppercase() throws Exception {
    assertTrue(CaseUtil.isInitialUppercase("Orange", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("O", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("o", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("orange", Locale.US));
    assertTrue(CaseUtil.isInitialUppercase("Or", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("ORANGE", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("OrangE", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("oRANGE", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("1asdf", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("A1", Locale.US));
    assertTrue(CaseUtil.isInitialUppercase("A1asdf", Locale.US));
    assertFalse(CaseUtil.isInitialUppercase("A1234", Locale.US));
    assertTrue(CaseUtil.isInitialUppercase("A1234a", Locale.US));
  }

  @Test
  public void testIsAllUppercase() throws Exception {
    assertFalse(CaseUtil.isAllUppercase("Orange", Locale.US));
    assertTrue(CaseUtil.isAllUppercase("O", Locale.US));
    assertFalse(CaseUtil.isAllUppercase("o", Locale.US));
    assertFalse(CaseUtil.isAllUppercase("orange", Locale.US));
    assertFalse(CaseUtil.isAllUppercase("Or", Locale.US));
    assertTrue(CaseUtil.isAllUppercase("ORANGE", Locale.US));
    assertFalse(CaseUtil.isAllUppercase("OrangE", Locale.US));
    assertFalse(CaseUtil.isAllUppercase("oRANGE", Locale.US));
    assertFalse(CaseUtil.isAllUppercase("1234", Locale.US));
    assertTrue(CaseUtil.isAllUppercase("A1234B", Locale.US));

  }

}
