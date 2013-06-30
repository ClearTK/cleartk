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
import static org.junit.Assert.assertNotNull;

import org.cleartk.classifier.util.InstanceFactory;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class InstanceFactoryTest {

  @Test
  public void testCreateInstanceString() {
    Instance<String> instance = InstanceFactory.createInstance("A", "a b c d");
    assertEquals("A", instance.getOutcome());
    assertEquals(4, instance.getFeatures().size());
    assertEquals("a", instance.getFeatures().get(0).getName());
    assertEquals("b", instance.getFeatures().get(1).getName());
    assertEquals("c", instance.getFeatures().get(2).getName());
    assertEquals("d", instance.getFeatures().get(3).getName());

    assertEquals("Feature(<a>, <null>)", instance.getFeatures().get(0).toString());

    instance = InstanceFactory
        .createInstance(
            "O",
            "Word_Three LCWord_three CapitalType_INITIAL_UPPERCASE L0OOB1 L1OOB2 R0_sequence R0_TypePath_Pos_NN R0_TypePath_Stem_sequenc R1_elements R1_TypePath_Pos_NNS R1_TypePath_Stem_element TypePath_Pos_CD TypePath_Stem_Three PrevNEMTokenLabel_L0OOB1 PrevNEMTokenLabel_L1OOB2");
    assertEquals("O", instance.getOutcome());
    assertEquals(15, instance.getFeatures().size());
    assertEquals("Word_Three", instance.getFeatures().get(0).getName());

    IllegalArgumentException iae = null;
    try {
      InstanceFactory.createInstance("A", "feature1", 1, "feature2");

    } catch (IllegalArgumentException e) {
      iae = e;
    }
    assertNotNull(iae);
  }

  @Test
  public void testConstrucor() {
    assertNotNull(new InstanceFactory());
  }

}
