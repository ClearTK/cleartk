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

import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class DefaultChunkLabeler extends ChunkLabeler_ImplBase {

  public static final String PARAM_CHUNK_LABEL_FEATURE_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(DefaultChunkLabeler.class, "chunkLabelFeatureName");

  private static final String CHUNK_LABEL_FEATURE_DESCRIPTION = "names  the feature of the type system chunk type that provides a label for each "
      + "chunk. The feature is queried and the value of the feature is used as the label for the chunk.  If this parameter has no value, then the name of the "
      + "chunk type will be used as a label. For example, if the value of the parameter 'org.cleartk.chunk.ChunkLabeler_ImplBase.chunkAnnotationClassName' is 'org.cleartk.type.Chunk', "
      + "then a good value for this parameter would be 'chunkType'.  This would result in labels corresponding to the values found in the type system feature "
      + "chunkType.  If the value of the parameter ''org.cleartk.chunk.ChunkLabeler_ImplBase.chunkAnnotationClassName'' is 'org.cleartk.type.Chunk' and no value is given for "
      + "this parameter, then the label will always be 'Chunk'";

  @ConfigurationParameter(description = CHUNK_LABEL_FEATURE_DESCRIPTION)
  private String chunkLabelFeatureName;

  public void setChunkLabelFeatureName(String chunkLabelFeatureName) {
    this.chunkLabelFeatureName = chunkLabelFeatureName;
  }

  private org.apache.uima.cas.Feature chunkLabelFeature;

  Constructor<? extends Annotation> chunkAnnotationConstructor;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    if (chunkLabelFeatureName != null) {
      chunkLabelFeatureName = chunkAnnotationClass.getCanonicalName() + ":" + chunkLabelFeatureName;
    }

    try {
      chunkAnnotationConstructor = chunkAnnotationClass.getConstructor(new Class[] {
          JCas.class,
          java.lang.Integer.TYPE,
          java.lang.Integer.TYPE });
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  protected void initializeTypes(JCas jCas) throws AnalysisEngineProcessException {
    super.initializeTypes(jCas);
    try {
      if (chunkLabelFeatureName != null) {
        chunkLabelFeature = jCas.getTypeSystem().getFeatureByFullName(chunkLabelFeatureName);
        if (chunkLabelFeature == null)
          throw new AnalysisEngineProcessException("type feature for name '"
              + chunkLabelFeatureName + "' not found.  ", null);
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  public Annotation createChunk(
      JCas jCas,
      List<? extends Annotation> labeledAnnotations,
      String label) throws AnalysisEngineProcessException {
    try {
      int begin = labeledAnnotations.get(0).getBegin();
      int end = labeledAnnotations.get(labeledAnnotations.size() - 1).getEnd();
      Annotation annotation = chunkAnnotationConstructor.newInstance(new Object[] {
          jCas,
          begin,
          end });

      if (chunkLabelFeature != null)
        annotation.setFeatureValueFromString(chunkLabelFeature, label);
      annotation.addToIndexes();
      return annotation;
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  @Override
  public String getChunkLabel(JCas jCas, Annotation chunkAnnotation)
      throws AnalysisEngineProcessException {
    if (!typesInitialized)
      initializeTypes(jCas);

    if (chunkLabelFeature != null)
      return chunkAnnotation.getFeatureValueAsString(chunkLabelFeature);
    else
      return chunkAnnotationClass.getSimpleName();
  }
}
