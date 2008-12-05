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
package org.cleartk.classifier.opennlp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import opennlp.maxent.RealValueFileEventStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter_ImplBase;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.factory.ContextValueEncoderFactory;
import org.cleartk.classifier.encoder.features.contextvalue.ContextValue;
import org.cleartk.classifier.encoder.features.contextvalue.ContextValueFeaturesEncoder;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * This training data consumer produces training data suitable for <a
 * href="http://maxent.sourceforge.net/"> OpenNLP Maxent package</a>.
 * 
 * Each line of the training data contains a label/result for the instance
 * followed by a string representation of each feature. Models trained with data
 * generated by this class should use RealValueFileEventStream. For relevant
 * discussion, please see:
 * 
 * https://sourceforge.net/forum/forum.php?thread_id=1925312&forum_id=18385
 * 
 * 
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 * @see RealValueFileEventStream
 */
public class MaxentDataWriter extends DataWriter_ImplBase<String, String, List<ContextValue>> {

	public static final String FEATURE_LOOKUP_FILE_NAME = "feature-lookup.txt";

	protected PrintWriter trainingDataWriter;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// initialize output writer and Classifier class
		this.trainingDataWriter = this.getPrintWriter("training-data.maxent");
	}

	public String consume(Instance<String> instance) {
		// write the label
		String outcomeString = this.outcomeEncoder.encode(instance.getOutcome());
		this.trainingDataWriter.print(outcomeString);

		// aggregate the features
		List<ContextValue> features = this.featuresEncoder.encodeAll(instance.getFeatures());

		// write each of the string features, encoded, into the training data
		for (ContextValue contextValue : features) {
			this.trainingDataWriter.print(' ');
			if(contextValue.getValue() == 1)
				trainingDataWriter.print(contextValue.getContext());
			else
				trainingDataWriter.print(contextValue.getContext() + "=" + contextValue.getValue());

		}

		// complete the feature line
		this.trainingDataWriter.println();

		// no labels created, so return null
		return null;
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		if (featuresEncoder instanceof ContextValueFeaturesEncoder) {
			try {
				ContextValueFeaturesEncoder cvfe = (ContextValueFeaturesEncoder) featuresEncoder;
				if(cvfe.isCompressFeatures())
					cvfe.writeKeys(this.getPrintWriter(FEATURE_LOOKUP_FILE_NAME));
			}
			catch (ResourceInitializationException e) {
				throw new AnalysisEngineProcessException(e);
			}
			catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
		
		super.collectionProcessComplete();
	}

	@Override
	protected Class<? extends ClassifierBuilder<? extends String>> getDefaultClassifierBuilderClass() {
		return MaxentClassifierBuilder.class;
	}

	@Override
	protected Class<? extends EncoderFactory> getDefaultEncoderFactoryClass() {
		return ContextValueEncoderFactory.class;
	}
}
