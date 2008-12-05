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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public abstract class InstanceConsumer_ImplBase<OUTCOME_TYPE> extends JCasAnnotator_ImplBase implements InstanceConsumer<OUTCOME_TYPE> {

	/**
	 * The name of a AnnotationHandler which will generate classification
	 * instances for each document.
	 */
	public static final String PARAM_ANNOTATION_HANDLER = "AnnotationHandler";

	protected AnnotationHandler<OUTCOME_TYPE> annotationHandler;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// get the class name from the parameter
		Object className = UIMAUtil.getRequiredConfigParameterValue(context,
				InstanceConsumer_ImplBase.PARAM_ANNOTATION_HANDLER);

		// create a new instance of the AnnotationHandler
		try {
			Class<?> cls = Class.forName((String) className);
			this.annotationHandler = this.getProducerClass(cls).newInstance();
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
		// TODO: do a runtime type check of AnnotationHandler<OUTCOME_TYPE> vs. InstanceConsumer<OUTCOME_TYPE>

		// initialize the AnnotationHandler
		this.annotationHandler.initialize(context);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		this.annotationHandler.process(jCas, this);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends AnnotationHandler<OUTCOME_TYPE>> getProducerClass(Class<?> cls) {
		return (Class<? extends AnnotationHandler<OUTCOME_TYPE>>) cls.asSubclass(AnnotationHandler.class);
	}
}
