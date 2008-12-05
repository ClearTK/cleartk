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
/*
 * This file was copied from the Apache UIMA examples source code base from
 * org.apache.uima.examples.cpe.XCasWriterCasConsumer and modified.  The apache
 * licence that applies to the original work is provided in the next comment.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.cleartk.util.linewriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * This writer provides a way to write out annotations one-per-line to a plain
 * text file in a variety of ways configurable at run time.
 * 
 * <p>This class has no relation to LineReader - i.e. LineWriter does not provide "reverse functionality" of LineReader.
 * 
 * <p>If you mistook this class for a line rider, then please redirect to the
 * completely unrelated, but totally awesome Line Rider at: http://linerider.com
 * 
 * @author Philip Ogren
 */

public class LineWriter<T extends Annotation> extends JCasAnnotator_ImplBase {

	/**
	 * "OutputDirectory" is a single, optional, string parameter that takes a
	 * path to directory into which output files will be written. If no value is
	 * given for this parameter, then the parameter "OutputFile" is required. If
	 * a value is given, then one file for each document will be created in the
	 * output directory provided. If a value for both "OutputDirectory" and
	 * "OutputFile" is given, then an exception will be thrown.
	 */
	public static final String PARAM_OUTPUT_DIRECTORY = "OutputDirectory";

	/**
	 * "OutputFile" is a single, optional, string parameter that takes a file
	 * name to write results to. If no value is given for this parameter, then
	 * the parameter "OutputDirectory" is required. If a value is given, then
	 * one file for all documents will be created in the output directory
	 * provided. If a value for both "OutputDirectory" and "OutputFile" is
	 * given, then an exception will be thrown.
	 */

	public static final String PARAM_OUTPUT_FILE = "OutputFile";

	/**
	 * "OutputAnnotationClass" is a single, required, string parameter that
	 * takes the name of the annotation class of the annotations that are to be
	 * written out. The annotation class must be a subclass of
	 * org.apache.uima.jcas.tcas.Annotation.
	 * 
	 * @see org.apache.uima.jcas.tcas.DocumentAnnotation
	 */
	public static final String PARAM_OUTPUT_ANNOTATION_CLASS = "OutputAnnotationClass";

	/**
	 * "BlockAnnotationClass" is a single, optional, string parameter that takes
	 * the name of an annotation class that determines a "block" of lines in the
	 * resulting output file(s). Each "block" of lines is separated by an extra
	 * newline. If, for example, the value of "OutputAnnotationClass" is
	 * "org.cleartk.type.Token" and the value for
	 * "BlockAnnotationClass" is "org.cleartk.type.Sentence", then the
	 * tokens in each sentence will be written out one per line with a blank
	 * line between the last token of a sentence and the first token of the
	 * following sentence. Note that setting this parameter may limit the number
	 * of annotations that are written out if, for example, not all tokens are
	 * found inside sentences. If no value is given, then there will be no blank
	 * lines in the resulting file (assuming the AnnotationWriter does not
	 * produce a blank line). If you want there to be a blank line between each
	 * document (assuming the "OutputFile" is given a parameter), then this
	 * parameter should be given the value
	 * "org.apache.uima.jcas.tcas.DocumentAnnotation".
	 */
	public static final String PARAM_BLOCK_ANNOTATION_CLASS = "BlockAnnotationClass";

	/**
	 * "AnnotationWriterClass" is a single, required, string parameter that
	 * provides the class name of a class that extends
	 * org.cleartk.util.linewriter.AnnotationWriter
	 * 
	 * @see AnnotationWriter
	 */
	public static final String PARAM_ANNOTATION_WRITER_CLASS = "AnnotationWriterClass";

	/**
	 * "FileSuffix" is a single, optional, string parameter that provides a file
	 * name suffix for each file generated by this writer. If there is no value
	 * given for the parameter "OutputDirectory", then this parameter is
	 * ignored. If "OutputDirectory" is given a value, then the generated files
	 * will be named by the document ids and the suffix provided by this
	 * parameter. If no value for this parameter is given, then the files will
	 * be named the same as the document id.
	 * 
	 * @see DocumentUtil#getDocument(JCas)
	 * @see org.cleartk.type.Document#getIdentifier()
	 */
	public static final String PARAM_FILE_SUFFIX = "FileSuffix";

	private File outputDirectory;

	private File outputFile;

	private Class<? extends Annotation> outputAnnotationClass;

	private Type outputAnnotationType;

	private Class<? extends Annotation> blockAnnotationClass;

	private Type blockAnnotationType;

	boolean blockOnDocument = false;

	AnnotationWriter<T> annotationWriter;

	PrintStream out;

	String fileSuffix;

	private boolean typesInitialized = false;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			super.initialize(context);

			String outputDirectoryName = (String) context.getConfigParameterValue(PARAM_OUTPUT_DIRECTORY);
			if (outputDirectoryName != null && outputDirectoryName.trim().length() == 0) outputDirectoryName = null;
			String outputFileName = (String) context.getConfigParameterValue(PARAM_OUTPUT_FILE);
			if (outputFileName != null && outputFileName.trim().length() == 0) outputFileName = null;

			if (outputDirectoryName == null && outputFileName == null) {
				String key = ResourceInitializationException.CONFIG_SETTING_ABSENT;
				throw new ResourceInitializationException(key,
						new Object[] { PARAM_OUTPUT_DIRECTORY, PARAM_OUTPUT_FILE });
			}

			if (outputDirectoryName != null && outputFileName != null) {
				throw new ResourceInitializationException("One of the parameters " + PARAM_OUTPUT_DIRECTORY + " or "
						+ PARAM_OUTPUT_FILE + " must be set but not both.", null);
			}

			if (outputDirectoryName != null) {
				outputDirectory = new File(outputDirectoryName);
				if (!this.outputDirectory.exists()) {
					this.outputDirectory.mkdirs();
				}
			}

			if (outputFileName != null) {
				outputFile = new File(outputFileName);
				if (!outputFile.getParentFile().exists()) {
					outputFile.getParentFile().mkdirs();
				}
				out = new PrintStream(outputFile);
			}

			String outputAnnotationClassName = (String) context.getConfigParameterValue(PARAM_OUTPUT_ANNOTATION_CLASS);
			Class<?> cls = Class.forName(outputAnnotationClassName);
			outputAnnotationClass = cls.asSubclass(Annotation.class);

			String blockAnnotationClassName = (String) context.getConfigParameterValue(PARAM_BLOCK_ANNOTATION_CLASS);
			if (blockAnnotationClassName != null) {
				if (blockAnnotationClassName.equals("org.apache.uima.jcas.tcas.DocumentAnnotation")) {
					blockOnDocument = true;
				}
				else {
					cls = Class.forName(blockAnnotationClassName);
					blockAnnotationClass = cls.asSubclass(Annotation.class);
				}
			}

			String annotationWriterClassName = (String) UIMAUtil.getRequiredConfigParameterValue(context,
					PARAM_ANNOTATION_WRITER_CLASS);

			cls = Class.forName(annotationWriterClassName);
			Class<?> annotationWriterClass = cls.asSubclass(AnnotationWriter.class);
			@SuppressWarnings("unchecked")
			AnnotationWriter<T> annotationWriter = (AnnotationWriter<T>) annotationWriterClass.newInstance();
			this.annotationWriter = annotationWriter;

			java.lang.reflect.Type annotationWriterType = ReflectionUtil.getTypeArgument(
					AnnotationWriter.class, "T", this.annotationWriter);

			if (!ReflectionUtil.isAssignableFrom(annotationWriterType, outputAnnotationClass)) {
				throw new ResourceInitializationException("the class ", null);
			}

			this.annotationWriter.initialize(context);

			fileSuffix = (String) context.getConfigParameterValue(PARAM_FILE_SUFFIX);
			if (fileSuffix == null) {
				fileSuffix = "";
			}
			else if (!fileSuffix.startsWith(".")) {
				fileSuffix = "." + fileSuffix;
			}
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	private void initializeTypes(JCas jCas) throws AnalysisEngineProcessException {
		try {
			outputAnnotationType = UIMAUtil.getCasType(jCas, outputAnnotationClass);
			if (blockAnnotationClass != null) {
				blockAnnotationType = UIMAUtil.getCasType(jCas, blockAnnotationClass);
			}
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		typesInitialized = true;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		if (!typesInitialized) initializeTypes(jCas);

		try {
			if (outputDirectory != null) {
				String id = DocumentUtil.getIdentifier(jCas);
				if (id.endsWith(".")) id = id.substring(0, id.length() - 1);
				out = new PrintStream(new File(outputDirectory, id + fileSuffix));
			}

			if (blockOnDocument) {
				FSIterator outputAnnotations = jCas.getAnnotationIndex(outputAnnotationType).iterator();
				while (outputAnnotations.hasNext()) {
					@SuppressWarnings("unchecked")
					T outputAnnotation = (T) outputAnnotations.next();
					out.println(annotationWriter.writeAnnotation(jCas, outputAnnotation));

				}
				out.println();
			}
			else if (blockAnnotationType != null) {
				FSIterator blocks = jCas.getAnnotationIndex(blockAnnotationType).iterator();
				while (blocks.hasNext()) {
					Annotation blockAnnotation = (Annotation) blocks.next();
					FSIterator outputAnnotations = jCas.getAnnotationIndex(outputAnnotationType).subiterator(
							blockAnnotation);
					while (outputAnnotations.hasNext()) {
						@SuppressWarnings("unchecked")
						T outputAnnotation = (T) outputAnnotations.next();
						out.println(annotationWriter.writeAnnotation(jCas, outputAnnotation));
					}
					out.println();
				}
			}

			else {
				FSIterator outputAnnotations = jCas.getAnnotationIndex(outputAnnotationType).iterator();
				while (outputAnnotations.hasNext()) {
					@SuppressWarnings("unchecked")
					T outputAnnotation = (T) outputAnnotations.next();
					out.println(annotationWriter.writeAnnotation(jCas, outputAnnotation));
				}
			}

			if (outputDirectory != null) {
				out.flush();
				out.close();
			}
		}
		catch (FileNotFoundException fnfe) {
			throw new AnalysisEngineProcessException(fnfe);
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		if (outputFile != null) {
			out.flush();
			out.close();
		}
		// TODO Auto-generated method stub
		super.collectionProcessComplete();
	}
}
