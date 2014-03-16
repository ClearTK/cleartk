/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.ml.weka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.annotations.Beta;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */
@Beta
public class WekaTest {

  public static void main(String[] args) {
    // Declare the class attribute along with its values
    Attribute classAttribute = new Attribute("theClass", Arrays.asList("positive", "negative"), 0);

    Attribute attribute1 = new Attribute("firstNumeric", 1);
    Attribute attribute2 = new Attribute("secondNumeric", 2);

    // Declare a nominal attribute along with its values
    Attribute attribute3 = new Attribute("aNominal", Arrays.asList("blue", "gray", "black"), 3);

    // Declare the feature vector
    Instance instance = new DenseInstance(4);
    instance.setValue(classAttribute, "positive");
    instance.setValue(attribute1, 1.0);

    Instance instance1 = new DenseInstance(4);
    instance1.setValue(classAttribute, "positive");
    instance1.setValue(attribute1, 1.0);
    instance1.setValue(attribute2, 0.5);
    instance1.setValue(attribute3, "gray");

    List<Attribute> attributes = Arrays.asList(classAttribute, attribute1, attribute2, attribute3);
    Instances instances = new Instances("Rel", new ArrayList<Attribute>(attributes), 10);

    // Set class index
    instances.setClassIndex(0);
    // add the instance
    instances.add(instance);
    instances.add(instance1);

    System.out.println(instances);
  }
}
