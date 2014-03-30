/* 
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
package org.cleartk.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.Instances;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link Instances} class.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class InstancesTest {

  @Test
  public void test() {
    List<String> outcomes = Arrays.asList("X", "Y", "Z");
    List<Feature> features1 = Arrays.asList(new Feature("foo", 42), new Feature("bar", "a"));
    List<Feature> features2 = Arrays.asList(new Feature("foo", -1), new Feature("bar", "b"));
    List<Feature> features3 = Arrays.asList(new Feature("foo", 13), new Feature("bar", "c"));
    List<List<Feature>> featureLists = new ArrayList<List<Feature>>();
    featureLists.add(features1);
    featureLists.add(features2);
    featureLists.add(features3);

    List<Instance<String>> expected = new ArrayList<Instance<String>>();
    expected.add(new Instance<String>("X", features1));
    expected.add(new Instance<String>("Y", features2));
    expected.add(new Instance<String>("Z", features3));

    List<Instance<String>> actual = Instances.toInstances(outcomes, featureLists);
    Assert.assertEquals(expected, actual);
  }

}
