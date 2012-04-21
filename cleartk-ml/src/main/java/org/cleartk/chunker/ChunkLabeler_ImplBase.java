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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.chunking.Chunking;
import org.cleartk.util.UIMAUtil;
import org.uimafit.component.initialize.ConfigurationParameterInitializer;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.Initializable;
import org.uimafit.factory.initializable.InitializableFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @deprecated Use {@link Chunking} instead.
 */

@Deprecated
public abstract class ChunkLabeler_ImplBase implements ChunkLabeler, Initializable {

  public static final String PARAM_CHUNK_ANNOTATION_CLASS_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      ChunkLabeler_ImplBase.class,
      "chunkAnnotationClassName");

  @ConfigurationParameter(
      mandatory = true,
      description = "names the class of the type system chunk annotation type. An example value might be something like: 'org.cleartk.type.ne.NamedEntityMention'")
  private String chunkAnnotationClassName;

  @ConfigurationParameter(
      name = Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME,
      mandatory = true,
      description = "names the class of the type system type used to associate B, I, and O (for example) labels with.  An example value might be 'org.cleartk.type.Token'")
  private String labeledAnnotationClassName;

  public static final String BEGIN_PREFIX = "B";

  public static final String INSIDE_PREFIX = "I";

  public static final String OUTSIDE_LABEL = "O";

  public static final String SEPARATOR = "-";

  protected Class<? extends Annotation> chunkAnnotationClass;

  protected Type chunkAnnotationType;

  protected Class<? extends Annotation> labeledAnnotationClass;

  protected Type labeledAnnotationType;

  protected boolean typesInitialized = false;

  protected Map<Annotation, String> annotationLabels;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    ConfigurationParameterInitializer.initialize(this, context);
    labeledAnnotationClass = InitializableFactory.getClass(
        labeledAnnotationClassName,
        Annotation.class);
    chunkAnnotationClass = InitializableFactory.getClass(chunkAnnotationClassName, Annotation.class);
    annotationLabels = new HashMap<Annotation, String>();
  }

  public abstract String getChunkLabel(JCas jCas, Annotation chunkAnnotation)
      throws AnalysisEngineProcessException;

  public abstract Annotation createChunk(
      JCas jCas,
      List<? extends Annotation> labeledAnnotations,
      String label) throws AnalysisEngineProcessException;

  protected void initializeTypes(JCas jCas) throws AnalysisEngineProcessException {
    try {
      chunkAnnotationType = UIMAUtil.getCasType(jCas, chunkAnnotationClass);
      labeledAnnotationType = UIMAUtil.getCasType(jCas, labeledAnnotationClass);
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
    typesInitialized = true;
  }

  public void chunks2Labels(JCas jCas, Annotation sequence) throws AnalysisEngineProcessException {
    if (!typesInitialized)
      initializeTypes(jCas);

    annotationLabels.clear();

    FSIterator<Annotation> chunkAnnotations = jCas.getAnnotationIndex(chunkAnnotationType).subiterator(
        sequence);
    while (chunkAnnotations.hasNext()) {
      Annotation chunkAnnotation = chunkAnnotations.next();
      String labelBase = getChunkLabel(jCas, chunkAnnotation);
      String label = labelBase;

      List<? extends Annotation> labeledAnnotations = JCasUtil.selectCovered(
          jCas,
          labeledAnnotationClass,
          chunkAnnotation);

      boolean begin = true;
      for (Annotation labelAnnotation : labeledAnnotations) {
        if (begin)
          label = BEGIN_PREFIX + SEPARATOR + labelBase;
        else
          label = INSIDE_PREFIX + SEPARATOR + labelBase;
        begin = false;
        setLabel(labelAnnotation, label);
      }
    }
  }

  public List<Annotation> labels2Chunks(JCas jCas, Annotation sequence)
      throws AnalysisEngineProcessException {
    if (!typesInitialized)
      initializeTypes(jCas);

    List<Annotation> returnValues = new ArrayList<Annotation>();
    FSIterator<Annotation> labeledAnnotations = jCas.getAnnotationIndex(labeledAnnotationType).subiterator(
        sequence);

    String currentLabelValue = null;
    List<Annotation> currentLabeledAnnotations = new ArrayList<Annotation>();

    while (labeledAnnotations.hasNext()) {
      Annotation labeledAnnotation = labeledAnnotations.next();
      String label = getLabel(labeledAnnotation);
      if (label == null)
        label = OUTSIDE_LABEL;

      String labelPrefix = label.equals(OUTSIDE_LABEL) ? null : label.substring(0, 1);
      String labelValue = label.equals(OUTSIDE_LABEL) ? null : label.substring(2);

      if (labelPrefix == null || labelPrefix.equals(BEGIN_PREFIX)
          || !labelValue.equals(currentLabelValue)) {
        if (currentLabeledAnnotations.size() > 0) {
          Annotation chunk = createChunk(jCas, currentLabeledAnnotations, currentLabelValue);
          if (chunk != null)
            returnValues.add(chunk);
        }
        currentLabeledAnnotations.clear();
        currentLabelValue = labelValue;
      }
      if (labelValue != null)
        currentLabeledAnnotations.add(labeledAnnotation);
    }

    if (currentLabeledAnnotations.size() > 0) {
      Annotation chunk = createChunk(jCas, currentLabeledAnnotations, currentLabelValue);
      if (chunk != null)
        returnValues.add(chunk);
    }
    return returnValues;
  }

  public String getLabel(Annotation labeledAnnotation) throws AnalysisEngineProcessException {
    if (annotationLabels.containsKey(labeledAnnotation))
      return annotationLabels.get(labeledAnnotation);
    return OUTSIDE_LABEL;
  }

  public void setLabel(Annotation labeledAnnotation, String label)
      throws AnalysisEngineProcessException {
    annotationLabels.put(labeledAnnotation, label);
  }

  public void setChunkAnnotationClassName(String chunkAnnotationClassName) {
    this.chunkAnnotationClassName = chunkAnnotationClassName;
  }

  public void setLabeledAnnotationClassName(String labeledAnnotationClassName) {
    this.labeledAnnotationClassName = labeledAnnotationClassName;
  }

}
