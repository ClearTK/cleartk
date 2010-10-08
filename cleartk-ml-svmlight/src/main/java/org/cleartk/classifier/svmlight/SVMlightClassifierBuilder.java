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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.jar.BuildJar;
import org.cleartk.classifier.jar.ClassifierBuilder;
import org.cleartk.classifier.sigmoid.Sigmoid;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class SVMlightClassifierBuilder implements ClassifierBuilder<Boolean> {

	static Logger logger = UIMAFramework.getLogger(SVMlightClassifierBuilder.class); 
		
	public static String COMMAND_ARGUMENT = "--executable";
	
	public static void train(String filePath, String[] args) throws Exception {
		String executable = "svm_learn";
		if(args.length > 0 && args[0].equals(COMMAND_ARGUMENT)) {
			executable = args[1]; 
			String[] tempArgs = new String[args.length - 2];
			System.arraycopy(args, 2, tempArgs, 0, tempArgs.length);
			args = tempArgs;
		}
		
		String[] command = new String[args.length + 3];
		command[0] = executable;
		System.arraycopy(args, 0, command, 1, args.length);
		command[command.length - 2] = new File(filePath).getPath();
		command[command.length - 1] = new File(filePath + ".model").getPath();
		
		logger.log(Level.INFO, "training with svmlight using the following command: "+toString(command));
		logger.log(Level.INFO, "if the svmlight learner does not seem to be working correctly, then try running the above command directly to see if e.g. svm_learn or svm_perf_learn gives a useful error message.");
		Process process = Runtime.getRuntime().exec(command);
		output(process.getInputStream(), System.out);
		output(process.getErrorStream(), System.err);
		process.waitFor();
	}

	private static String toString(String[] command) {
		StringBuilder sb = new StringBuilder();
		for(String cmmnd : command) {
			sb.append(cmmnd+" ");
		}
		return sb.toString();
	}
	
	public void train(File dir, String[] args) throws Exception {
		File trainingDataFile = new File(dir, "training-data.svmlight");
		train(trainingDataFile.getPath(), args);
		
		Sigmoid s = FitSigmoid.fit(new File(trainingDataFile.toString() + ".model"), trainingDataFile);
		System.out.println("Computed output mapping function: " + s.toString());
		
		ObjectOutput o = new ObjectOutputStream(new FileOutputStream(new File(trainingDataFile.toString() + ".sigmoid")));
		o.writeObject(s);
		o.close();
	}
	
	public void buildJar(File dir, String[] args) throws Exception {
		BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
		stream.write("model.svmlight", new File(dir, "training-data.svmlight.model"));
		stream.write("model.sigmoid", new File(dir, "training-data.svmlight.sigmoid"));
		stream.close();
	}

	public Class<? extends Classifier<Boolean>> getClassifierClass() {
		return SVMlightClassifier.class;
	}
	
	private static void output(InputStream input, PrintStream output) throws IOException {
		byte[] buffer = new byte[128];
		int count = input.read(buffer);
		while (count != -1) {
			output.write(buffer, 0, count);
			count = input.read(buffer);
		}
	}

}
