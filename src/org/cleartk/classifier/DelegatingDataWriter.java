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

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class DelegatingDataWriter extends DataWriter_ImplBase<Object,Object,Object> {
	
	public static final String PARAM_DATA_WRITER = "DataWriter";

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		String dataWriterName = (String)UIMAUtil.getRequiredConfigParameterValue(
				context, PARAM_DATA_WRITER);
		
		try {
			this.setDataWriter(Class.forName(dataWriterName));
		} catch (ClassNotFoundException e) {
			throw new ResourceInitializationException(e);
		} catch (InstantiationException e) {
			throw new ResourceInitializationException(e);
		} catch (IllegalAccessException e) {
			throw new ResourceInitializationException(e);
		}
		
		this.dataWriter.initialize(context);
		
//		super.initialize(context);

		Class<?> handlerLabelType = (Class<?>)ReflectionUtil.getTypeArgument(
				AnnotationHandler.class, "OUTCOME_TYPE", this.dataWriter.annotationHandler);
		Class<?> writerLabelType = (Class<?>)ReflectionUtil.getTypeArgument(
				InstanceConsumer.class, "OUTCOME_TYPE", this.dataWriter);

		if (!handlerLabelType.isAssignableFrom(writerLabelType)) {
			throw new RuntimeException(String.format(
					"%s data writer is incompatible with %s annotation handler",
					writerLabelType.getSimpleName(), handlerLabelType.getSimpleName()));
		}
	}

	public Object consume(Instance<Object> instance) {
		if( instance.getOutcome() != null )
			this.dataWriter.consume(instance);
		return null;
	}
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		this.dataWriter.process(jCas);
	}

	@Override
	public List<Object> consumeSequence(List<Instance<Object>> instances) {
		return this.dataWriter.consumeSequence(instances);
	}
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		this.dataWriter.collectionProcessComplete();
	}
	
	private void setDataWriter(Class<?> cls) throws InstantiationException, IllegalAccessException {
		this.dataWriter = ReflectionUtil.uncheckedCast(cls.newInstance());
	}
	
	private DataWriter_ImplBase<Object,Object,Object> dataWriter;

	@Override
	protected Class<? extends ClassifierBuilder<?>> getDefaultClassifierBuilderClass() {
		return this.dataWriter.getDefaultClassifierBuilderClass();
	}

	@Override
	protected Class<? extends EncoderFactory> getDefaultEncoderFactoryClass() {
		return this.dataWriter.getDefaultEncoderFactoryClass();
	}
	
	@Override
	protected Class<?> getMyTypeArgument(String parameterName) {
		return getTypeArgument(DataWriter_ImplBase.class, parameterName, this.dataWriter);
	}

}
