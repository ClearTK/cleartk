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
package org.cleartk.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.opennlp.MaxentDataWriter;
import org.cleartk.example.ExamplePOSAnnotationHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philip Ogren
 */
public class ReflectionUtilTests {
	
	public static class TestSuperClass<T> {
	}
	public static class TestSubClass extends TestSuperClass<String> {
	}
	public static class TestArraySubClass extends TestSuperClass<double[]> {
	}

	@Test
	public void testGetTypeArgument() throws Exception {

		Type type = ReflectionUtil.getTypeArgument(TestSuperClass.class, "T", new TestSubClass());
		Assert.assertEquals(String.class, type);

		type = ReflectionUtil.getTypeArgument(TestSuperClass.class, "T", new TestSuperClass<String>());
		Assert.assertNull(type);
		
		type = ReflectionUtil.getTypeArgument(TestSuperClass.class, "T", new TestArraySubClass());
		Assert.assertTrue(type instanceof GenericArrayType);
		Assert.assertEquals(double.class, ((GenericArrayType)type).getGenericComponentType());

		type = ReflectionUtil.getTypeArgument(AnnotationHandler.class, "OUTCOME_TYPE", new ExamplePOSAnnotationHandler());
		Assert.assertEquals(String.class, type);

		type = ReflectionUtil.getTypeArgument(InstanceConsumer.class, "OUTCOME_TYPE", new MaxentDataWriter());
		Assert.assertEquals(String.class, type);
		
		type = ReflectionUtil.getTypeArgument(DataWriter_ImplBase.class, "INPUTOUTCOME_TYPE", new MaxentDataWriter());
		Assert.assertEquals(String.class, type);
	}
}
