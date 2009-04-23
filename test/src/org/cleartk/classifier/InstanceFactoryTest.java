package org.cleartk.classifier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
		
		instance = InstanceFactory.createInstance("O", "Word_Three LCWord_three CapitalType_INITIAL_UPPERCASE L0OOB1 L1OOB2 R0_sequence R0_TypePath_Pos_NN R0_TypePath_Stem_sequenc R1_elements R1_TypePath_Pos_NNS R1_TypePath_Stem_element TypePath_Pos_CD TypePath_Stem_Three PrevNEMTokenLabel_L0OOB1 PrevNEMTokenLabel_L1OOB2");
		assertEquals("O", instance.getOutcome());
		assertEquals(15, instance.getFeatures().size());
		assertEquals("Word_Three", instance.getFeatures().get(0).getName());
	}
}
