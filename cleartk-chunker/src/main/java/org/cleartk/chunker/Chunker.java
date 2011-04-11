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
package org.cleartk.chunker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.Instance;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class Chunker extends CleartkSequenceAnnotator<String> {

  // The name is hard coded here as a string because it needs to be referenced as a constant in
  // ChunkLabeler_ImplBase
  public static final String PARAM_LABELED_ANNOTATION_CLASS_NAME = "org.cleartk.chunker.Chunker.labeledAnnotationClassName";

  @ConfigurationParameter(mandatory = true, description = "names the class of the type system type used to associate B, I, and O (for example) labels with.  An example value might be 'org.cleartk.type.Token'")
  protected String labeledAnnotationClassName;

  public static final String PARAM_SEQUENCE_CLASS_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      Chunker.class,
      "sequenceClassName");

  @ConfigurationParameter(mandatory = true, description = "names the class of the type system type that specifies a 'sequence' of labels.  An example might be something like 'org.cleartk.type.Sentence'")
  protected String sequenceClassName;

  public static final String PARAM_CHUNK_LABELER_CLASS_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      Chunker.class,
      "chunkLabelerClassName");

  @ConfigurationParameter(mandatory = true, description = "provides the class name of a class that extends org.cleartk.chunk.ChunkLabeler.")
  protected String chunkLabelerClassName;

  public static final String PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      Chunker.class,
      "chunkerFeatureExtractorClassName");

  @ConfigurationParameter(mandatory = true, description = "provides the class name of a class that extends org.cleartk.chunk.ChunkFeatureExtractor.")
  protected String chunkerFeatureExtractorClassName;

  protected Class<? extends Annotation> labeledAnnotationClass;

  protected Class<? extends Annotation> sequenceClass;

  protected ChunkLabeler chunkLabeler;

  protected ChunkerFeatureExtractor featureExtractor;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    labeledAnnotationClass = InitializableFactory.getClass(
        labeledAnnotationClassName,
        Annotation.class);
    sequenceClass = InitializableFactory.getClass(sequenceClassName, Annotation.class);
    chunkLabeler = InitializableFactory.create(context, chunkLabelerClassName, ChunkLabeler.class);
    featureExtractor = InitializableFactory.create(
        context,
        chunkerFeatureExtractorClassName,
        ChunkerFeatureExtractor.class);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    List<Instance<String>> instances = new ArrayList<Instance<String>>();
    Instance<String> instance;

    List<Annotation> labeledAnnotationList = new ArrayList<Annotation>();

    for (Annotation sequence : JCasUtil.select(jCas, this.sequenceClass)) {
      if (this.isTraining()) {
        chunkLabeler.chunks2Labels(jCas, sequence);
      }
      instances.clear();
      labeledAnnotationList.clear();

      // select the sub-annotations within this sequence
      Collection<? extends Annotation> labeledAnnotations;
      labeledAnnotations = JCasUtil.selectCovered(jCas, this.labeledAnnotationClass, sequence);
      if (labeledAnnotations.size() == 0) {
        throw new RuntimeException(String.format(
            "Found %s containing no %s: \"%s\" %s",
            this.sequenceClassName,
            this.labeledAnnotationClassName,
            sequence.getCoveredText(),
            sequence));
      }

      // create an instance for each sub-annotation
      for (Annotation labeledAnnotation : labeledAnnotations) {
        labeledAnnotationList.add(labeledAnnotation);
        instance = featureExtractor.extractFeatures(jCas, labeledAnnotation, sequence);
        String label = chunkLabeler.getLabel(labeledAnnotation);
        instance.setOutcome(label);
        instances.add(instance);
      }

      // write data while training
      if (this.isTraining()) {
        this.dataWriter.write(instances);
      }

      // set labels during classification
      else {
        List<String> results = this.classify(instances);
        for (int i = 0; i < results.size(); i++) {
          Annotation labeledAnnotation = labeledAnnotationList.get(i);
          String label = results.get(i);
          chunkLabeler.setLabel(labeledAnnotation, label);
        }
        chunkLabeler.labels2Chunks(jCas, sequence);
      }
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
