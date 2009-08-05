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

import java.io.IOException;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
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
 * @see org.cleartk.example.pos.ExamplePOSAnnotationHandler
 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public class ClassifierAnnotator<OUTCOME_TYPE> extends InstanceConsumer_ImplBase<OUTCOME_TYPE> {

	/**
	 * "org.cleartk.classifier.ClassifierAnnotator.PARAM_CLASSIFIER_JAR" is a single, required, string parameter that provides the filename of the classifier file (e.g. "model.jar")
	 */
	public static final String PARAM_CLASSIFIER_JAR = "org.cleartk.classifier.ClassifierAnnotator.PARAM_CLASSIFIER_JAR";

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		// get the Classifier jar file path and load the Classifier
		String jarPath = (String)UIMAUtil.getRequiredConfigParameterValue(
				context, PARAM_CLASSIFIER_JAR);
		Classifier<?> untypedClassifier;
		try {
			untypedClassifier = ClassifierFactory.createClassifierFromJar(jarPath);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		
		// check that the Classifier matches the AnnotationHandler type
		this.checkOutcomeType(Classifier.class, "OUTCOME_TYPE", untypedClassifier);
		this.classifier = ReflectionUtil.uncheckedCast(untypedClassifier);
		UIMAUtil.initialize(this.classifier, context);
	}

	public OUTCOME_TYPE consume(Instance<OUTCOME_TYPE> instance) throws CleartkException {
		return this.classifier.classify(instance.getFeatures());
	}
	
	private Classifier<OUTCOME_TYPE> classifier;

	public boolean expectsOutcomes() {
		return false;
	}
	
}
