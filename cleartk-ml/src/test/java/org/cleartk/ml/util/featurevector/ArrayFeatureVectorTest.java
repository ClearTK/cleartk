/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.ml.util.featurevector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.cleartk.ml.util.featurevector.ArrayFeatureVector;
import org.cleartk.ml.util.featurevector.FeatureVector;
import org.cleartk.ml.util.featurevector.SparseFeatureVector;
import org.junit.Before;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philipp G. Wetzler
 */
public class ArrayFeatureVectorTest {

  @Before
  public void setUp() throws Throwable {
    fv1 = new ArrayFeatureVector();
    fv2 = new ArrayFeatureVector();
    fv2.set(1, 4.5);
    fv2.set(5, 7.1);
    fv2.set(7, 1.3);
    fv2.set(10, 2.3);
    fv2.set(7, 0);
    fv2.set(6, 13);
    fv2.set(3, 0);
  }

  @Test
  public void testPlainConstructor() {
    Iterator<FeatureVector.Entry> it = fv1.iterator();
    assertFalse(it.hasNext());
  }

  @Test
  public void testGet() {
    assertEquals(fv2.get(0), 0, 0.0);
    assertEquals(fv2.get(1), 4.5, delta);
    assertEquals(fv2.get(2), 0, 0.0);
    assertEquals(fv2.get(3), 0, 0.0);
    assertEquals(fv2.get(4), 0, 0.0);
    assertEquals(fv2.get(5), 7.1, delta);
    assertEquals(fv2.get(6), 13, delta);
    assertEquals(fv2.get(7), 0, 0.0);
    assertEquals(fv2.get(8), 0, 0.0);
    assertEquals(fv2.get(9), 0, 0.0);
    assertEquals(fv2.get(10), 2.3, delta);
    assertEquals(fv2.get(11), 0, 0.0);
  }

  @Test
  public void toArray() {
    double[] a = fv2.toArray();
    double[] expected = new double[] { 0, 4.5, 0, 0, 0, 7.1, 13, 0, 0, 0, 2.3 };

    for (int i = 0; i < a.length; i++) {
      if (i < expected.length)
        assertEquals(a[i], expected[i], delta);
      else
        assertEquals(a[i], 0.0, 0.0);
    }
  }

  @Test
  public void testEquals() throws Throwable {
    FeatureVector fv = new ArrayFeatureVector();
    fv.set(1, 4.5);
    fv.set(5, 7.1);
    fv.set(6, 13);
    assertFalse(fv.equals(fv2));
    fv.set(10, 2.3);
    assertTrue(fv.equals(fv2));
    assertEquals(fv.hashCode(), fv2.hashCode());
    fv.set(11, 7);
    assertFalse(fv.equals(fv2));
    fv.set(5, 7.2);
    assertFalse(fv.equals(fv2));
    assertFalse(fv.equals(new Integer(5)));
  }

  @Test
  public void testRemove() {
    boolean exceptionThrown = false;
    try {
      Iterator<FeatureVector.Entry> it = fv2.iterator();
      it.remove();
    } catch (UnsupportedOperationException e) {
      exceptionThrown = true;
    }

    assertTrue(exceptionThrown);
  }

  @Test
  public void testInnerProduct() throws Throwable {
    FeatureVector fv = new SparseFeatureVector();
    fv.set(1, 1);
    fv.set(3, 2.5);
    fv.set(6, 0.5);

    assertEquals(fv.innerProduct(fv2), 11, delta);
    assertEquals(fv2.innerProduct(fv), 11, delta);
  }

  static final double delta = 0.0000001;

  FeatureVector fv1;

  ArrayFeatureVector fv2;

  FeatureVector fv3;

}
