package org.cleartk.classifier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeatureTest {

	@Test
	public void testFactoryMethod() {
		Feature feature1 = new Feature("asdf", 1);
		Feature feature2 = Feature.createFeature("qwerty", feature1);
		assertEquals("qwerty_asdf", feature2.getName());
		assertEquals(1, feature2.getValue());
		
	}
}
