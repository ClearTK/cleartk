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

package org.cleartk.classifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cleartk.classifier.util.InstanceFactory;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Philip Ogren
 */

public class FeatureTest {

  @Test
  public void testFactoryMethod() {
    Feature feature1 = new Feature("asdf", 1);
    Feature feature2 = Feature.createFeature("qwerty", feature1);
    assertEquals("qwerty_asdf", feature2.getName());
    assertEquals(1, feature2.getValue());

    Instance<String> instance = InstanceFactory.createInstance(
        "A",
        "feature1",
        1,
        "feature1",
        1,
        "feature1",
        2);
    Feature feature = instance.getFeatures().get(0);
    assertEquals("Feature(<feature1>, <1>)", feature.toString());
    assertEquals(-420503769, feature.hashCode());
    assertTrue(feature.equals(instance.getFeatures().get(1)));
    assertFalse(feature.equals(instance.getFeatures().get(2)));
    assertFalse(feature.equals("feature1"));

    instance = InstanceFactory.createInstance("A", "feature1 feature1");
    feature = instance.getFeatures().get(0);
    assertEquals("Feature(<feature1>, <null>)", feature.toString());
    assertEquals(-420503770, feature.hashCode());
    assertTrue(feature.equals(instance.getFeatures().get(1)));

    feature = new Feature(5);
    assertFalse(feature.equals(instance.getFeatures().get(0)));
    assertEquals(966, feature.hashCode());
    feature.setValue(6);
    assertEquals(6, feature.getValue());
  }
}
