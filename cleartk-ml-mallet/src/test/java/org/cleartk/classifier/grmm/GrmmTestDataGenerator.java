/**
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Würzburg nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
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
package org.cleartk.classifier.grmm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;

/**
 * <br>
 * Copyright (c) 2010, University of Würzburg <br>
 * All rights reserved.
 * <p>
 * 
 * @author Martin Toepfer
 */
public class GrmmTestDataGenerator {

  public static List<Instance<String[]>> createInstances1() {
    List<Instance<String[]>> instances = new java.util.ArrayList<Instance<String[]>>();
    instances.add(createInstance("A a fa"));
    return instances;
  }

  /**
   * 
   * @return list of instances with the same features as {@link
   *         GrmmClassifierTest.createInstances1()} but different outcomes
   */
  public static List<Instance<String[]>> createInstances1test() {
    List<Instance<String[]>> instances = new java.util.ArrayList<Instance<String[]>>();
    instances.add(createInstance("B d fa"));
    return instances;
  }

  public static List<Instance<String[]>> createInstances2() {
    List<Instance<String[]>> instances = new java.util.ArrayList<Instance<String[]>>();
    instances.add(createInstance("A b f1 f2"));
    instances.add(createInstance("C d f3 f4"));
    instances.add(createInstance("A d f1 f4"));
    instances.add(createInstance("B b f2 f5"));
    instances.add(createInstance("B b f2 f5"));
    return instances;
  }

  /**
   * 
   * @return list of instances with the same features as {@link
   *         GrmmClassifierTest.createInstances2()} but different outcomes
   */
  public static List<Instance<String[]>> createInstances2test() {
    List<Instance<String[]>> instances = new java.util.ArrayList<Instance<String[]>>();
    instances.add(createInstance("C a f1 f2"));
    instances.add(createInstance("C a f3 f4"));
    instances.add(createInstance("C a f1 f4"));
    instances.add(createInstance("C a f2 f5"));
    instances.add(createInstance("C a f2 f5"));
    return instances;
  }

  public static List<Instance<String[]>> createInstances3() {
    List<Instance<String[]>> instances = new java.util.ArrayList<Instance<String[]>>();
    instances.add(createInstance("A a faabb-a1"));
    instances.add(createInstance("B b faabb-b2"));
    return instances;
  }

  public static Instance<String[]> createInstance(String data) {
    Instance<String[]> instance = new Instance<String[]>();
    String[] columns = data.split(" ");
    ArrayList<String> elements = new ArrayList<String>(2);
    elements.add(columns[0]);
    elements.add(columns[1]);
    instance.setOutcome(elements.toArray(new String[2]));
    for (int i = 2; i < columns.length; i++) {
      instance.add(new Feature(columns[i]));
    }
    return instance;
  }

  public static File createBigramTemplate(String outputDirectoryName, String filename)
      throws IOException {
    File f = new File(outputDirectoryName, filename);
    FileWriter fileWriter = new FileWriter(f);
    fileWriter
        .write("new ACRF.BigramTemplate (0)\nnew ACRF.BigramTemplate (1)\nnew ACRF.PairwiseFactorTemplate (0,1)\n");
    fileWriter.close();
    return f;
  }
}
