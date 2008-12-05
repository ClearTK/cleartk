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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.FeaturesEncoder_ImplBase;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard, Philipp Wetzler
 */
public abstract class DataWriter_ImplBase<INPUTOUTCOME_TYPE, OUTPUTOUTCOME_TYPE,FEATURES_TYPE> extends
InstanceConsumer_ImplBase<INPUTOUTCOME_TYPE> {

	protected String outputDir;
	protected List<PrintWriter> writers;
	protected ClassifierManifest classifierManifest;

	protected FeaturesEncoder<FEATURES_TYPE> featuresEncoder;
	protected OutcomeEncoder<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE> outcomeEncoder;

	/**
	 * The name of the directory where the training data will be written.
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "OutputDirectory";

	/**
	 * The name of the class used to initialize the feature encoding
	 */
	public static final String PARAM_ENCODER_FACTORY_CLASS = "EncoderFactoryClass";

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// Initialize the output directory and list of output writers
		this.writers = new ArrayList<PrintWriter>();
		this.outputDir = (String)UIMAUtil.getRequiredConfigParameterValue(
				context, DataWriter_ImplBase.PARAM_OUTPUT_DIRECTORY);

		// Initialize the Manifest
		this.classifierManifest = new ClassifierManifest();

		/*
		 * Initialize Encoders:
		 */
		String factoryClassName = (String)context.getConfigParameterValue(
				DataWriter_ImplBase.PARAM_ENCODER_FACTORY_CLASS);
		if (factoryClassName != null) {
			try {
				Class<?> factoryClass = Class.forName(factoryClassName);
				EncoderFactory factory = (EncoderFactory) factoryClass.newInstance();
				this.featuresEncoder = this.getFeaturesEncoder(factory, context);
				this.outcomeEncoder = this.getOutcomeEncoder(factory, context);
			} catch (ClassNotFoundException e) {
				throw new ResourceInitializationException(e);
			} catch (InstantiationException e) {
				throw new ResourceInitializationException(e);
			} catch (IllegalAccessException e) {
				throw new ResourceInitializationException(e);
			}
		}

		// use default factory if none given or if one of the encoders is still null
		if( this.featuresEncoder == null || this.outcomeEncoder == null ) {
			try {
				EncoderFactory defaultFactory = this.getDefaultEncoderFactoryClass().newInstance();
				
				if( this.featuresEncoder == null )
					this.featuresEncoder = this.getFeaturesEncoder(defaultFactory, context);
				
				if( this.outcomeEncoder == null )
					this.outcomeEncoder = this.getOutcomeEncoder(defaultFactory, context);
			} catch (InstantiationException e) {
				throw new ResourceInitializationException(e);
			} catch (IllegalAccessException e) {
				throw new ResourceInitializationException(e);
			}
		}
	}

	public File getFile(String fileName) {
		return new File(this.outputDir, fileName);
	}

	public PrintWriter getPrintWriter(String fileName)
	throws ResourceInitializationException {
		File file = this.getFile(fileName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try {
			PrintWriter writer = new PrintWriter(
					new BufferedWriter(new FileWriter(file)));
			this.writers.add(writer);
			return writer;
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		// close out the file writers
		for (PrintWriter writer: this.writers) {
			writer.flush();
			writer.close();
		}

		// serialize the features encoder
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(getFile(
					FeaturesEncoder_ImplBase.ENCODER_FILE_NAME)));
			os.writeObject(this.featuresEncoder);
			os.writeObject(this.outcomeEncoder);
			os.close();
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		
		// set manifest values
		try {
			Class<? extends ClassifierBuilder<? extends INPUTOUTCOME_TYPE>> classifierBuilderClass =
				this.getDefaultClassifierBuilderClass();
			this.classifierManifest.setClassifierBuilder(classifierBuilderClass.newInstance());
		} catch (InstantiationException e) {
			throw new AnalysisEngineProcessException(e);
		} catch (IllegalAccessException e) {
			throw new AnalysisEngineProcessException(e);
		}

		// write the manifest file
		try {
			classifierManifest.write(new File(this.outputDir));
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	public List<INPUTOUTCOME_TYPE> consumeAll(List<Instance<INPUTOUTCOME_TYPE>> instances) {
		for (Instance<INPUTOUTCOME_TYPE> instance: instances) {
			this.consume(instance);
		}
		return null;
	}

	protected abstract Class<? extends EncoderFactory> getDefaultEncoderFactoryClass();

	protected abstract Class<? extends ClassifierBuilder<? extends INPUTOUTCOME_TYPE>> getDefaultClassifierBuilderClass();

	protected Class<?> getMyTypeArgument(String parameterName) {
		return getTypeArgument(DataWriter_ImplBase.class, parameterName, this);
	}
	
	protected  Class<?> getTypeArgument(Class<?> cls, String parameterName, Object instance) {
		Map<String,Type> typeArguments = ReflectionUtil.getTypeArguments(cls, instance);
		Type t = typeArguments.get(parameterName);
		if( t instanceof Class )
			return (Class<?>)t;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	private FeaturesEncoder<FEATURES_TYPE> getFeaturesEncoder(EncoderFactory factory, UimaContext context)
	throws ResourceInitializationException {
		FeaturesEncoder<?> genericEncoder = factory.createFeaturesEncoder(context);
		if( genericEncoder == null )
			return null;

		Class<?> myFEATURESTYPE = this.getMyTypeArgument("FEATURES_TYPE");
		Class<?> encoderFEATURESTYPE = this.getTypeArgument(FeaturesEncoder.class, "FEATURES_TYPE", genericEncoder);

		if( myFEATURESTYPE != encoderFEATURESTYPE )
			throw new ClassCastException();

		return (FeaturesEncoder<FEATURES_TYPE>) genericEncoder;
	}
	
	@SuppressWarnings({ "unchecked" })
	private OutcomeEncoder<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE> getOutcomeEncoder(EncoderFactory factory, UimaContext context) 
	throws ResourceInitializationException {
		OutcomeEncoder<?,?> genericEncoder = factory.createOutcomeEncoder(context);
		if( genericEncoder == null )
			return null;
		
		Class<?> myINPUTOUTCOME_TYPE = this.getMyTypeArgument("INPUTOUTCOME_TYPE");
		Class<?> myOUTPUTOUTCOME_TYPE = this.getMyTypeArgument("OUTPUTOUTCOME_TYPE");
		
		Class<?> encoderINPUTOUTCOME_TYPE = this.getTypeArgument(OutcomeEncoder.class, "INPUTOUTCOME_TYPE", genericEncoder);
		Class<?> encoderOUTPUTOUTCOME_TYPE = this.getTypeArgument(OutcomeEncoder.class, "OUTPUTOUTCOME_TYPE", genericEncoder);
		
		if( myINPUTOUTCOME_TYPE != encoderINPUTOUTCOME_TYPE )
			throw new ClassCastException();

		if( myOUTPUTOUTCOME_TYPE != encoderOUTPUTOUTCOME_TYPE )
			throw new ClassCastException();
		
		return (OutcomeEncoder<INPUTOUTCOME_TYPE,OUTPUTOUTCOME_TYPE>) genericEncoder;
}
	
	public boolean expectsOutcomes() {
		return true;
	}
}
