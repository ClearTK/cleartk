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
package org.cleartk.classifier.jar;

import java.io.IOException;
import java.util.jar.JarFile;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.Initializable;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ClassifierFactory;
import org.cleartk.classifier.SequentialClassifier;
import org.cleartk.classifier.SequentialClassifierFactory;
import org.cleartk.test.util.ConfigurationParameterNameFactory;
import org.cleartk.util.ReflectionUtil;
import org.uutuc.descriptor.ConfigurationParameter;
import org.uutuc.util.InitializeUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class JarClassifierFactory<OUTCOME_TYPE> implements ClassifierFactory<OUTCOME_TYPE>, SequentialClassifierFactory<OUTCOME_TYPE>, Initializable {

	public static final String PARAM_CLASSIFIER_JAR_PATH = ConfigurationParameterNameFactory
			.createConfigurationParameterName(JarClassifierFactory.class, "classifierJarPath");

	@ConfigurationParameter(mandatory = true, description = "provides the path to the jar file that should be used to instantiate the classifier.")
	private String classifierJarPath;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		InitializeUtil.initialize(this, context);
	}

	public Classifier<OUTCOME_TYPE> createClassifier() throws IOException, CleartkException {
		return createClassifierFromJar(classifierJarPath);
	}

	public SequentialClassifier<OUTCOME_TYPE> createSequentialClassifier() throws IOException, CleartkException {
		return createSequentialClassifierFromJar(classifierJarPath);
	}

	
	/**
	 * Every classifier should be able to instantiated from a jar file. This
	 * factory method reads the class of the classifier from manifest of the jar
	 * file from the attribute "classifier". e.g.:
	 * <p>
	 * <code>classifier: org.cleartk.classifier.OpenNLPMaxentClassifier</code>
	 * </p>
	 * The value of the classifier attribute of the manifest is used to
	 * instantiate a classifier using reflection and the constructor that takes
	 * a jar file.
	 * 
	 * @param jarFileName
	 *            the name of a jar file
	 * @return a classifier defined by the contents of the jar file.
	 * @throws IOException
	 */
	public Classifier<OUTCOME_TYPE> createClassifierFromJar(String jarFileName) throws IOException {
		return ReflectionUtil.uncheckedCast(createClassifierFromJar(jarFileName, Classifier.class));
	}

	public SequentialClassifier<OUTCOME_TYPE> createSequentialClassifierFromJar(String jarFileName) throws IOException {
		return ReflectionUtil.uncheckedCast(createClassifierFromJar(jarFileName, SequentialClassifier.class));
	}

	public static <CLASSIFIER_TYPE> CLASSIFIER_TYPE createClassifierFromJar(String jarFileName, Class<CLASSIFIER_TYPE> cls) throws IOException {
		// get the jar file manifest
		JarFile modelFile = new JarFile(jarFileName);
		ClassifierManifest manifest = new ClassifierManifest(modelFile);

		// get the classifier class
		ClassifierBuilder<?> builder = manifest.getClassifierBuilder();
		Class<? extends CLASSIFIER_TYPE> classifierClass = builder.getClassifierClass().asSubclass(cls);

		// create the classifier, passing in the jar file
		try {
			return classifierClass.getConstructor(JarFile.class).newInstance(modelFile);
		}
		catch (Exception e) {
			IOException exception = new IOException();
			exception.initCause(e);
			throw exception;
		}
		finally {
			modelFile.close();
		}
	}

	

}
