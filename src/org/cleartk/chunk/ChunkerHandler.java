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
package org.cleartk.chunk;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.Initializable;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialAnnotationHandler;
import org.cleartk.classifier.SequentialInstanceConsumer;
import org.cleartk.util.UIMAUtil;
import org.uutuc.descriptor.ConfigurationParameter;
import org.uutuc.util.InitializeUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class ChunkerHandler implements SequentialAnnotationHandler<String>, Initializable {

	public static final String PARAM_LABELED_ANNOTATION_CLASS = "org.cleartk.chunk.ChunkerHandler.PARAM_LABELED_ANNOTATION_CLASS";
	@ConfigurationParameter(
			name = PARAM_LABELED_ANNOTATION_CLASS,
			mandatory = true,
			description = "names the class of the type system type used to associate B, I, and O (for example) labels with.  An example value might be 'org.cleartk.type.Token'")
	 private String labeledAnnotationClassName;

	public static final String PARAM_SEQUENCE_CLASS = "org.cleartk.chunk.ChunkerHandler.PARAM_SEQUENCE_CLASS";

	@ConfigurationParameter(
			name = PARAM_SEQUENCE_CLASS,
			mandatory = true,
			description = "names the class of the type system type that specifies a 'sequence' of labels.  An example might be something like 'org.cleartk.type.Sentence'")
	private String sequenceClassName;
	
	public static final String PARAM_CHUNK_LABELER_CLASS = "org.cleartk.chunk.ChunkerHandler.PARAM_CHUNK_LABELER_CLASS";

	@ConfigurationParameter(
			name = PARAM_CHUNK_LABELER_CLASS,
			mandatory = true,
			description = "provides the class name of a class that extends org.cleartk.chunk.ChunkLabeler.")
	private String chunkLabelerClassName;
	
	public static final String PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS = "org.cleartk.chunk.ChunkerHandler.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS";
	
	@ConfigurationParameter(
			name = PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS,
			mandatory = true,
			description = "provides the class name of a class that extends org.cleartk.chunk.ChunkFeatureExtractor.")
	private String chunkerFeatureExtractorClassName;
	
	protected Class<? extends Annotation> labeledAnnotationClass;

	private Type labeledAnnotationType;

	private Class<? extends Annotation> sequenceClass;

	private Type sequenceType;

	protected ChunkLabeler chunkLabeler;

	protected ChunkerFeatureExtractor featureExtractor;

	protected boolean typesInitialized = false;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		InitializeUtil.initialize(this, context);
		labeledAnnotationClass = UIMAUtil.getClass(labeledAnnotationClassName, Annotation.class);
		sequenceClass = UIMAUtil.getClass(sequenceClassName, Annotation.class);
		chunkLabeler = UIMAUtil.create(chunkLabelerClassName, ChunkLabeler.class, context);
		featureExtractor = UIMAUtil.create(chunkerFeatureExtractorClassName, ChunkerFeatureExtractor.class, context);
	}

	protected void initializeTypes(JCas jCas) throws AnalysisEngineProcessException {
		try {
			labeledAnnotationType = UIMAUtil.getCasType(jCas, labeledAnnotationClass);
			sequenceType = UIMAUtil.getCasType(jCas, sequenceClass);
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		typesInitialized = true;
	}

	protected FSIterator sequences(JCas jCas) {
		return jCas.getAnnotationIndex(sequenceType).iterator();
	}
	
	protected FSIterator labeledAnnotations(JCas jCas, Annotation sequence) {
		return jCas.getAnnotationIndex(labeledAnnotationType).subiterator(sequence);
	}
	
	public void process(JCas jCas, SequentialInstanceConsumer<String> consumer) throws AnalysisEngineProcessException, CleartkException {
		if (!typesInitialized) initializeTypes(jCas);


		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		Instance<String> instance;

		List<Annotation> labeledAnnotationList = new ArrayList<Annotation>();

		FSIterator sequences = sequences(jCas);

		boolean consumerReturnsValues = false;
		while (sequences.hasNext()) {
			Annotation sequence = (Annotation) sequences.next();
			if(consumer.expectsOutcomes())
				chunkLabeler.chunks2Labels(jCas, sequence);

			instances.clear();
			labeledAnnotationList.clear();

			FSIterator labeledAnnotations = labeledAnnotations(jCas, sequence);
			while (labeledAnnotations.hasNext()) {
				Annotation labeledAnnotation = (Annotation) labeledAnnotations.next();
				labeledAnnotationList.add(labeledAnnotation);

				instance = featureExtractor.extractFeatures(jCas, labeledAnnotation, sequence);

				String label = chunkLabeler.getLabel(labeledAnnotation);
				instance.setOutcome(label);
				instances.add(instance);
			}

			List<String> results = consumer.consumeSequence(instances);

			if (results != null) {
				consumerReturnsValues = true;
				for (int i = 0; i < results.size(); i++) {
					Annotation labeledAnnotation = labeledAnnotationList.get(i);
					String label = results.get(i);
					chunkLabeler.setLabel(labeledAnnotation, label);
				}
				
			}
			if(consumerReturnsValues)
				chunkLabeler.labels2Chunks(jCas, sequence);

		}
		

	}
	
	public void setLabeledAnnotationClassName(String labeledAnnotationClassName) {
		this.labeledAnnotationClassName = labeledAnnotationClassName;
	}

	public void setSequenceClassName(String sequenceClassName) {
		this.sequenceClassName = sequenceClassName;
	}

	public void setChunkLabelerClassName(String chunkLabelerClassName) {
		this.chunkLabelerClassName = chunkLabelerClassName;
	}

	public void setChunkerFeatureExtractorClassName(String chunkerFeatureExtractorClassName) {
		this.chunkerFeatureExtractorClassName = chunkerFeatureExtractorClassName;
	}

}

