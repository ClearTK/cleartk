/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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

package org.cleartk.ml.feature.transform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.feature.transform.InstanceStream;
import org.junit.Test;

import com.google.common.io.Files;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class InstanceStreamTest {

  @Test
  public void TestIteration() throws Exception {

    // Write out instances
    Instance<Boolean> instance1 = new Instance<Boolean>();
    instance1.add(new Feature("WORD_LEFT", "Buffalo"));
    instance1.add(new Feature("WORD_RIGHT", "Waffle"));
    instance1.setOutcome(false);
    Instance<Boolean> instance2 = new Instance<Boolean>();
    instance2.add(new Feature("PUMPKIN", 3.14));
    instance2.add(new Feature("IS_FALSE", true));
    instance2.setOutcome(true);
    InstanceStream.Terminator<Boolean> terminator = new InstanceStream.Terminator<Boolean>();

    File tmpDir = Files.createTempDir();
    File file = new File(tmpDir, "instances.dat");

    try {
      FileOutputStream fos = new FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(instance1);
      oos.writeObject(instance2);
      oos.writeObject(terminator);
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Test iterator
    Iterator<Instance<Boolean>> iterator = new InstanceStream.Iterator<Boolean>(file.toURI());
    int i = 0;
    while (iterator.hasNext()) {
      Instance<Boolean> inst = iterator.next();
      assertFalse(inst == null);
      if (i == 0) {
        assertTrue(inst.getFeatures().get(0).getValue().equals("Buffalo"));
        assertTrue(inst.getFeatures().get(1).getValue().equals("Waffle"));
        assertTrue(inst.getOutcome() == false);
      }

      if (i == 1) {
        assertTrue(inst.getFeatures().get(0).getName().equals("PUMPKIN"));
        assertTrue(inst.getFeatures().get(1).getName().equals("IS_FALSE"));
        assertTrue(inst.getOutcome() == true);
      }
      i++;
    }
    assertTrue(i == 2);

    // Test iterable style iteration
    i = 0;
    for (Instance<Boolean> inst : new InstanceStream<Boolean>(file.toURI())) {
      assertFalse(inst == null);
      if (i == 0) {
        assertTrue(inst.getFeatures().get(0).getValue().equals("Buffalo"));
        assertTrue(inst.getFeatures().get(1).getValue().equals("Waffle"));
        assertTrue(inst.getOutcome() == false);
      }

      if (i == 1) {
        assertTrue(inst.getFeatures().get(0).getName().equals("PUMPKIN"));
        assertTrue(inst.getFeatures().get(1).getName().equals("IS_FALSE"));
        assertTrue(inst.getOutcome() == true);
      }

      i++;
    }
    assertTrue(i == 2);

  }
}
