/*
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.timeml.eval;

import java.io.File;
import java.util.Collection;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class ModelInfo<ANNOTATION_TYPE extends TOP> {

  public Class<ANNOTATION_TYPE> annotatedClass;

  public String annotatedFeatureName;

  public Function<ANNOTATION_TYPE, ? extends Object> annotationConverter;

  public CleartkInternalModelFactory modelFactory;

  public String[] trainingArguments;

  public ModelInfo(
      Class<ANNOTATION_TYPE> annotatedClass,
      String annotatedFeatureName,
      Function<ANNOTATION_TYPE, ? extends Object> annotationConverter,
      CleartkInternalModelFactory modelFactory,
      String[] trainingArguments) {
    this.annotatedClass = annotatedClass;
    this.annotatedFeatureName = annotatedFeatureName;
    this.annotationConverter = annotationConverter;
    this.modelFactory = modelFactory;
    this.trainingArguments = trainingArguments;
  }

  public File getModelSubdirectory(File directory) {
    String annotatorName = this.modelFactory.getAnnotatorClass().getName();
    return new File(new File(directory, "models"), annotatorName);
  }

  public void updateStatistics(AnnotationStatistics statistics, JCas goldView, JCas systemView) {
    Collection<ANNOTATION_TYPE> goldAnns = JCasUtil.select(goldView, this.annotatedClass);
    Collection<ANNOTATION_TYPE> systemAnns = JCasUtil.select(systemView, this.annotatedClass);
    statistics.add(
        goldAnns,
        systemAnns,
        this.annotationConverter,
        AnnotationStatistics.<ANNOTATION_TYPE> annotationToFeatureValue(this.annotatedFeatureName));
  }
}