package org.cleartk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;

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
