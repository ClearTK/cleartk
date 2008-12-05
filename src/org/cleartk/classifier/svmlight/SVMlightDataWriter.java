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

import java.io.PrintWriter;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.factory.BinarySVMEncoderFactory;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 */
public class SVMlightDataWriter extends
DataWriter_ImplBase<Boolean,Boolean,FeatureVector> {

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// prepare logger
//		logger = Logger.getLogger("org.cleartk.classifier");

		// create the output writer
		this.outputWriter = this.getPrintWriter("training-data.svmlight");
	}

	public Boolean consume(Instance<Boolean> instance) {
		StringBuffer output = new StringBuffer();
		Boolean outcome = this.outcomeEncoder.encode(instance.getOutcome());
		
		if( outcome == null )
			return null;
			
		if( outcome.booleanValue() ) {
			output.append("+1");
		} else {
			output.append("-1");
		}

		FeatureVector featureVector = featuresEncoder.encodeAll(instance.getFeatures());

		for( FeatureVector.Entry entry : featureVector ) {
			if( entry.value == 0.0 )
				continue;

			output.append(" ");
			output.append(entry.index);
			output.append(":");
			output.append(entry.value);
		}

		outputWriter.println(output);

		// data writer, so no labels to return
		return null;
	}

	private PrintWriter outputWriter;

//	private Logger logger;

	@Override
	protected Class<? extends ClassifierBuilder<? extends Boolean>> getDefaultClassifierBuilderClass() {
		return SVMlightClassifierBuilder.class;
	}

	@Override
	protected Class<? extends EncoderFactory> getDefaultEncoderFactoryClass() {
		return BinarySVMEncoderFactory.class;
	}


}
