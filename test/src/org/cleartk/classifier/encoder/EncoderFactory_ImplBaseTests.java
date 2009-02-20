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
package org.cleartk.classifier.encoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.apache.uima.UimaContext;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.encoder.factory.BinarySVMEncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.features.featurevector.DefaultFeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.BooleanToBooleanOutcomeEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.UimaContextFactory;
import org.uutuc.util.TearDownUtil;



/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 *
 * @author Steven Bethard
 */
public class EncoderFactory_ImplBaseTests {
	
	public final File outputDirectory = new File("test/data/encoder");
	
	@Before
	public void setUp() {
		if (!this.outputDirectory.exists()) {
			this.outputDirectory.mkdirs();
		}
	}
	
	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(this.outputDirectory);
	}
	
	@Test
	public void test() throws Exception {
		String outputDir = this.outputDirectory.getPath();
		EncoderFactory fileSystemFactory = new EncoderFactory_ImplBase() {};
		UimaContext context;
		
		// try to get an encoder with an empty output directory
		context = UimaContextFactory.createUimaContext(
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY, outputDir);
		Assert.assertNull(fileSystemFactory.createFeaturesEncoder(context));
		
		// create and serialize an encoder in the output directory
		EncoderFactory vectorFactory = new BinarySVMEncoderFactory();
		FeaturesEncoder<?> featuresEncoder = vectorFactory.createFeaturesEncoder(context);
		OutcomeEncoder<?,?> outcomeEncoder = vectorFactory.createOutcomeEncoder(context);
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(
				this.outputDirectory, FeaturesEncoder_ImplBase.ENCODER_FILE_NAME)));
		os.writeObject(featuresEncoder);
		os.writeObject(outcomeEncoder);
		os.close();
		
		// try to get an encoder without specifying the parameter
		context = UimaContextFactory.createUimaContext(
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY, outputDir);
		Assert.assertNull(fileSystemFactory.createFeaturesEncoder(context));

		// try specifying both true and false for the loading parameter
		this.testParameter(fileSystemFactory, outputDir);
		
		// do it a second time to make sure the context is paid attention to
		this.testParameter(fileSystemFactory, outputDir);
	}
	
	public void testParameter(EncoderFactory fileSystemFactory, String outputDir) throws Exception {
		UimaContext context;
		
		// try specifying no encoder
		context = UimaContextFactory.createUimaContext(
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY, outputDir,
				EncoderFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM, false);
		Assert.assertNull(fileSystemFactory.createFeaturesEncoder(context));
		
		// make sure the encoder is loaded when requested
		context = UimaContextFactory.createUimaContext(
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY, outputDir,
				EncoderFactory_ImplBase.PARAM_LOAD_ENCODERS_FROM_FILE_SYSTEM, true);
		Assert.assertTrue(
				fileSystemFactory.createFeaturesEncoder(context)
				instanceof DefaultFeaturesEncoder);
		Assert.assertTrue(
				fileSystemFactory.createOutcomeEncoder(context)
				instanceof BooleanToBooleanOutcomeEncoder);
	}

}
