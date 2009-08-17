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
package org.cleartk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */

public class ClassifierTestUtil {

	/**
	 * Create a number of random Instance objects that should be easy to
	 * classify. This is primarily useful for testing DataWriter and Classifier
	 * implementations.
	 * 
	 * @param n
	 *            The number of instances
	 * @return The list of newly-created instances
	 */
	public static List<Instance<String>> generateStringInstances(int n) {
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		for (int i = 0; i < n; i++) {
			Instance<String> instance = new Instance<String>();
			switch (ClassifierTestUtil.random.nextInt(3)) {
			case 0:
				instance.setOutcome("A");
				instance.add(new Feature("hello", -1050 + ClassifierTestUtil.random.nextInt(100)));
				break;
			case 1:
				instance.setOutcome("B");
				instance.add(new Feature("hello", -50 + ClassifierTestUtil.random.nextInt(100)));
				break;
			case 2:
				instance.setOutcome("C");
				instance.add(new Feature("hello", 950 + ClassifierTestUtil.random.nextInt(100)));
				break;
			}
			instances.add(instance);
		}
		return instances;
	}

	/**
	 * Create a number of random Instance objects that should be easy to
	 * classify. This is primarily useful for testing DataWriter and Classifier
	 * implementations.
	 * 
	 * @param n
	 *            The number of instances
	 * @return The list of newly-created instances
	 */
	public static List<Instance<Boolean>> generateBooleanInstances(int n) {
		List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
		for (int i = 0; i < n; i++) {
			Instance<Boolean> instance = new Instance<Boolean>();
			if (ClassifierTestUtil.random.nextInt(2) == 0) {
				instance.setOutcome(true);
				instance.add(new Feature("hello", ClassifierTestUtil.random.nextInt(1000) + 1000));
			}
			else {
				instance.setOutcome(false);
				instance.add(new Feature("hello", ClassifierTestUtil.random.nextInt(100)));
			}
			instances.add(instance);
		}
		return instances;
	}

	/**
	 * A random number generator for creating Instance objects
	 */
	private static final Random random = new Random(42);

}
