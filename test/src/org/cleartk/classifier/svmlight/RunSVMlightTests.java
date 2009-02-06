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
package org.cleartk.classifier.svmlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.cleartk.classifier.ClassifierFactory;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer_ImplBase;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.SparseFeatureVector;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;



/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * 
 * @author Steven Bethard
*/
public class RunSVMlightTests {

	protected String outputDirectory = "test/data/svmlight/output";
	protected String dataDirectory = "test/data/svmlight";
	
	
	@After
	public void tearDown() {
		TestsUtil.emptyDirectory(new File(this.outputDirectory));
	}
	
	@Test
	public void testPath() throws Exception {
		String[] command = new String[] {
				"svm_learn"
		};
		
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.getOutputStream().write('\n');
			process.getOutputStream().write('\n');
			process.getOutputStream().close();
			slurp(process.getInputStream());
			slurp(process.getErrorStream());
			process.waitFor();
		} catch (IOException e) {
			throw e;
		}
	}
	
	@Test
	public void testLinearKernel() throws Exception {
		File dir = new File(dataDirectory, "linear");
		File trainingFile = new File(dir, "training-data.svmlight");
		File testFile = new File(dir, "test-data.svmlight");

		trainAndTest(trainingFile, testFile, new String[] {"-t", "0"}, "linear kernel");
	}

	@Test
	public void testPolynomialKernel() throws Exception {
		File dir = new File(dataDirectory, "nonlinear");
		File trainingFile = new File(dir, "training-data.svmlight");
		File testFile = new File(dir, "test-data.svmlight");

		trainAndTest(trainingFile, testFile, new String[] {"-t", "1", "-d", "2"}, "quadratic kernel");
		trainAndTest(trainingFile, testFile, new String[] {"-t", "1", "-d", "3"}, "cubic kernel");
	}
	
	@Test
	public void testRBFKernel() throws Exception {
		File dir = new File(dataDirectory, "nonlinear");
		File trainingFile = new File(dir, "training-data.svmlight");
		File testFile = new File(dir, "test-data.svmlight");

		trainAndTest(trainingFile, testFile, new String[] {"-t", "2"}, "RBF kernel");
	}
	
//	@Test
	public void testSigmoidKernel() throws Exception {
		File dir = new File(dataDirectory, "nonlinear");
		File trainingFile = new File(dir, "training-data.svmlight");
		File testFile = new File(dir, "test-data.svmlight");

		trainAndTest(trainingFile, testFile, new String[] {"-t", "3"}, "sigmoid kernel");
	}

	private void trainAndTest(File trainingFile, File testFile, String[] args, String name) throws IOException, InterruptedException {
		File modelFile = new File(this.outputDirectory, "model.svmlight");
		
		String[] command = new String[3 + args.length];
		command[0] = "svm_learn";
		for( int i=0; i<args.length; i++ )
			command[i+1] = args[i];
		command[command.length-2] = trainingFile.getPath();
		command[command.length-1] = modelFile.getPath();
		                              		                              
		Process process = Runtime.getRuntime().exec(command);
		slurp(process.getInputStream());
		output(process.getErrorStream(), System.err);
		process.waitFor();
		
		SVMlightModel model = SVMlightModel.fromFile(modelFile);
		BufferedReader r = new BufferedReader(new FileReader(testFile));
		float total = 0;
		float correct = 0;
		String line;
		while( (line = r.readLine()) != null ) {
			String[] fields = line.split(" ");
			
			boolean expectedResult = fields[0].equals("+1");
			
			FeatureVector fv = new SparseFeatureVector();
			for( int i=1; i<fields.length; i++ ) {
				String[] parts = fields[i].split(":");
				int featureIndex = Integer.valueOf(parts[0]);
				double featureValue = Double.valueOf(parts[1]);
				fv.set(featureIndex, featureValue);
			}
			
			boolean actualResult = model.evaluate(fv) > 0;

			total += 1;
			if( expectedResult == actualResult )
				correct += 1;
		}
		
		if( correct < (total * 0.95) )
			Assert.fail("model accuracy using " + name + " is below 95%");
	}
	
	private static void output(InputStream input, PrintStream output) throws IOException {
		byte[] buffer = new byte[128];
		int count = input.read(buffer);
		while (count != -1) {
			output.write(buffer, 0, count);
			count = input.read(buffer);
		}
	}
	
	private static void slurp(InputStream input) throws IOException {
		byte[] buffer = new byte[128];
		int count = input.read();
		while( count != -1 ) {
			count = input.read(buffer);
		}
	}

	@Test
	public void testSVMlight() throws Exception {
		
		// create the data writer
		SVMlightDataWriter dataWriter = new SVMlightDataWriter();
		dataWriter.initialize(TestsUtil.getUimaContext(
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory,
				InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER,
				TestsUtil.EmptyBooleanHandler.class.getName()));
		
		// add a bunch of instances
		dataWriter.consumeAll(TestsUtil.generateBooleanInstances(500));
		for (Instance<Boolean> instance: TestsUtil.generateBooleanInstances(500)) {
			dataWriter.consume(instance);
		}
		dataWriter.collectionProcessComplete();
		
		// check that the output file was written and is not empty
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.svmlight")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		TestsUtil.HideOutput hider = new TestsUtil.HideOutput();
		Train.main(new String[] {this.outputDirectory, "-c", "1.0"});
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		SVMlightClassifier classifier = (SVMlightClassifier)ClassifierFactory.readFromJar(
				new File(this.outputDirectory, "model.jar").getPath());
		for (Instance<Boolean> instance: TestsUtil.generateBooleanInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			Boolean outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}
	
	@Test
	public void testOVASVMlight() throws Exception {
		
		// create the data writer
		OVASVMlightDataWriter dataWriter = new OVASVMlightDataWriter();
		dataWriter.initialize(TestsUtil.getUimaContext(
				DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory,
				InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER,
				TestsUtil.EmptyStringHandler.class.getName()));
		
		// add a bunch of instances
		dataWriter.consumeAll(TestsUtil.generateStringInstances(500));
		for (Instance<String> instance: TestsUtil.generateStringInstances(500)) {
			dataWriter.consume(instance);
		}
		dataWriter.collectionProcessComplete();
		
		// check that the output files were written for each class
		for (String fileName: new String[]{
				"training-data-1.svmlight",
				"training-data-2.svmlight",
				"training-data-3.svmlight"}) {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					this.outputDirectory, fileName)));
			Assert.assertTrue(reader.readLine().length() > 0);
			reader.close();
		}
		
		// run the training command
		TestsUtil.HideOutput hider = new TestsUtil.HideOutput();
		Train.main(new String[] {this.outputDirectory, "-c", "0.01", "-t", "2"});
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		OVASVMlightClassifier classifier = (OVASVMlightClassifier)ClassifierFactory.readFromJar(
				new File(this.outputDirectory, "model.jar").getPath());
		for (Instance<String> instance: TestsUtil.generateStringInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			String outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}

}
