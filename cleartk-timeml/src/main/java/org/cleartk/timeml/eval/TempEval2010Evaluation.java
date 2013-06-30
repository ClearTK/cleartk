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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.corpus.timeml.TempEval2010CollectionReader;
import org.cleartk.corpus.timeml.TempEval2010GoldAnnotator;
import org.cleartk.corpus.timeml.TempEval2010Writer;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.xml.sax.SAXException;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010Evaluation extends
    Evaluation_ImplBase<String, Map<ModelInfo<?>, AnnotationStatistics<String>>> {

  public static final String GOLD_VIEW_NAME = "GoldView";

  public static final String SYSTEM_VIEW_NAME = CAS.NAME_DEFAULT_SOFA;

  private File trainDir;

  private File testDir;

  private List<File> dataDirs;

  private List<String> goldAnnotatorParamsForViewsRequiredBySystem;

  private String goldAnnotatorParamForViewAnnotatedBySystem;

  private String timemlWriterParamForViewAnnotatedBySystem;

  private List<AnalysisEngineDescription> preprocessingAnnotators;

  private List<? extends ModelInfo<?>> modelInfos;

  public TempEval2010Evaluation(
      File trainDir,
      File testDir,
      File outputDirectory,
      List<String> goldAnnotatorParamsForViewsRequiredBySystem,
      String goldAnnotatorParamForViewAnnotatedBySystem,
      String timemlWriterParamForViewAnnotatedBySystem,
      List<AnalysisEngineDescription> preprocessingAnnotators,
      List<? extends ModelInfo<?>> modelInfos) throws Exception {
    super(outputDirectory);

    this.trainDir = trainDir;
    this.testDir = testDir;
    this.dataDirs = Arrays.asList(trainDir, testDir);
    this.goldAnnotatorParamsForViewsRequiredBySystem = goldAnnotatorParamsForViewsRequiredBySystem;
    this.goldAnnotatorParamForViewAnnotatedBySystem = goldAnnotatorParamForViewAnnotatedBySystem;
    this.timemlWriterParamForViewAnnotatedBySystem = timemlWriterParamForViewAnnotatedBySystem;
    this.preprocessingAnnotators = preprocessingAnnotators;
    this.modelInfos = modelInfos;
  }

  @Override
  protected CollectionReader getCollectionReader(List<String> items) throws Exception {
    return TempEval2010CollectionReader.getCollectionReader(this.dataDirs, new HashSet<String>(
        items));
  }

  @Override
  protected void train(CollectionReader collectionReader, File directory) throws Exception {

    // run the XMI reader and the classifier data writers
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        XMIReader.class,
        XMIAnnotator.PARAM_XMI_DIRECTORY,
        this.getXMIDirectory(directory, Stage.TRAIN).getPath()));
    for (ModelInfo<?> modelInfo : this.modelInfos) {
      File outputDir = modelInfo.getModelSubdirectory(directory);
      builder.add(modelInfo.modelFactory.getWriterDescription(outputDir));
    }
    SimplePipeline.runPipeline(collectionReader, builder.createAggregateDescription());

    // train the classifiers
    for (ModelInfo<?> modelInfo : this.modelInfos) {
      File modelDir = modelInfo.getModelSubdirectory(directory);
      JarClassifierBuilder.trainAndPackage(modelDir, modelInfo.trainingArguments);
    }

    // if building to the pre-defined training directory, clean up non-model files
    for (ModelInfo<?> modelInfo : this.modelInfos) {
      File modelDir = modelInfo.modelFactory.getTrainingDirectory();
      if (modelDir.exists()) {
        for (File file : modelDir.listFiles()) {
          File modelFile = JarClassifierBuilder.getModelJarFile(modelDir);
          if (!file.isDirectory() && !file.equals(modelFile)) {
            file.delete();
          }
        }
      }
    }
  }

  @Override
  protected Map<ModelInfo<?>, AnnotationStatistics<String>> test(
      CollectionReader collectionReader,
      File directory) throws Exception {
    // prepare the XMI reader, the classifiers and the TempEval writer
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        XMIReader.class,
        XMIAnnotator.PARAM_XMI_DIRECTORY,
        this.getXMIDirectory(directory, Stage.TEST).getPath()));
    for (ModelInfo<?> modelInfo : this.modelInfos) {
      File modelFile = JarClassifierBuilder.getModelJarFile(modelInfo.getModelSubdirectory(directory));
      builder.add(modelInfo.modelFactory.getAnnotatorDescription(modelFile.getPath()));
    }
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        TempEval2010Writer.class,
        TempEval2010Writer.PARAM_OUTPUT_DIRECTORY,
        new File(directory, "eval").getPath(),
        TempEval2010Writer.PARAM_TEXT_VIEW,
        SYSTEM_VIEW_NAME,
        this.timemlWriterParamForViewAnnotatedBySystem,
        SYSTEM_VIEW_NAME));

    // create statistics for each feature that is classified
    Map<ModelInfo<?>, AnnotationStatistics<String>> modelInfoToStatistics;
    modelInfoToStatistics = new HashMap<ModelInfo<?>, AnnotationStatistics<String>>();
    for (ModelInfo<?> modelInfo : this.modelInfos) {
      modelInfoToStatistics.put(modelInfo, new AnnotationStatistics<String>());
    }

    // gather statistics over all the CASes in the test set
    AnalysisEngine engine = builder.createAggregate();
    for (JCas jCas : new JCasIterable(collectionReader, engine)) {
      JCas goldView = jCas.getView(GOLD_VIEW_NAME);
      JCas systemView = jCas.getView(SYSTEM_VIEW_NAME);
      for (ModelInfo<?> modelInfo : this.modelInfos) {
        AnnotationStatistics<String> statistics = modelInfoToStatistics.get(modelInfo);
        modelInfo.updateStatistics(statistics, goldView, systemView);
      }
    }
    engine.collectionProcessComplete();
    return modelInfoToStatistics;
  }

  private enum Stage {
    TRAIN, TEST
  }

  private File getXMIDirectory(File directory, Stage stage) throws Exception {
    int dotIndex = Math.max(0, this.goldAnnotatorParamForViewAnnotatedBySystem.lastIndexOf('.'));
    String name = this.goldAnnotatorParamForViewAnnotatedBySystem.substring(dotIndex + 1);
    File xmiDirectory = new File(new File(new File(directory, "xmi"), name), stage.toString());

    // create XMIs if necessary
    if (!xmiDirectory.exists()) {
      Set<String> fileNames = new HashSet<String>();
      fileNames.addAll(TempEval2010CollectionReader.getAnnotatedFileNames(this.trainDir));
      fileNames.addAll(TempEval2010CollectionReader.getAnnotatedFileNames(this.testDir));
      CollectionReader reader = TempEval2010CollectionReader.getCollectionReader(
          this.dataDirs,
          fileNames);

      List<String> viewParams = Arrays.asList(
          TempEval2010GoldAnnotator.PARAM_TEXT_VIEWS,
          TempEval2010GoldAnnotator.PARAM_DOCUMENT_CREATION_TIME_VIEWS,
          TempEval2010GoldAnnotator.PARAM_TIME_EXTENT_VIEWS,
          TempEval2010GoldAnnotator.PARAM_TIME_ATTRIBUTE_VIEWS,
          TempEval2010GoldAnnotator.PARAM_EVENT_EXTENT_VIEWS,
          TempEval2010GoldAnnotator.PARAM_EVENT_ATTRIBUTE_VIEWS,
          TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_DOCUMENT_CREATION_TIME_VIEWS,
          TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_SAME_SENTENCE_TIME_VIEWS,
          TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_EVENT_TO_SUBORDINATED_EVENT_VIEWS,
          TempEval2010GoldAnnotator.PARAM_TEMPORAL_LINK_MAIN_EVENT_TO_NEXT_SENTENCE_MAIN_EVENT_VIEWS);

      // determine the view parameters for the gold annotator
      List<Object> goldAnnotatorParams = new ArrayList<Object>();
      for (String viewParam : viewParams) {
        goldAnnotatorParams.add(viewParam);
        String[] paramValue;
        switch (stage) {
          case TRAIN:
            // during training, put all required annotations, and the annotations from which the
            // models will be trained, in the system view
            paramValue = this.goldAnnotatorParamsForViewsRequiredBySystem.contains(viewParam)
                || viewParam.equals(this.goldAnnotatorParamForViewAnnotatedBySystem)
                ? new String[] { SYSTEM_VIEW_NAME }
                : new String[] {};
            break;

          case TEST:
            // during testing, put required annotation in both views, and the annotations which the
            // model is supposed to predict only in the gold view
            if (this.goldAnnotatorParamsForViewsRequiredBySystem.contains(viewParam)) {
              paramValue = new String[] { SYSTEM_VIEW_NAME, GOLD_VIEW_NAME };
            } else if (viewParam.equals(this.goldAnnotatorParamForViewAnnotatedBySystem)) {
              paramValue = new String[] { GOLD_VIEW_NAME };
            } else {
              paramValue = new String[] {};
            }
            break;

          default:
            throw new IllegalArgumentException();
        }
        goldAnnotatorParams.add(paramValue);
      }

      // run the gold annotator, the preprocessing annotators, and the XMI writer
      AggregateBuilder builder = new AggregateBuilder();
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(
          TempEval2010GoldAnnotator.class,
          goldAnnotatorParams.toArray()));
      for (AnalysisEngineDescription desc : this.preprocessingAnnotators) {
        builder.add(desc);
      }
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(
          XMIWriter.class,
          XMIAnnotator.PARAM_XMI_DIRECTORY,
          xmiDirectory.getPath()));
      SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
    }

    return xmiDirectory;
  }

  public static abstract class XMIAnnotator extends JCasAnnotator_ImplBase {

    @ConfigurationParameter(
        name = PARAM_XMI_DIRECTORY,
        mandatory = true)
    protected File xmiDirectory;

    public static final String PARAM_XMI_DIRECTORY = "xmiDirectory";

    protected File getFile(JCas jCas) throws AnalysisEngineProcessException {
      return new File(this.xmiDirectory, ViewURIUtil.getURI(jCas).getFragment() + ".xmi");
    }

  }

  public static class XMIReader extends XMIAnnotator {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      try {
        FileInputStream stream = new FileInputStream(this.getFile(jCas));
        try {
          XmiCasDeserializer.deserialize(stream, jCas.getCas());
        } finally {
          stream.close();
        }
      } catch (SAXException e) {
        throw new AnalysisEngineProcessException(e);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }

  public static class XMIWriter extends XMIAnnotator {

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      if (!this.xmiDirectory.exists()) {
        this.xmiDirectory.mkdirs();
      }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      XmiCasSerializer ser = new XmiCasSerializer(jCas.getTypeSystem());
      try {
        FileOutputStream stream = new FileOutputStream(this.getFile(jCas));
        try {
          ser.serialize(jCas.getCas(), new XMLSerializer(stream, false).getContentHandler());
        } finally {
          stream.close();
        }
      } catch (SAXException e) {
        throw new AnalysisEngineProcessException(e);
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }
}