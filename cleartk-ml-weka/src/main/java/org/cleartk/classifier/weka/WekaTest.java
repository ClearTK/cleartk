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
package org.cleartk.classifier.weka;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */

public class WekaTest {

	public static void main(String[] args) {
		 // Declare the class attribute along with its values
		 FastVector fvClassVal = new FastVector(2);
		 fvClassVal.addElement("positive");
		 fvClassVal.addElement("negative");
		 
		 Attribute classAttribute = new Attribute("theClass", fvClassVal, 0);

		Attribute attribute1 = new Attribute("firstNumeric", 1);
		Attribute attribute2 = new Attribute("secondNumeric", 2);
		 
		 // Declare a nominal attribute along with its values
		 FastVector fvNominalVal = new FastVector(3);
		 fvNominalVal.addElement("blue");
		 fvNominalVal.addElement("gray");
		 fvNominalVal.addElement("black");
		 Attribute attribute3 = new Attribute("aNominal", fvNominalVal, 3);
		 
		 
		 // Declare the feature vector
		 FastVector attributes = new FastVector(4);
		 attributes.addElement(classAttribute);
		 attributes.addElement(attribute1);    

		 Instance instance = new Instance(4);
		 instance.setValue((Attribute)attributes.elementAt(0), "positive");
		 instance.setValue((Attribute)attributes.elementAt(1), 1.0);      

		 
		 attributes.addElement(attribute2);    
		 attributes.addElement(attribute3);    

		 Instance instance1 = new Instance(4);
		 instance1.setValue((Attribute)attributes.elementAt(0), "positive");
		 instance1.setValue((Attribute)attributes.elementAt(1), 1.0);      
		 instance1.setValue((Attribute)attributes.elementAt(2), 0.5);      
		 instance1.setValue((Attribute)attributes.elementAt(3), "gray");

		 Instances instances = new Instances("Rel", attributes, 10);           


		 // Set class index
		 instances.setClassIndex(0);
		 // add the instance
		 instances.add(instance);
		 instances.add(instance1);
		 
		 System.out.println(instances);
	}
}
