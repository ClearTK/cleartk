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
package org.cleartk.classifier.mallet;

import java.io.PrintWriter;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.factory.ContextValueEncoderFactory;
import org.cleartk.classifier.encoder.features.contextvalue.ContextValue;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * This training data consumer produces training data suitable for 
 * <a href="http://mallet.cs.umass.edu/index.php/SimpleTagger_example">
 * Mallet Conditional Random Field (CRF) tagger</a>.  
 * 
 * Each line of the training data contains a string representation of each feature followed
 * by the label/result for that instance.  
 * 
 * @author Philip Ogren
 */
public class MalletDataWriter extends DataWriter_ImplBase<String,String,List<ContextValue>> {

	protected PrintWriter trainingDataWriter;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		// initialize output writer and Classifier class
		this.trainingDataWriter = this.getPrintWriter("training-data.mallet");
	}

	public String consume(Instance<String> instance) {
		String outcomeString = this.outcomeEncoder.encode(instance.getOutcome());
		
		List<ContextValue> contextValues = this.featuresEncoder.encodeAll(instance.getFeatures());
		for (ContextValue contextValue : contextValues) {
			trainingDataWriter.print(contextValue.getContext() + ":" + contextValue.getValue()+" ");
		}
		this.trainingDataWriter.print(outcomeString);
		this.trainingDataWriter.println();
		
		// no labels created
		return null;
	}

	
	@Override
	protected Class<? extends ClassifierBuilder<? extends String>> getDefaultClassifierBuilderClass() {
		return MalletClassifierBuilder.class;
	}

	@Override
	protected Class<? extends EncoderFactory> getDefaultEncoderFactoryClass() {
		return ContextValueEncoderFactory.class;
	}
}
