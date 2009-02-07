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
package org.cleartk.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.Constants;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.impl.AnalysisEngineDescription_impl;
import org.apache.uima.analysis_engine.impl.PrimitiveAnalysisEngine_impl;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.impl.CollectionReaderDescription_impl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceCreationSpecifier;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.ConfigurationParameter;
import org.apache.uima.resource.metadata.ConfigurationParameterDeclarations;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.ConfigurationParameter_impl;
import org.apache.uima.resource.metadata.impl.Import_impl;
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLParser;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.EmptyAnnotator;
import org.cleartk.util.XReader;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public class TestsUtil {

	public static class HideOutput extends OutputStream {
		protected PrintStream out;

		protected PrintStream err;

		public HideOutput() {
			this.out = System.out;
			this.err = System.err;
			System.setOut(new PrintStream(this));
			System.setErr(new PrintStream(this));
		}

		public void restoreOutput() {
			System.setOut(this.out);
			System.setErr(this.err);
		}

		@Override
		public void write(int b) throws IOException {
		}
	}

	public static JCas getJCas(String xmiFileName) throws UIMAException, IOException {
		return getJCas(xmiFileName, getTypeSystem("org.cleartk.TypeSystem"));
	}

	public static JCas getJCas(String xmiFileName, TypeSystemDescription typeSystemDescription) throws UIMAException,
			IOException {
		return getJCas(xmiFileName, typeSystemDescription, true);
	}

	public static JCas getJCas(String xmiFileName, TypeSystemDescription typeSystemDescription, boolean isXmi)
			throws UIMAException, IOException {
		if (isXmi) {
			CollectionReader reader = TestsUtil.getCollectionReader(XReader.class, typeSystemDescription,
					XReader.PARAM_XML_SCHEME, "XMI", PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY, xmiFileName);

			return new TestsUtil.JCasIterable(reader).next();
		} else {
			CollectionReader reader = TestsUtil.getCollectionReader(XReader.class, typeSystemDescription,
					XReader.PARAM_XML_SCHEME, "XCAS", PlainTextCollectionReader.PARAM_FILE_OR_DIRECTORY, xmiFileName);

			return new TestsUtil.JCasIterable(reader).next();
			
		}
	}

	/**
	 * Disable UIMA logging.
	 * 
	 * @return The original logging level.
	 */
	public static Level disableLogging() {
		Logger logger = Logger.getLogger("org.apache.uima");
		while (logger.getLevel() == null) {
			logger = logger.getParent();
		}
		Level level = logger.getLevel();
		logger.setLevel(Level.OFF);
		return level;
	}

	/**
	 * Enable UIMA logging.
	 * 
	 * @param level
	 *            The logging level to be restored. Usually this is the result
	 *            returned by TestsUtil.disableLogging().
	 */
	public static void enableLogging(Level level) {
		Logger logger = Logger.getLogger("org.apache.uima");
		logger.setLevel(level);
	}

	/**
	 * Creates an AnalysisEngine from the given descriptor, and uses the engine
	 * to process the file or text.
	 * 
	 * @param descriptorFileName
	 *            The AnalysisEngine descriptor file.
	 * @param fileNameOrText
	 *            Either the path of a file to be loaded, or a string to use as
	 *            the text. If the string given is not a valid path in the file
	 *            system, it will be assumed to be text.
	 * @return A JCas object containing the processed document.
	 * @throws IOException
	 * @throws UIMAException
	 */
	public static JCas process(String descriptorFileName, String fileNameOrText) throws IOException, UIMAException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(descriptorFileName);
		return TestsUtil.process(engine, fileNameOrText);
	}

	/**
	 * Processes the file or text with an empty AnalysisEngine.
	 * 
	 * @param fileNameOrText
	 *            Either the path of a file to be loaded, or a string to use as
	 *            the text. If the string given is not a valid path in the file
	 *            system, it will be assumed to be text.
	 * @return A JCas object containing the processed document.
	 * @throws IOException
	 * @throws UIMAException
	 */
	public static JCas process(String fileNameOrText) throws IOException, UIMAException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(EmptyAnnotator.class, TestsUtil
				.getTypeSystem("org.cleartk.TypeSystem"));
		return TestsUtil.process(engine, fileNameOrText);
	}

	/**
	 * Processes the file or text with the given AnalysisEngine.
	 * 
	 * @param analysisEngine
	 *            The AnalysisEngine object to process the text.
	 * @param fileNameOrText
	 *            Either the path of a file to be loaded, or a string to use as
	 *            the text. If the string given is not a valid path in the file
	 *            system, it will be assumed to be text.
	 * @return A JCas object containing the processed document.
	 * @throws IOException
	 * @throws UIMAException
	 */
	public static JCas process(AnalysisEngine analysisEngine, String fileNameOrText) throws IOException, UIMAException {
		File textFile = new File(fileNameOrText);
		String text;
		if (textFile.exists()) {
			text = FileUtils.file2String(textFile);
		}
		else {
			text = fileNameOrText;
		}

		JCas jCas = analysisEngine.newJCas();
		jCas.setDocumentText(text);
		analysisEngine.process(jCas);
		return jCas;
	}

	/**
	 * Initializes the instance producer with the AnalysisEngine's context, and
	 * returns all instances created by the producer for the given JCas. The
	 * value null will be returned for each instance.
	 * 
	 * @param producer
	 *            The object for producing ClassifierInstances from a JCas
	 * @param engine
	 *            The AnalysisEngine with the context for initialization.
	 * @param jCas
	 *            The JCas to be processed.
	 * @return The list of ClassifierInstances created by the producer.
	 * @throws UIMAException
	 */
	public static <T> List<Instance<T>> produceInstances(AnnotationHandler<T> producer, AnalysisEngine engine, JCas jCas)
			throws UIMAException {
		return produceInstances(producer, null, engine, jCas);
	}

	/**
	 * Initializes the instance producer with the AnalysisEngine's context, and
	 * returns all instances created by the producer for the given JCas.
	 * 
	 * @param producer
	 *            The object for producing ClassifierInstances from a JCas
	 * @param returnValue
	 *            The value that should be returned for each instance
	 * @param engine
	 *            The AnalysisEngine with the context for initialization.
	 * @param jCas
	 *            The JCas to be processed.
	 * @return The list of ClassifierInstances created by the producer.
	 * @throws UIMAException
	 */
	public static <T> List<Instance<T>> produceInstances(AnnotationHandler<T> producer, T returnValue,
			AnalysisEngine engine, JCas jCas) throws UIMAException {
		UimaContext context = engine.getUimaContext();
		AnnotatorConsumer<T> consumer = new AnnotatorConsumer<T>(returnValue);
		producer.initialize(context);
		producer.process(jCas, consumer);
		return consumer.instances;
	}

	/**
	 * A simple instance consumer that stores all instances in a public
	 * attribute. Intended primarily for testing.
	 */
	public static class AnnotatorConsumer<T> implements InstanceConsumer<T> {
		public List<Instance<T>> instances;

		public T returnValue;

		public AnnotatorConsumer(T returnValue) {
			this.instances = new ArrayList<Instance<T>>();
			this.returnValue = returnValue;
		}

		public T consume(Instance<T> instance) {
			this.instances.add(instance);
			return this.returnValue;
		}

		public List<T> consumeAll(List<Instance<T>> instances) {
			this.instances.addAll(instances);
			List<T> result = null;
			if (this.returnValue != null) {
				result = new ArrayList<T>();
				for (int i = 0; i < instances.size(); i++) {
					result.add(this.returnValue);
				}
			}
			return result;
		}

		public boolean expectsOutcomes() {
			return this.returnValue == null;
		}
	}

	/**
	 * A class implementing iteration over a the documents of a collection. Each
	 * element in the Iterable is a JCas containing a single document. The
	 * documents have been loaded by the CollectionReader and processed by the
	 * AnalysisEngine (if any).
	 */
	public static class JCasIterable implements Iterator<JCas>, Iterable<JCas> {

		private CollectionReader collectionReader;

		private AnalysisEngine[] analysisEngines;

		private JCas jCas;

		/**
		 * Iterate over the documents loaded by the CollectionReader. (Uses an
		 * EmptyAnnotator to create the document JCas.)
		 * 
		 * @param reader
		 *            The CollectionReader for loading documents.
		 * @throws UIMAException
		 * @throws IOException
		 */
		public JCasIterable(CollectionReader reader) throws UIMAException, IOException {
			this(reader, getAnalysisEngine(EmptyAnnotator.class, getTypeSystem("org.cleartk.TypeSystem")));
		}

		/**
		 * Iterate over the documents loaded by the CollectionReader, running
		 * the AnalysisEngine on each one before yielding them.
		 * 
		 * @param reader
		 *            The CollectionReader for loading documents.
		 * @param engines
		 *            The AnalysisEngines for processing documents.
		 * @throws UIMAException
		 * @throws IOException
		 */
		public JCasIterable(CollectionReader reader, AnalysisEngine... engines) throws UIMAException, IOException {
			this.collectionReader = reader;
			this.analysisEngines = engines;
			List<ResourceMetaData> metaData = new ArrayList<ResourceMetaData>();
			metaData.add(reader.getMetaData());
			for (AnalysisEngine engine : engines) {
				metaData.add(engine.getMetaData());
			}
			this.jCas = CasCreationUtils.createCas(metaData).getJCas();
		}

		public Iterator<JCas> iterator() {
			return this;
		}

		public boolean hasNext() {
			try {
				return this.collectionReader.hasNext();
			}
			catch (CollectionException e) {
				throw new RuntimeException(e);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public JCas next() {
			this.jCas.reset();
			try {
				this.collectionReader.getNext(this.jCas.getCas());
				for (AnalysisEngine engine : this.analysisEngines) {
					engine.process(this.jCas);
				}
			}
			catch (CollectionException e) {
				throw new RuntimeException(e);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			catch (AnalysisEngineProcessException e) {
				throw new RuntimeException(e);
			}
			return this.jCas;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Create a new JCas with the ClearTK type system.
	 * 
	 * @return The new JCas.
	 * @throws UIMAException
	 */
	public static JCas newJCas() throws UIMAException {
		return newJCas("org.cleartk.TypeSystem");
	}

	public static JCas newJCas(String typeSystemDescriptorName) throws UIMAException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(EmptyAnnotator.class, TestsUtil
				.getTypeSystem(typeSystemDescriptorName));
		return engine.newJCas();

	}

	public static JCas newJCasFromPath(String typeSystemDescriptor) throws UIMAException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(EmptyAnnotator.class, TestsUtil
				.getTypeSystemFromPath(typeSystemDescriptor));
		return engine.newJCas();

	}

	/**
	 * Create a new UimaContext with the given configuration parameters set.
	 * 
	 * @param configurationParameters
	 *            The parameters to be set.
	 * @return The new UimaContext.
	 * @throws ResourceInitializationException
	 *             If the context could not be created.
	 */
	public static UimaContext getUimaContext(Object... configurationParameters) throws ResourceInitializationException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(EmptyAnnotator.class, null, configurationParameters);
		return engine.getUimaContext();
	}

	public static void createTokens(JCas jCas, String text) {
		createTokens(jCas, text, null, null, null);
	}

	public static void createTokens(JCas jCas, String text, String tokensString) {
		createTokens(jCas, text, tokensString, null, null);
	}

	/**
	 * Add Token and Sentence annotations to the JCas.
	 * 
	 * @param jCas
	 *            The JCas where the annotation should be added.
	 * @param text
	 *            The text of the document.
	 * @param tokensString
	 *            The text of the document with spaces separating words and
	 *            newlines separating sentences.
	 * @param posTagsString
	 *            The part of speech tags for each word, separated by spaces.
	 * @param stemsString
	 *            The stems for each word, separated by spaces.
	 */
	public static void createTokens(JCas jCas, String text, String tokensString, String posTagsString,
			String stemsString) {
		// set the document text and add Token annotations as indicated
		jCas.setDocumentText(text);
		int offset = 0;
		int tokenIndex = 0;
		String[] sentenceStrings = (tokensString != null ? tokensString : text).split("\n");
		String[] posTags = posTagsString != null ? posTagsString.split(" ") : null;
		String[] stems = stemsString != null ? stemsString.split(" ") : null;
		for (String sentenceString : sentenceStrings) {
			List<Token> tokenAnnotations = new ArrayList<Token>();
			for (String tokenString : sentenceString.trim().split(" ")) {
				// move the offset up to the beginning of the token
				while (!text.startsWith(tokenString, offset)) {
					offset++;
					if (offset > text.length()) {
						throw new IllegalArgumentException(String.format("unable to find string %s", tokenString));
					}
				}

				// add the Token
				int start = offset;
				offset = offset + tokenString.length();
				Token token = new Token(jCas, start, offset);
				token.addToIndexes();
				tokenAnnotations.add(token);

				// set the stem and part of speech if present
				if (posTags != null) {
					token.setPos(posTags[tokenIndex]);
				}
				if (stems != null) {
					token.setStem(stems[tokenIndex]);
				}
				tokenIndex++;
			}
			if (tokenAnnotations.size() > 0) {
				int begin = tokenAnnotations.get(0).getBegin();
				int end = tokenAnnotations.get(tokenAnnotations.size() - 1).getEnd();
				Sentence sentence = new Sentence(jCas, begin, end);
				sentence.addToIndexes();
			}
		}
	}

	/**
	 * Create a leaf TreebankNode in a JCas.
	 * 
	 * @param jCas
	 *            The JCas which the annotation should be added to.
	 * @param begin
	 *            The begin offset of the node.
	 * @param end
	 *            The end offset of the node.
	 * @param nodeType
	 *            The part of speech tag of the node.
	 * @return The TreebankNode which was added to the JCas.
	 */
	public static TreebankNode newNode(JCas jCas, int begin, int end, String nodeType) {
		TreebankNode node = new TreebankNode(jCas, begin, end);
		node.setNodeType(nodeType);
		node.setChildren(new FSArray(jCas, 0));
		node.setLeaf(true);
		node.addToIndexes();
		return node;
	}

	/**
	 * Create a branch TreebankNode in a JCas. The offsets of this node will be
	 * determined by its children.
	 * 
	 * @param jCas
	 *            The JCas which the annotation should be added to.
	 * @param nodeType
	 *            The phrase type tag of the node.
	 * @param children
	 *            The TreebankNode children of the node.
	 * @return The TreebankNode which was added to the JCas.
	 */
	public static TreebankNode newNode(JCas jCas, String nodeType, TreebankNode... children) {
		int begin = children[0].getBegin();
		int end = children[children.length - 1].getEnd();
		TreebankNode node = new TreebankNode(jCas, begin, end);
		node.setNodeType(nodeType);
		node.addToIndexes();
		FSArray fsArray = new FSArray(jCas, children.length);
		fsArray.copyFromArray(children, 0, 0, children.length);
		node.setChildren(fsArray);
		for (TreebankNode child : children) {
			child.setParent(node);
		}
		return node;
	}

	/**
	 * Create a CollectionReader from an XML descriptor file and a set of
	 * configuration parameters.
	 * 
	 * @param descriptorPath
	 *            The path to the XML descriptor file.
	 * @param parameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The CollectionReader created from the XML descriptor and the
	 *         configuration parameters.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static CollectionReader getCollectionReaderFromPath(String descriptorPath, Object... parameters)
			throws UIMAException, IOException {
		ResourceCreationSpecifier specifier;
		specifier = TestsUtil.getResourceCreationSpecifier(descriptorPath, parameters);
		return UIMAFramework.produceCollectionReader(specifier);
	}
	
	/**
	 * Get a CollectionReader from the name (Java-style, dotted) of an XML
	 * descriptor file, and a set of configuration parameters.
	 * 
	 * @param descriptorName
	 *            The fully qualified, Java-style, dotted name of the XML
	 *            descriptor file.
	 * @param parameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The AnalysisEngine created from the XML descriptor and the
	 *         configuration parameters.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static CollectionReader getCollectionReader(String descriptorName, Object... parameters)
	throws UIMAException, IOException {
		Import_impl imp = new Import_impl();
		imp.setName(descriptorName);
		URL url = imp.findAbsoluteUrl(UIMAFramework.newDefaultResourceManager());
		ResourceSpecifier specifier = TestsUtil.getResourceCreationSpecifier(url, parameters);
		return UIMAFramework.produceCollectionReader(specifier);
	}
	/**
	 * Get an AnalysisEngine from an XML descriptor file and a set of
	 * configuration parameters.
	 * 
	 * @param descriptorPath
	 *            The path to the XML descriptor file.
	 * @param parameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The AnalysisEngine created from the XML descriptor and the
	 *         configuration parameters.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static AnalysisEngine getAnalysisEngineFromPath(String descriptorPath, Object... parameters) throws UIMAException,
			IOException {
		ResourceSpecifier specifier;
		specifier = TestsUtil.getResourceCreationSpecifier(descriptorPath, parameters);
		return UIMAFramework.produceAnalysisEngine(specifier);
	}
	
	/**
	 * Get an AnalysisEngine from the name (Java-style, dotted) of an XML
	 * descriptor file, and a set of configuration parameters.
	 * 
	 * @param descriptorName
	 *            The fully qualified, Java-style, dotted name of the XML
	 *            descriptor file.
	 * @param parameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The AnalysisEngine created from the XML descriptor and the
	 *         configuration parameters.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static AnalysisEngine getAnalysisEngine(String descriptorName, Object... parameters) throws UIMAException,
			IOException {
		Import_impl imp = new Import_impl();
		imp.setName(descriptorName);
		URL url = imp.findAbsoluteUrl(UIMAFramework.newDefaultResourceManager());
		ResourceSpecifier specifier = TestsUtil.getResourceCreationSpecifier(url, parameters);
		return UIMAFramework.produceAnalysisEngine(specifier);
	}

	/**
	 * Get an AnalysisEngine from an AnalysisComponent class, a type system and
	 * a set of configuration parameters.
	 * 
	 * @param componentClass
	 *            The class of the AnalysisComponent to be created as an
	 *            AnalysisEngine.
	 * @param typeSystem
	 *            A description of the types used by the AnalysisComponent (may
	 *            be null).
	 * @param configurationParameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The AnalysisEngine created from the AnalysisComponent class and
	 *         initialized with the type system and the configuration
	 *         parameters.
	 * @throws ResourceInitializationException
	 */
	public static AnalysisEngine getAnalysisEngine(Class<? extends AnalysisComponent> componentClass,
			TypeSystemDescription typeSystem, Object... configurationParameters) throws ResourceInitializationException {

		// create the descriptor and set configuration parameters
		AnalysisEngineDescription desc = new AnalysisEngineDescription_impl();
		desc.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		desc.setPrimitive(true);
		desc.setAnnotatorImplementationName(componentClass.getName());
		TestsUtil.setConfigurationParameters(desc, configurationParameters);

		// set the type system
		if (typeSystem != null) {
			desc.getAnalysisEngineMetaData().setTypeSystem(typeSystem);
		}

		// create the AnalysisEngine, initialize it and return it
		AnalysisEngine engine = new PrimitiveAnalysisEngine_impl();
		engine.initialize(desc, null);
		return engine;

	}

	/**
	 * Get a CollectionReader from a CollectionReader class, a type system, and
	 * a set of configuration parameters.
	 * 
	 * @param readerClass
	 *            The class of the CollectionReader to be created.
	 * @param typeSystem
	 *            A description of the types used by the CollectionReader (may
	 *            be null).
	 * @param configurationParameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The CollectionReader created and initialized with the type system
	 *         and configuration parameters.
	 * @throws ResourceInitializationException
	 */
	public static CollectionReader getCollectionReader(Class<? extends CollectionReader> readerClass,
			TypeSystemDescription typeSystem, Object... configurationParameters) throws ResourceInitializationException {

		// create the descriptor and set configuration parameters
		CollectionReaderDescription desc = new CollectionReaderDescription_impl();
		desc.setFrameworkImplementation(Constants.JAVA_FRAMEWORK_NAME);
		desc.setImplementationName(readerClass.getName());
		TestsUtil.setConfigurationParameters(desc, configurationParameters);

		// set the type system
		if (typeSystem != null) {
			desc.getCollectionReaderMetaData().setTypeSystem(typeSystem);
		}

		// create the CollectionReader
		CollectionReader reader;
		try {
			reader = readerClass.newInstance();
		}
		catch (InstantiationException e) {
			throw new ResourceInitializationException(e);
		}
		catch (IllegalAccessException e) {
			throw new ResourceInitializationException(e);
		}

		// initialize the CollectionReader and return it
		reader.initialize(desc, null);
		return reader;
	}

	/**
	 * Creates a TypeSystemDescription from a list of Annotation types.
	 * 
	 * @param annotationClasses
	 *            The Annotation class objects.
	 * @return A TypeSystemDescription that includes all of the specified
	 *         Annotation types.
	 */
	public static TypeSystemDescription getTypeSystem(Class<?>... annotationClasses) {
		TypeSystemDescription typeSystem = new TypeSystemDescription_impl();
		List<Import> imports = new ArrayList<Import>();
		for (Class<?> annotationClass : annotationClasses) {
			Import imp = new Import_impl();
			imp.setName(annotationClass.getName());
			imports.add(imp);
		}
		Import[] importArray = new Import[imports.size()];
		typeSystem.setImports(imports.toArray(importArray));
		return typeSystem;
	}

	/**
	 * Creates a TypeSystemDescription from descriptor names.
	 * 
	 * @param descriptorNames
	 *            The fully qualified, Java-style, dotted descriptor names.
	 * @return A TypeSystemDescription that includes the types from all of the
	 *         specified files.
	 */
	public static TypeSystemDescription getTypeSystem(String... descriptorNames) {
		TypeSystemDescription typeSystem = new TypeSystemDescription_impl();
		List<Import> imports = new ArrayList<Import>();
		for (String descriptorName : descriptorNames) {
			Import imp = new Import_impl();
			imp.setName(descriptorName);
			imports.add(imp);
		}
		Import[] importArray = new Import[imports.size()];
		typeSystem.setImports(imports.toArray(importArray));
		return typeSystem;
	}

	/**
	 * Creates a TypeSystemDescription from descriptor files.
	 * 
	 * @param descriptorPaths
	 *            The descriptor file paths.
	 * @return A TypeSystemDescription that includes the types from all of the
	 *         specified files.
	 */
	public static TypeSystemDescription getTypeSystemFromPath(String... descriptorPaths) {
		TypeSystemDescription typeSystem = new TypeSystemDescription_impl();
		List<Import> imports = new ArrayList<Import>();
		for (String descriptorPath : descriptorPaths) {
			Import imp = new Import_impl();
			imp.setLocation(descriptorPath);
			imports.add(imp);
		}
		Import[] importArray = new Import[imports.size()];
		typeSystem.setImports(imports.toArray(importArray));
		return typeSystem;
	}

	/**
	 * Parse a ResourceCreationSpecifier from an XML descriptor file, setting
	 * additional configuration parameters as necessary.
	 * 
	 * @param descriptorPath
	 *            The path to the XML descriptor file.
	 * @param parameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The ResourceCreationSpecifier for the XML descriptor with all the
	 *         configuration parameters set.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static ResourceCreationSpecifier getResourceCreationSpecifier(String descriptorPath, Object[] parameters)
	throws UIMAException, IOException {
		return TestsUtil.getResourceCreationSpecifier(new XMLInputSource(descriptorPath), parameters);
	}

	/**
	 * Parse a ResourceCreationSpecifier from the URL of an XML descriptor file,
	 * setting additional configuration parameters as necessary.
	 * 
	 * @param descriptorURL
	 *            The URL of the XML descriptor file.
	 * @param parameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The ResourceCreationSpecifier for the XML descriptor with all the
	 *         configuration parameters set.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static ResourceCreationSpecifier getResourceCreationSpecifier(URL descriptorURL, Object[] parameters)
	throws UIMAException, IOException {
		return TestsUtil.getResourceCreationSpecifier(new XMLInputSource(descriptorURL), parameters);
	}
	
	/**
	 * Parse a ResourceCreationSpecifier from XML descriptor file input,
	 * setting additional configuration parameters as necessary.
	 * 
	 * @param xmlInput
	 *            The descriptor file as an XMLInputSource.
	 * @param parameters
	 *            Any additional configuration parameters to be set. These
	 *            should be supplied as (name, value) pairs, so there should
	 *            always be an even number of parameters.
	 * @return The ResourceCreationSpecifier for the XML descriptor with all the
	 *         configuration parameters set.
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static ResourceCreationSpecifier getResourceCreationSpecifier(XMLInputSource xmlInput, Object[] parameters)
	throws UIMAException, IOException {
		if (parameters.length % 2 != 0) {
			String message = "a value must be specified for each parameter name";
			throw new IllegalArgumentException(message);
		}
		ResourceCreationSpecifier specifier;
		XMLParser parser = UIMAFramework.getXMLParser();
		specifier = (ResourceCreationSpecifier) parser.parseResourceSpecifier(xmlInput);
		ResourceMetaData metaData = specifier.getMetaData();
		ConfigurationParameterSettings settings = metaData.getConfigurationParameterSettings();
		for (int i = 0; i < parameters.length; i += 2) {
			settings.setParameterValue((String) parameters[i], parameters[i + 1]);
		}
		return specifier;
	}
	
	/**
	 * Create configuration parameter declarations and settings from a list of
	 * (name, value) pairs.
	 * 
	 * @param specifier
	 *            The ResourceCreationSpecifier whose parameters are to be set.
	 * @param configurationParameters
	 *            The configuration parameters to be set. These should be
	 *            supplied as (name, value) pairs, so there should always be an
	 *            even number of parameters.
	 */
	public static void setConfigurationParameters(ResourceCreationSpecifier specifier,
			Object... configurationParameters) {
		if (configurationParameters.length % 2 != 0) {
			String message = "a value must be specified for each parameter name";
			throw new IllegalArgumentException(message);
		}
		ResourceMetaData metaData = specifier.getMetaData();
		ConfigurationParameterDeclarations paramDecls = metaData.getConfigurationParameterDeclarations();
		ConfigurationParameterSettings paramSettings = metaData.getConfigurationParameterSettings();
		for (int i = 0; i < configurationParameters.length; i += 2) {
			String name = (String) configurationParameters[i];
			Object value = configurationParameters[i + 1];

			Class<?> javaClass = value.getClass();
			String javaClassName;
			if (javaClass.isArray()) javaClassName = javaClass.getComponentType().getName();
			else javaClassName = javaClass.getName();

			String uimaType = TestsUtil.javaUimaTypeMap.get(javaClassName);
			if (uimaType == null) {
				String message = "invalid parameter type " + javaClassName;
				throw new IllegalArgumentException(message);
			}
			ConfigurationParameter param = new ConfigurationParameter_impl();
			param.setName(name);
			param.setType(uimaType);
			param.setMultiValued(javaClass.isArray());
			paramDecls.addConfigurationParameter(param);
			paramSettings.setParameterValue(name, value);
		}
	}

	/**
	 * A mapping from Java class names to UIMA configuration parameter type
	 * names. Used by setConfigurationParameters().
	 */
	public static final Map<String, String> javaUimaTypeMap = new HashMap<String, String>();
	static {
		TestsUtil.javaUimaTypeMap.put(Boolean.class.getName(), ConfigurationParameter.TYPE_BOOLEAN);
		TestsUtil.javaUimaTypeMap.put(Float.class.getName(), ConfigurationParameter.TYPE_FLOAT);
		TestsUtil.javaUimaTypeMap.put(Integer.class.getName(), ConfigurationParameter.TYPE_INTEGER);
		TestsUtil.javaUimaTypeMap.put(String.class.getName(), ConfigurationParameter.TYPE_STRING);
	};

	// Provides a way to create an annotation and addToIndexes in a single line.
	public static <T extends Annotation> T createAnnotation(JCas jCas, int begin, int end, Class<T> cls) {
		try {
			T annotation = cls.getConstructor(JCas.class, Integer.TYPE, Integer.TYPE).newInstance(jCas, begin, end);
			annotation.addToIndexes();
			return annotation;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Empty the files in the directory, and delete the directory itself.
	 * 
	 * @param dir
	 *            The directory to be deleted
	 */
	public static void tearDown(File dir) {
		if (!dir.exists()) return;
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) tearDown(file);
			file.delete();
		}
		dir.delete();
	}

	/**
	 * Empty the files in the directory, but do not delete the directory itself.
	 * 
	 * @param dir
	 *            The directory to be emptied
	 */
	public static void emptyDirectory(File dir) {
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (file.isDirectory() && !file.getName().equals(".svn")) {
					TestsUtil.emptyDirectory(file);
				}
				file.delete();
			}
		}
	}

	/**
	 * A simple do-nothing AnnotationHandler that expects Boolean outcomes.
	 * Useful primarily for testing DataWriter objects which require some
	 * annotation handler to be specified.
	 */
	public static class EmptyBooleanHandler implements AnnotationHandler<Boolean> {
		public void initialize(UimaContext context) throws ResourceInitializationException {
		}

		public void process(JCas cas, InstanceConsumer<Boolean> consumer) throws AnalysisEngineProcessException {
		}
	}

	/**
	 * A simple do-nothing AnnotationHandler that expects String outcomes.
	 * Useful primarily for testing DataWriter objects which require some
	 * annotation handler to be specified.
	 */
	public static class EmptyStringHandler implements AnnotationHandler<String> {
		public void initialize(UimaContext context) throws ResourceInitializationException {
		}

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
		}
	}

	/**
	 * A random number generator for creating Instance objects
	 */
	private static final Random random = new Random(42);

	/**
	 * Create a number of random Instance objects that should be easy to
	 * classify. This is primarily useful for testing DataWriter and Classifier
	 * implementations.
	 * 
	 * @param n
	 *            The number of instances
	 * @return The list of newly-created instances
	 */
	public static List<Instance<Boolean>> generateBooleanInstances(int n) {
		List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
		for (int i = 0; i < n; i++) {
			Instance<Boolean> instance = new Instance<Boolean>();
			if (TestsUtil.random.nextInt(2) == 0) {
				instance.setOutcome(true);
				instance.add(new Feature("hello", TestsUtil.random.nextInt(1000) + 1000));
			}
			else {
				instance.setOutcome(false);
				instance.add(new Feature("hello", TestsUtil.random.nextInt(100)));
			}
			instances.add(instance);
		}
		return instances;
	}

	/**
	 * Create a number of random Instance objects that should be easy to
	 * classify. This is primarily useful for testing DataWriter and Classifier
	 * implementations.
	 * 
	 * @param n
	 *            The number of instances
	 * @return The list of newly-created instances
	 */
	public static List<Instance<String>> generateStringInstances(int n) {
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		for (int i = 0; i < n; i++) {
			Instance<String> instance = new Instance<String>();
			switch (TestsUtil.random.nextInt(3)) {
			case 0:
				instance.setOutcome("A");
				instance.add(new Feature("hello", -1050 + TestsUtil.random.nextInt(100)));
				break;
			case 1:
				instance.setOutcome("B");
				instance.add(new Feature("hello", -50 + TestsUtil.random.nextInt(100)));
				break;
			case 2:
				instance.setOutcome("C");
				instance.add(new Feature("hello", 950 + TestsUtil.random.nextInt(100)));
				break;
			}
			instances.add(instance);
		}
		return instances;
	}
}
