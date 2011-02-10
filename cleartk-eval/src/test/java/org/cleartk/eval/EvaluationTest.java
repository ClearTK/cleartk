/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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

package org.cleartk.eval;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Philip Ogren
 */

public class EvaluationTest {

  @Test
  public void testCreateFoldName() throws Exception {
    assertEquals("fold-1", Evaluation.createFoldName(0, 1));
    assertEquals("fold-01", Evaluation.createFoldName(0, 10));
    assertEquals("fold-001", Evaluation.createFoldName(0, 100));
    assertEquals("fold-0004", Evaluation.createFoldName(3, 1000));
    assertEquals("fold-0004", Evaluation.createFoldName(3, 4444));
    assertEquals("fold-0987", Evaluation.createFoldName(986, 1200));
    assertEquals("fold-4444", Evaluation.createFoldName(4443, 4444));
    assertEquals("fold-10", Evaluation.createFoldName(9, 10));
    assertEquals("fold-9999", Evaluation.createFoldName(9998, 9999));
    assertEquals("fold-09999", Evaluation.createFoldName(9998, 10000));
    assertEquals("fold-090", Evaluation.createFoldName(89, 100));
  }
}
