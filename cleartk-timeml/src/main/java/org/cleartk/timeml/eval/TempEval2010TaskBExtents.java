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
import org.cleartk.timeml.event.EventAnnotator;
import org.cleartk.timeml.type.Event;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * TempEval 2010 task B: event extents
 * 
 * Best reported token-level scores in TempEval 2010:
 * <ul>
 * <li>precision: 0.83</li>
 * <li>recall: 0.88</li>
 * <li>F1: 0.83</li>
 * </ul>
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010TaskBExtents extends TempEval2010Main {

  @Override
  protected CleartkPipelineProvider getCleartkPipelineProvider(
      File modelDirectory,
      File xmiDirectory) throws Exception {
    return new TempEval2010PipelineProvider(
        modelDirectory,
        xmiDirectory,
        Arrays.asList(TempEval2010GoldAnnotator.PARAM_TEXT_VIEWS),
        TempEval2010GoldAnnotator.PARAM_EVENT_EXTENT_VIEWS,
        Arrays.asList(
            DefaultSnowballStemmer.getDescription("English"),
            PosTaggerAnnotator.getDescription()),
        Arrays.asList(EventAnnotator.FACTORY));
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
            TempEval2010Writer.PARAM_EVENT_EXTENT_VIEW,
            TempEval2010PipelineProvider.SYSTEM_VIEW_NAME),
        AnalysisEngineFactory.createPrimitive(
            AnnotationEvaluator.class,
            AnnotationEvaluator.PARAM_ANNOTATION_CLASS_NAME,
            Event.class.getName(),
            AnnotationEvaluator.PARAM_GOLD_VIEW_NAME,
            TempEval2010PipelineProvider.GOLD_VIEW_NAME,
            AnnotationEvaluator.PARAM_SYSTEM_VIEW_NAME,
            TempEval2010PipelineProvider.SYSTEM_VIEW_NAME)));
  }

  public static void main(String[] args) throws Exception {
    new TempEval2010TaskBExtents().runCommand(args);
  }
}
