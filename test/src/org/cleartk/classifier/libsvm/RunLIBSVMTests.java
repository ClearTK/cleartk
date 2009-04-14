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
package org.cleartk.classifier.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
import java.util.jar.JarFile;

import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.Train;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.UimaContextFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.TearDownUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * 
 * @author Steven Bethard
*/
public class RunLIBSVMTests {

	protected Random random;
	protected String outputDirectory = "test/data/libsvm";
	
	@Before
	public void setUp() {
		random = new Random(System.currentTimeMillis());
	}
	
	@After
	public void tearDown() {
		TearDownUtil.emptyDirectory(new File(this.outputDirectory));
	}
	
	@Test
	public void testBinaryLIBSVM() throws Exception {
		
		// create the data writer
		DataWriterAnnotator<Boolean> dataWriter = new DataWriterAnnotator<Boolean>();
		dataWriter.initialize(UimaContextFactory.createUimaContext(
				InstanceConsumer.PARAM_ANNOTATION_HANDLER,
				TestsUtil.EmptyBooleanHandler.class.getName(),
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS,
				BinaryLIBSVMDataWriter.class.getName()));
		
		// add a bunch of instances
		dataWriter.consumeSequence(TestsUtil.generateBooleanInstances(500));
		for (Instance<Boolean> instance: TestsUtil.generateBooleanInstances(500)) {
			dataWriter.consume(instance);
		}
		dataWriter.collectionProcessComplete();
		
		// check that the output file was written and is not empty
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.libsvm")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		HideOutput hider = new HideOutput();
		Train.main(new String[] {this.outputDirectory, "-c", "1.0", "-s", "1"});
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		BinaryLIBSVMClassifier classifier = new BinaryLIBSVMClassifier(
				new JarFile(new File(this.outputDirectory, "model.jar")));
		for (Instance<Boolean> instance: TestsUtil.generateBooleanInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			Boolean outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}

	@Test
	public void testLIBLINEAR() throws Exception {
		
		// create the data writer
		DataWriterAnnotator<Boolean> dataWriter = new DataWriterAnnotator<Boolean>();
		dataWriter.initialize(UimaContextFactory.createUimaContext(
				InstanceConsumer.PARAM_ANNOTATION_HANDLER,
				TestsUtil.EmptyBooleanHandler.class.getName(),
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS,
				LIBLINEARDataWriter.class.getName()));
		
		// add a bunch of instances
		dataWriter.consumeSequence(TestsUtil.generateBooleanInstances(500));
		for (Instance<Boolean> instance: TestsUtil.generateBooleanInstances(500)) {
			dataWriter.consume(instance);
		}
		dataWriter.collectionProcessComplete();
		
		// check that the output file was written and is not empty
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.libsvm")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		HideOutput hider = new HideOutput();
		Train.main(new String[] {this.outputDirectory, "-c", "1.0", "-s", "1"});
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		LIBLINEARClassifier classifier = new LIBLINEARClassifier(
				new JarFile(new File(this.outputDirectory, "model.jar")));
		for (Instance<Boolean> instance: TestsUtil.generateBooleanInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			Boolean outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}

	@Test
	public void testMultiClassLIBSVM() throws Exception {
		
		// create the data writer
		DataWriterAnnotator<String> dataWriter = new DataWriterAnnotator<String>();
		dataWriter.initialize(UimaContextFactory.createUimaContext(
				InstanceConsumer.PARAM_ANNOTATION_HANDLER,
				TestsUtil.EmptyStringHandler.class.getName(),
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS,
				MultiClassLIBSVMDataWriter.class.getName()));
		
		// add a bunch of instances
		dataWriter.consumeSequence(TestsUtil.generateStringInstances(500));
		for (Instance<String> instance: TestsUtil.generateStringInstances(500)) {
			dataWriter.consume(instance);
		}
		dataWriter.collectionProcessComplete();
		
		// check that the output files were written for each class
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.libsvm")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		HideOutput hider = new HideOutput();
		Train.main(new String[] {this.outputDirectory, "-c", "1.0", "-t", "2"});
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		MultiClassLIBSVMClassifier classifier = new MultiClassLIBSVMClassifier(
				new JarFile(new File(this.outputDirectory, "model.jar")));
		for (Instance<String> instance: TestsUtil.generateStringInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			String outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}
}
