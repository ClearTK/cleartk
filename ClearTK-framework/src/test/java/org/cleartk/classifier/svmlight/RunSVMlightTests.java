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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarFile;

import org.cleartk.CleartkException;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.svmlight.model.SVMlightModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.SparseFeatureVector;
import org.cleartk.util.TestsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.UimaContextFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.TearDownUtil;



/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * 
 * @author Steven Bethard, Philipp Wetzler
*/
public class RunSVMlightTests {

	protected String outputDirectory = "test/data/svmlight/output";
	protected String dataDirectory = "test/data/svmlight";
	
	
	@After
	public void tearDown() {
		TearDownUtil.emptyDirectory(new File(this.outputDirectory));
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

	private void trainAndTest(File trainingFile, File testFile, String[] args, String name) throws IOException, InterruptedException, CleartkException {
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
		DataWriterAnnotator<Boolean> dataWriter = new DataWriterAnnotator<Boolean>();
		dataWriter.initialize(UimaContextFactory.createUimaContext(
				InstanceConsumer.PARAM_ANNOTATION_HANDLER,
				TestsUtil.EmptyBooleanHandler.class.getName(),
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS,
				DefaultSVMlightDataWriterFactory.class.getName()));
		
		// add a bunch of instances
		for (Instance<Boolean> instance: generateBooleanInstances(1000)) {
			dataWriter.consume(instance);
		}
		dataWriter.collectionProcessComplete();
		
		// check that the output file was written and is not empty
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.svmlight")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		HideOutput hider = new HideOutput();
		Train.main(this.outputDirectory, "-c", "1.0");
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		JarFile modelFile = new JarFile(new File(this.outputDirectory, "model.jar"));
		SVMlightClassifier classifier = new SVMlightClassifier(modelFile);
		for (Instance<Boolean> instance: generateBooleanInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			Boolean outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}
	
	@Test
	public void testOVASVMlight() throws Exception {
		
		// create the data writer
		DataWriterAnnotator<String> dataWriter = new DataWriterAnnotator<String>();
		dataWriter.initialize(UimaContextFactory.createUimaContext(
				InstanceConsumer.PARAM_ANNOTATION_HANDLER,
				TestsUtil.EmptyStringHandler.class.getName(),
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY,
				this.outputDirectory,
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS,
				DefaultOVASVMlightDataWriterFactory.class.getName()));
		
		// add a bunch of instances
		for (Instance<String> instance: generateStringInstances(1000)) {
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
		HideOutput hider = new HideOutput();
		Train.main(this.outputDirectory, "-c", "0.01", "-t", "1", "-d", "2");
		hider.restoreOutput();
		
		
		// read in the classifier and test it on new instances
		JarFile modelFile = new JarFile(new File(this.outputDirectory, "model.jar"));
		OVASVMlightClassifier classifier = new OVASVMlightClassifier(modelFile);
		for (Instance<String> instance: generateStringInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			String outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}
	
	private static List<Instance<Boolean>> generateBooleanInstances(int n) {
		Random random = new Random(42);
		List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
		for (int i = 0; i < n; i++) {
			Instance<Boolean> instance = new Instance<Boolean>();
			if (random.nextInt(2) == 0) {
				instance.setOutcome(true);
				instance.add(new Feature("hello", random.nextInt(100) + 1000));
				instance.add(new Feature("goodbye", 500));
			}
			else {
				instance.setOutcome(false);
				instance.add(new Feature("hello", random.nextInt(100)));
				instance.add(new Feature("goodbye", 500));
			}
			instances.add(instance);
		}
		return instances;
	}
	
	private static List<Instance<String>> generateStringInstances(int n) {
		Random random = new Random(42);
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		for (int i = 0; i < n; i++) {
			Instance<String> instance = new Instance<String>();
			int c = random.nextInt(3);
			if ( c == 0 ) {
				instance.setOutcome("A");
				instance.add(new Feature("hello", random.nextInt(100) + 950));
				instance.add(new Feature("goodbye", random.nextInt(100)));
				instance.add(new Feature("farewell", random.nextInt(100)));
			}
			else if( c == 1 ) {
				instance.setOutcome("B");
				instance.add(new Feature("hello", random.nextInt(100)));
				instance.add(new Feature("goodbye", random.nextInt(100) + 950));
				instance.add(new Feature("farewell", random.nextInt(100)));
			} else {
				instance.setOutcome("C");
				instance.add(new Feature("hello", random.nextInt(100)));
				instance.add(new Feature("goodbye", random.nextInt(100)));
				instance.add(new Feature("farewell", random.nextInt(100) + 950));
			}
			instances.add(instance);
		}
		return instances;
	}

}
