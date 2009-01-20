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
package org.cleartk.classifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * ClassifierAnnotator objects are used to take the classification labels produced
 * by a Classifier model, and add these as annotations to new documents. In order
 * to create a new ClassifierAnnotator, you need:
 *   (1) A Classifier object stored in a jar file
 *   (2) A AnnotationHandler which defines a feature extraction routine and
 *       how the classification labels should be turned into annotations 
 *   
 * For each document, AnnotationHandler.produce(JCas, InstanceConsumer)
 * is called, passing the document as a JCas and this object as the consumer. The
 * AnnotationHandler should then extract lists of features and pass them
 * back to the ClassifierAnnotator as ClassifierInstance objects, using one of:
 *   InstanceConsumer.consume(ClassifierInstance)
 *   InstanceConsumer.consumeAll(List)
 * The consumer (this class) will then pass the features on to the classifier,
 * collect the classification labels, and then return those labels to the producer.
 * The AnnotationHandler should then add those labels as annotations to the
 * document in whatever way it sees fit.
 * 
 * @see org.cleartk.classifier.Classifier
 * @see org.cleartk.classifier.AnnotationHandler
 * @see org.cleartk.example.ExamplePOSAnnotationHandler
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public class ClassifierAnnotator<OUTCOME_TYPE> extends InstanceConsumer_ImplBase<OUTCOME_TYPE> {

	/**
	 * The path to a jar file used to instantiate the classifier.
	 */
	public static final String PARAM_CLASSIFIER_JAR = "ClassifierJar";

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// get the Classifier jar file path and load the Classifier
		String jarPath = (String)UIMAUtil.getRequiredConfigParameterValue(
				context, ClassifierAnnotator.PARAM_CLASSIFIER_JAR);
		try {
			Classifier<?> untypedClassifier = ClassifierFactory.readFromJar(jarPath);
			Type classifierLabelType = ReflectionUtil.getTypeArgument(
					Classifier.class, "OUTCOME_TYPE", untypedClassifier);
			Type annotationHandlerLabelType = ReflectionUtil.getTypeArgument(
					AnnotationHandler.class, "OUTCOME_TYPE", annotationHandler);

			if (!ReflectionUtil.isAssignableFrom(annotationHandlerLabelType, classifierLabelType)) {
				throw new RuntimeException(String.format(
						"%s classifier is incompatible with %s annotation handler",
						classifierLabelType, annotationHandlerLabelType));
			}
			
			this.classifier = (Classifier<OUTCOME_TYPE>)untypedClassifier;
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public OUTCOME_TYPE consume(Instance<OUTCOME_TYPE> instance) {
		
		// sequential classifiers cannot handle a single instance at a time
		if (this.classifier.isSequential()) {
			String message = "Sequential classifiers cannot consume a single instance";
			throw new UnsupportedOperationException(message);
		}
		
		// non-sequential classifiers classify a single instance as usual
		else {
			return this.classifier.classify(instance.getFeatures());
		}
	}
	
	public List<OUTCOME_TYPE> consumeAll(List<Instance<OUTCOME_TYPE>> instances) {

		// sequential classifiers classify all instances in a sequence at once
		if (this.classifier.isSequential()) {
			List<List<Feature>> instanceFeatures = new ArrayList<List<Feature>>();
			for (Instance<OUTCOME_TYPE> instance: instances) {
				instanceFeatures.add(instance.getFeatures());
			}
			return this.classifier.classifySequence(instanceFeatures);
		}
		
		// non-sequential classifiers classify each instance individually
		else {
			List<OUTCOME_TYPE> labels = new ArrayList<OUTCOME_TYPE>();
			for (Instance<OUTCOME_TYPE> instance: instances) {
				labels.add(this.classifier.classify(instance.getFeatures()));
			}
			return labels;
		}
	}

	private Classifier<OUTCOME_TYPE> classifier;

	public boolean expectsOutcomes() {
		return false;
	}
	
	public boolean isSequential() {
		return classifier.isSequential();
	}

}
