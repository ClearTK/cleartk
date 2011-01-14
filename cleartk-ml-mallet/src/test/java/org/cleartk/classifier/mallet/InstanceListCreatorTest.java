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
package org.cleartk.classifier.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */

public class InstanceListCreatorTest {

  @Test
  public void testMain() throws IOException {
    InstanceListCreator instanceListCreator = new InstanceListCreator();
    InstanceList instanceList = instanceListCreator.createInstanceList(new File(
            "src/test/resources/data/mallet/instance-list-creator.txt"));

    assertEquals(100, instanceList.size());

    Alphabet dataAlphabet = instanceList.getDataAlphabet();
    int index = dataAlphabet.lookupIndex("SpannedText_In", false);
    assertNotSame(-1, index);

    Instance instance = instanceList.get(0);
    assertEquals("IN", instance.getTarget().toString());
    assertTrue(instance.getData() instanceof FeatureVector);
    assertEquals(1.0, ((FeatureVector) instance.getData()).value("SpannedText_In"), 0.001);
    assertEquals(2.0, ((FeatureVector) instance.getData()).value("LowerCase_SpannedText_in"), 0.001);
    assertEquals(
            0.12345,
            ((FeatureVector) instance.getData()).value("CapitalType_SpannedText_INITIAL_UPPERCASE"),
            0.001);
    assertEquals(10000.0,
            ((FeatureVector) instance.getData()).value("NGram_Left_0_2_2_SpannedText_In"), 0.001);
    assertEquals(12345.6789,
            ((FeatureVector) instance.getData()).value("NGram_Right_0_2_2_SpannedText_In"), 0.001);
    assertEquals(1.0, ((FeatureVector) instance.getData()).value("TypePath_Stem_in"), 0.001);

    instance = instanceList.get(1);
    assertEquals("DT", instance.getTarget().toString());
    assertEquals(1.0, ((FeatureVector) instance.getData()).value("SpannedText_an"), 0.001);

    instance = instanceList.get(98);
    assertEquals("NN", instance.getTarget().toString());
    assertEquals(32.0, ((FeatureVector) instance.getData()).value("SpannedText_growth"), 0.001);
    assertEquals(33.0, ((FeatureVector) instance.getData()).value("LowerCase_SpannedText_growth"),
            0.001);

    instance = instanceList.get(99);
    assertEquals("IN", instance.getTarget().toString());
    assertEquals(42.0, ((FeatureVector) instance.getData()).value("SpannedText_for"), 0.001);
    assertEquals(43.0, ((FeatureVector) instance.getData()).value("LowerCase_SpannedText_for"),
            0.001);

    Alphabet targetAlphabet = instanceList.getTargetAlphabet();
    index = targetAlphabet.lookupIndex("IN", false);
    assertNotSame(-1, index);
    index = targetAlphabet.lookupIndex("spaghetti", false);
    assertEquals(-1, index);

  }
}
