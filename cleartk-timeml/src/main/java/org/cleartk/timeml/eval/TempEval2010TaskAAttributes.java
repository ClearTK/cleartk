/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
import java.util.Arrays;

import org.cleartk.eval.provider.AnnotationEvaluator;
import org.cleartk.eval.provider.BatchBasedEvaluationPipelineProvider;
import org.cleartk.eval.provider.CleartkPipelineProvider;
import org.cleartk.eval.provider.EvaluationPipelineProvider;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.timeml.corpus.TempEval2010GoldAnnotator;
import org.cleartk.timeml.corpus.TempEval2010Writer;
import org.cleartk.timeml.time.TimeTypeAnnotator;
import org.cleartk.timeml.type.Time;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * TempEval 2010 task A: time attributes
 * 
 * Best reported accuracies in TempEval 2010:
 * <ul>
 * <li>type: 0.98</li>
 * <li>value: 0.85</li>
 * </ul>
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010TaskAAttributes extends TempEval2010Main {

  @Override
  protected CleartkPipelineProvider getCleartkPipelineProvider(
      File modelDirectory,
      File xmiDirectory) throws Exception {
    return new TempEval2010PipelineProvider(
        modelDirectory,
        xmiDirectory,
        Arrays.asList(
            TempEval2010GoldAnnotator.PARAM_TEXT_VIEWS,
            TempEval2010GoldAnnotator.PARAM_TIME_EXTENT_VIEWS),
        TempEval2010GoldAnnotator.PARAM_TIME_ATTRIBUTE_VIEWS,
        Arrays.asList(
            DefaultSnowballStemmer.getDescription("English"),
            PosTaggerAnnotator.getDescription()),
        Arrays.asList(TimeTypeAnnotator.FACTORY));
  }

  @Override
  protected EvaluationPipelineProvider getEvaluationPipelineProvider(File evalDirectory)
      throws Exception {
    return new BatchBasedEvaluationPipelineProvider(Arrays.asList(
        AnalysisEngineFactory.createPrimitive(
            TempEval2010Writer.getDescription(),
            TempEval2010Writer.PARAM_OUTPUT_DIRECTORY,
            evalDirectory.getPath(),
            TempEval2010Writer.PARAM_TEXT_VIEW,
            TempEval2010PipelineProvider.SYSTEM_VIEW_NAME,
            TempEval2010Writer.PARAM_TIME_ATTRIBUTE_VIEW,
            TempEval2010PipelineProvider.SYSTEM_VIEW_NAME),
        AnalysisEngineFactory.createPrimitive(
            AnnotationEvaluator.class,
            AnnotationEvaluator.PARAM_ANNOTATION_CLASS_NAME,
            Time.class.getName(),
            AnnotationEvaluator.PARAM_ANNOTATION_ATTRIBUTE_NAME,
            "timeType",
            AnnotationEvaluator.PARAM_GOLD_VIEW_NAME,
            TempEval2010PipelineProvider.GOLD_VIEW_NAME,
            AnnotationEvaluator.PARAM_SYSTEM_VIEW_NAME,
            TempEval2010PipelineProvider.SYSTEM_VIEW_NAME,
            AnnotationEvaluator.PARAM_IGNORE_SYSTEM_SPANS_NOT_IN_GOLD,
            true)));
  }

  public static void main(String[] args) throws Exception {
    new TempEval2010TaskAAttributes().runCommand(args);
  }
}
