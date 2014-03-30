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
package org.cleartk.ml.feature;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.cleartk.ml.feature.TypePathFeature;
import org.junit.Test;

public class TypePathFeatureTest {

  @Test
  public void testgetName() {
    TypePathFeature typePathFeature = new TypePathFeature(null, null, "asdf/asdf");

    assertEquals("TypePath(AsdfAsdf)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "pos");
    assertEquals("TypePath(Pos)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "Asdf/1asdf");
    assertEquals("TypePath(Asdf1asdf)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "A/B/C/D");
    assertEquals("TypePath(ABCD)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "A/1");
    assertEquals("TypePath(A1)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "a/b/c/d/e");
    assertEquals("TypePath(ABCDE)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "ab/cd/ef/gh");
    assertEquals("TypePath(AbCdEfGh)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "abcd/efgh");
    assertEquals("TypePath(AbcdEfgh)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "abcd/efgh");
    assertEquals("TypePath(AbcdEfgh)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "abcd/efgh/");
    assertEquals("TypePath(AbcdEfgh)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "/abcd/efgh/");
    assertEquals("TypePath(AbcdEfgh)", typePathFeature.getName());

    typePathFeature = new TypePathFeature(null, null, "/");
    assertNull(typePathFeature.getName());

    typePathFeature = new TypePathFeature("PATH", null, "/abcd/efgh/");
    assertEquals("PATH(AbcdEfgh)", typePathFeature.getName());

    typePathFeature = new TypePathFeature("PATH", null, "/");
    assertNull(typePathFeature.getName());

  }

}
