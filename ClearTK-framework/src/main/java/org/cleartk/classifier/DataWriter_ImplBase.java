/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.cleartk.CleartkException;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 */
public abstract class DataWriter_ImplBase<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE, FEATURES_TYPE> implements DataWriter<INPUTOUTCOME_TYPE> {

	public DataWriter_ImplBase(File outputDirectory) {
		// Initialize the output directory and list of output writers
		this.outputDirectory = outputDirectory;
		this.writers = new ArrayList<PrintWriter>();

		// Initialize the Manifest
		this.classifierManifest = new ClassifierManifest();
	}

	public void write(Instance<INPUTOUTCOME_TYPE> instance) throws CleartkException {
		FEATURES_TYPE features = featuresEncoder.encodeAll(instance.getFeatures());
		OUTPUTOUTCOME_TYPE outcome = outcomeEncoder.encode(instance.getOutcome());
		writeEncoded(features, outcome);
	}

	public abstract void writeEncoded(FEATURES_TYPE features, OUTPUTOUTCOME_TYPE outcome) throws CleartkException;

	public void finish() throws CleartkException {
		try {
			// close out the file writers
			for (PrintWriter writer : this.writers) {
				writer.flush();
				writer.close();
			}
			
			// finalize the features encoder feature set
			featuresEncoder.finalizeFeatureSet(outputDirectory);
	
			// serialize the features encoder
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
					getFile(FeaturesEncoder_ImplBase.ENCODERS_FILE_NAME)));
			os.writeObject(this.featuresEncoder);
			os.writeObject(this.outcomeEncoder);
			os.close();
	
			// set manifest values
			try {
				Class<? extends ClassifierBuilder<? extends INPUTOUTCOME_TYPE>> classifierBuilderClass = this
						.getDefaultClassifierBuilderClass();
				this.classifierManifest.setClassifierBuilder(classifierBuilderClass.newInstance());
			}
			catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
	
			// write the manifest file
			classifierManifest.write(this.outputDirectory);
		} catch(IOException ioe) {
			throw new CleartkException(ioe);
		}
	}

	public void setFeaturesEncoder(FeaturesEncoder<FEATURES_TYPE> featuresEncoder) {
		this.featuresEncoder = featuresEncoder;
	}

	public void setOutcomeEncoder(OutcomeEncoder<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE> outcomeEncoder) {
		this.outcomeEncoder = outcomeEncoder;
	}

	public ClassifierManifest getClassifierManifest() {
		return classifierManifest;
	}

	protected File getFile(String fileName) {
		return new File(this.outputDirectory, fileName);
	}

	protected PrintWriter getPrintWriter(String fileName) throws IOException {
		File file = this.getFile(fileName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		this.writers.add(writer);
		return writer;
	}

	protected File outputDirectory;
	private List<PrintWriter> writers;
	protected ClassifierManifest classifierManifest;
	protected FeaturesEncoder<FEATURES_TYPE> featuresEncoder;
	protected OutcomeEncoder<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE> outcomeEncoder;
}
