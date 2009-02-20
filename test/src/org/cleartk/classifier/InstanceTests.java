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
package org.cleartk.classifier;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class InstanceTests {
	
	@Test
	public void testFeatures() {
		Instance<String> instance = new Instance<String>();
	
		// test an instance with no features
		Assert.assertEquals(0, instance.getFeatures().size());
		
		// test the .add() method
		Feature f1 = new Feature("foo", "bar");
		instance.add(f1);
		Assert.assertEquals(1, instance.getFeatures().size());
		Assert.assertEquals(f1, instance.getFeatures().get(0));
		
		// test the .addAll() method
		Feature f2 = new Feature(true);
		Feature f3 = new Feature("baz", 42.0);
		List<Feature> newFeatures = new ArrayList<Feature>();
		newFeatures.add(f2);
		newFeatures.add(f3);
		instance.addAll(newFeatures);
		Assert.assertEquals(3, instance.getFeatures().size());
		Assert.assertEquals(f1, instance.getFeatures().get(0));
		Assert.assertEquals(f2, instance.getFeatures().get(1));
		Assert.assertEquals(f3, instance.getFeatures().get(2));
	}
	
	@Test
	public void testLabel() {
		Instance<Double> instance = new Instance<Double>();
		
		// test an instance with no label
		Assert.assertEquals(null, instance.getOutcome());
		
		// test setting and retrieving the label
		instance.setOutcome(3.14);
		Assert.assertEquals(3.14d, instance.getOutcome().doubleValue(), 0.01d);
		
	}

}
