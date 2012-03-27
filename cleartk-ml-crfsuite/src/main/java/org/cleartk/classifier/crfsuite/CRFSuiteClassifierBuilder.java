/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.classifier.crfsuite;



import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.jar.JarStreams;
import org.cleartk.classifier.jar.SequenceClassifierBuilder_ImplBase;
/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Martin Riedl
 */

public class CRFSuiteClassifierBuilder
		extends

		SequenceClassifierBuilder_ImplBase<CRFSuiteClassifier, List<NameNumber>, String, String> {
	public final String MODEL_NAME = "crfsuite.model";
	public final String TRAINING_NAME = "crfsuite.training";

	static Logger logger = UIMAFramework
			.getLogger(CRFSuiteClassifierBuilder.class);

	@Override
	public File getTrainingDataFile(File dir) {
		return new File(dir, TRAINING_NAME);
	}

	@Override
	protected void packageClassifier(File dir, JarOutputStream modelStream)
			throws IOException {
		super.packageClassifier(dir, modelStream);
		JarStreams.putNextJarEntry(modelStream, MODEL_NAME, new File(dir,
				MODEL_NAME));
	}

	@Override
	public void trainClassifier(File dir, String... args) throws Exception {
		logger.log(Level.INFO,"Start learning CRFsuite classifier");
		CRFSuiteWrapper wrapper = new CRFSuiteWrapper();
		String model = new File(dir, MODEL_NAME).getPath();
		String trainingDataFile = getTrainingDataFile(dir).getPath();
		wrapper.trainClassifier(model, trainingDataFile, args);
		logger.log(Level.INFO,"Finished learning CRFsuite classifier");
	}

	private File modelFile=null;

	/**
	 * As the filename of the model is not known the only solution is to write
	 * the model back to a temporary file
	 */
	@Override
	protected void unpackageClassifier(JarInputStream modelStream)
			throws IOException {
		super.unpackageClassifier(modelStream);
		JarStreams.getNextJarEntry(modelStream, MODEL_NAME);
		this.modelFile = File.createTempFile("model", ".crfsuite");
		this.modelFile.deleteOnExit();
		logger.log(Level.FINE,"Start writing model to " + modelFile.getAbsolutePath());
		
		InputStream inputStream = new DataInputStream(modelStream);
		OutputStream out = new FileOutputStream(modelFile);
		byte buf[] = new byte[1024];
		int len;
		while ((len = inputStream.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		out.close();
		inputStream.close();
		logger.log(Level.FINE,"Model is written to " + modelFile.getAbsolutePath());

	}

	@Override
	protected CRFSuiteClassifier newClassifier() {
		return new CRFSuiteClassifier(this.featuresEncoder,
				this.outcomeEncoder, this.modelFile);
	}

}
