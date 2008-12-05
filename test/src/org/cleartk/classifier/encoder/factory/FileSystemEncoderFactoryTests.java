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
package org.cleartk.classifier.encoder.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.factory.BinarySVMEncoderFactory;
import org.cleartk.classifier.encoder.factory.FileSystemEncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.features.featurevector.DefaultFeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.EmptyAnnotator;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 *
 * @author Steven Bethard
 */
public class FileSystemEncoderFactoryTests {
	
	public final File outputDirectory = new File("test/data/encoder");
	
	@Before
	public void setUp() {
		if (!this.outputDirectory.exists()) {
			this.outputDirectory.mkdirs();
		}
	}
	
	@After
	public void tearDown() {
		TestsUtil.tearDown(this.outputDirectory);
	}
	
	@Test
	public void test() throws UIMAException, IOException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				EmptyAnnotator.class,
				TestsUtil.getTypeSystem("desc/TypeSystem.xml"),
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory.getPath());
		UimaContext context = engine.getUimaContext();
		
		EncoderFactory fileSystemFactory = new FileSystemEncoderFactory();
		Assert.assertEquals(null, fileSystemFactory.createFeaturesEncoder(context));
		
		// create and serialize an encoder in the output directory
		EncoderFactory vectorFactory = new BinarySVMEncoderFactory();
		FeaturesEncoder<?> featuresEncoder = vectorFactory.createFeaturesEncoder(context);
		OutcomeEncoder<?,?> outcomeEncoder = vectorFactory.createOutcomeEncoder(context);
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(
				this.outputDirectory, FeaturesEncoder_ImplBase.ENCODER_FILE_NAME)));
		os.writeObject(featuresEncoder);
		os.writeObject(outcomeEncoder);
		os.close();
		
		fileSystemFactory = new FileSystemEncoderFactory();
		TestsUtil.HideOutput hider = new TestsUtil.HideOutput();
		Assert.assertTrue(
				fileSystemFactory.createFeaturesEncoder(context)
				instanceof DefaultFeaturesEncoder);
		hider.restoreOutput();
	}

}
