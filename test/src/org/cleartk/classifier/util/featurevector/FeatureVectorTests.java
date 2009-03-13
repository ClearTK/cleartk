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
package org.cleartk.classifier.util.featurevector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 * 
 * @author Philipp G. Wetzler
 */
public class FeatureVectorTests {
	
	@Before
	public void setUp() {
		fv1 = new SparseFeatureVector();
		fv2 = new SparseFeatureVector();
		fv2.set(1, 4.5);
		fv2.set(5, 7.1);
		fv2.set(7, 1.3);
		fv2.set(10, 2.3);
		fv2.set(7, 0);
		fv2.set(6, 13);
		fv2.set(3, 0);
		fv3 = new ArrayFeatureVector();
		fv3.set(1, 1);
		fv3.set(3, 2.5);
		fv3.set(6, 0.5);
	}
	
	@Test
	public void testAdd() {
		fv1.add(fv2);
		assertTrue(fv1.equals(fv2));
		
		fv1.add(fv3);
		assertEquals(fv1.get(0), 0.0, 0.0);
		assertEquals(fv1.get(1), 5.5, delta);
		assertEquals(fv1.get(2), 0.0, 0.0);
		assertEquals(fv1.get(3), 2.5, delta);
		assertEquals(fv1.get(4), 0.0, 0.0);
		assertEquals(fv1.get(5), 7.1, delta);
		assertEquals(fv1.get(6), 13.5, delta);
		assertEquals(fv1.get(7), 0.0, 0.0);
		assertEquals(fv1.get(8), 0.0, 0.0);
		assertEquals(fv1.get(9), 0.0, 0.0);
		assertEquals(fv1.get(10), 2.3, delta);
		assertEquals(fv1.get(11), 0.0, 0.0);
	}
	
	@Test
	public void testMultiply() {
		fv1.multiply(3.3);
		assertTrue(fv1.equals(new ArrayFeatureVector()));
		
		fv2.multiply(0.5);
		assertEquals(0.0, fv2.get(0), 0.0);
		assertEquals(2.25, fv2.get(1), delta);
		assertEquals(0.0, fv2.get(2), 0.0);
		assertEquals(0.0, fv2.get(3), 0.0);
		assertEquals(0.0, fv2.get(4), 0.0);
		assertEquals(3.55, fv2.get(5), delta);
		assertEquals(6.5, fv2.get(6), delta);
		assertEquals(0.0, fv2.get(7), 0.0);
		assertEquals(0.0, fv2.get(8), 0.0);
		assertEquals(0.0, fv2.get(9), 0.0);
		assertEquals(1.15, fv2.get(10), delta);
		assertEquals(0.0, fv2.get(11), 0.0);
	}
	
	@Test
	public void testl2Norm() {
		assertEquals(0.0, fv1.l2Norm(), 0.0);
		assertEquals(15.65087, fv2.l2Norm(), delta);
	}
	
	@Test
	public void testEntryEquals() {
		FeatureVector.Entry e1 = new FeatureVector.Entry(1, 2.3);
		FeatureVector.Entry e2 = new FeatureVector.Entry(1, 2.3);
		FeatureVector.Entry e3 = new FeatureVector.Entry(3, 2.3);
		FeatureVector.Entry e4 = new FeatureVector.Entry(1, 1.5);

		assertTrue(e1.equals(e2));
		assertEquals(e1.hashCode(), e2.hashCode());
		assertFalse(e1.equals(e3));
		assertFalse(e1.equals(e4));
		assertFalse(e1.equals(new Integer(42)));
	}
		
	static final double delta = 0.001;
	FeatureVector fv1;
	FeatureVector fv2;
	FeatureVector fv3;

}
