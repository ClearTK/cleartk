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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkSequenceAnnotator;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.eval.provider.CleartkPipelineProvider_ImplBase;
import org.cleartk.timeml.corpus.TempEval2010GoldAnnotator;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.xml.sax.SAXException;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010PipelineProvider extends CleartkPipelineProvider_ImplBase {

  public static final String GOLD_VIEW_NAME = "GoldView";

  public static final String SYSTEM_VIEW_NAME = CAS.NAME_DEFAULT_SOFA;

  private File modelDirectory;

  private File xmiDirectory;

  private String annotatedViewParam;

  private Set<String> requiredViewParam;

  private List<String> viewParams;

  private AnalysisEngine goldAnnotator;

  private AnalysisEngine xmiAnnotator;

  private Map<CleartkInternalModelFactory, AnalysisEngine> systemAnnotators;

  private List<AnalysisEngine> pipeline;

  public TempEval2010PipelineProvider(
      File modelDirectory,
      File xmiDirectory,
      Collection<String> requiredViewParams,
      String annotatedViewParam,
      Collection<AnalysisEngineDescription> preprocessingAnnotators,
      List<CleartkInternalModelFactory> systemAnnotatorFactories)
      throws ResourceInitializationException {
    this.modelDirectory = modelDirectory;
    int dotIndex = Math.max(0, annotatedViewParam.lastIndexOf('.'));
    String xmiDirName = annotatedViewParam.substring(dotIndex + 1);
    this.xmiDirectory = new File(xmiDirectory, xmiDirName);

    // set up TempEval2010GoldAnnotator view parameters
    this.annotatedViewParam = annotatedViewParam;
    this.requiredViewParam = new HashSet<String>(requiredViewParams);
    this.viewParams = Arrays.asList(
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

    // set up the pipeline, loading from XMI files when possible
    this.goldAnnotator = AnalysisEngineFactory.createPrimitive(TempEval2010GoldAnnotator.getDescription());
    this.systemAnnotators = new HashMap<CleartkInternalModelFactory, AnalysisEngine>();
    for (CleartkInternalModelFactory factory : systemAnnotatorFactories) {
      File outputDirectory = this.getOutputDirectory("", factory);
      AnalysisEngineDescription desc = factory.getWriterDescription(outputDirectory);
      AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(desc);
      this.systemAnnotators.put(factory, engine);
    }
    this.pipeline = new ArrayList<AnalysisEngine>();
    if (!this.xmiDirectory.exists()) {
      this.xmiAnnotator = AnalysisEngineFactory.createPrimitive(
          XMIWriter.class,
          XMIAnnotator.PARAM_XMI_DIRECTORY,
          this.xmiDirectory.getPath());
      this.pipeline.add(this.goldAnnotator);
      for (AnalysisEngineDescription desc : preprocessingAnnotators) {
        this.pipeline.add(AnalysisEngineFactory.createPrimitive(desc));
      }
      this.pipeline.add(this.xmiAnnotator);
    } else {
      this.xmiAnnotator = AnalysisEngineFactory.createPrimitive(
          XMIReader.class,
          XMIAnnotator.PARAM_XMI_DIRECTORY,
          this.xmiDirectory.getPath());
      this.pipeline.add(this.xmiAnnotator);
    }
    this.pipeline.addAll(this.systemAnnotators.values());
  }

  @Override
  public List<AnalysisEngine> getTrainingPipeline(String runName) throws UIMAException {
    // reconfigure xmi annotator to use training CASes
    String dir = new File(this.xmiDirectory, "training").getPath();
    this.xmiAnnotator.setConfigParameterValue(XMIAnnotator.PARAM_XMI_DIRECTORY, dir);
    this.xmiAnnotator.reconfigure();

    // reconfigure gold annotator to read annotations into system view
    for (String viewParam : this.viewParams) {
      if (viewParam.equals(this.annotatedViewParam) || this.requiredViewParam.contains(viewParam)) {
        this.goldAnnotator.setConfigParameterValue(viewParam, new String[] { SYSTEM_VIEW_NAME });
      } else {
        this.goldAnnotator.setConfigParameterValue(viewParam, new String[] {});
      }
    }
    this.goldAnnotator.reconfigure();

    // reconfigure system annotators to write training data
    for (CleartkInternalModelFactory factory : this.systemAnnotators.keySet()) {
      AnalysisEngine systemAnnotator = this.systemAnnotators.get(factory);
      systemAnnotator.setConfigParameterValue(CleartkAnnotator.PARAM_IS_TRAINING, true);
      systemAnnotator.setConfigParameterValue(CleartkSequenceAnnotator.PARAM_IS_TRAINING, true);
      systemAnnotator.setConfigParameterValue(
          DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
          this.getOutputDirectory(runName, factory).getPath());
      systemAnnotator.reconfigure();
    }

    // return the configured pipeline
    return this.pipeline;
  }

  @Override
  public void train(String runName, String... trainingArguments) throws Exception {
    Logger.getLogger("cc.mallet").setLevel(Level.WARNING);
    Logger.getLogger("opennlp").setLevel(Level.WARNING);
    // don't use HideOutput - overriding System.err screws up the logging for some reason
    PrintStream oldOut = System.out;
    System.setOut(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        // write nothing
      }
    }));
    try {
      for (CleartkInternalModelFactory factory : this.systemAnnotators.keySet()) {
        File modelDir = this.getOutputDirectory(runName, factory);
        JarClassifierBuilder.trainAndPackage(modelDir, trainingArguments);
      }
    } finally {
      System.setOut(oldOut);
    }
    // if building to the pre-defined training directory, clean up non-model files
    for (CleartkInternalModelFactory factory : this.systemAnnotators.keySet()) {
      File modelDir = factory.getTrainingDirectory();
      if (modelDir.exists()) {
        for (File file : modelDir.listFiles()) {
          if (!file.isDirectory() && !file.getName().equals("model.jar")) {
            file.delete();
          }
        }
      }
    }
  }

  @Override
  public List<AnalysisEngine> getClassifyingPipeline(String runName) throws UIMAException {
    // reconfigure xmi annotator to use classifying CASes
    String dir = new File(this.xmiDirectory, "classifying").getPath();
    this.xmiAnnotator.setConfigParameterValue(XMIAnnotator.PARAM_XMI_DIRECTORY, dir);
    this.xmiAnnotator.reconfigure();

    // reconfigure gold annotator to read annotations into gold view
    for (String viewParam : this.viewParams) {
      if (viewParam.equals(this.annotatedViewParam)) {
        this.goldAnnotator.setConfigParameterValue(viewParam, new String[] { GOLD_VIEW_NAME });
      } else if (this.requiredViewParam.contains(viewParam)) {
        this.goldAnnotator.setConfigParameterValue(viewParam, new String[] {
            SYSTEM_VIEW_NAME,
            GOLD_VIEW_NAME });
      } else {
        this.goldAnnotator.setConfigParameterValue(viewParam, new String[] {});
      }
    }
    this.goldAnnotator.reconfigure();

    // reconfigure system annotators to load model files
    for (CleartkInternalModelFactory factory : this.systemAnnotators.keySet()) {
      AnalysisEngine systemAnnotator = this.systemAnnotators.get(factory);
      File modelDir = this.getOutputDirectory(runName, factory);
      systemAnnotator.setConfigParameterValue(CleartkAnnotator.PARAM_IS_TRAINING, false);
      systemAnnotator.setConfigParameterValue(CleartkSequenceAnnotator.PARAM_IS_TRAINING, false);
      File modelFile;
      try {
        modelFile = JarClassifierBuilder.fromTrainingDirectory(modelDir).getModelJarFile(modelDir);
      } catch (IOException e) {
        throw new UIMAException(e);
      }
      systemAnnotator.setConfigParameterValue(
          GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
          modelFile.getPath());
      systemAnnotator.reconfigure();
    }

    // return the configured pipeline
    return this.pipeline;
  }

  private File getOutputDirectory(String runName, CleartkInternalModelFactory factory) {
    if (this.modelDirectory == null) {
      return factory.getTrainingDirectory();
    } else {
      String annotatorName = factory.getAnnotatorClass().getName();
      return new File(new File(this.modelDirectory, annotatorName), runName);
    }
  }

  public static abstract class XMIAnnotator extends JCasAnnotator_ImplBase {

    @ConfigurationParameter(mandatory = true)
    protected File xmiDirectory;

    public static final String PARAM_XMI_DIRECTORY = ConfigurationParameterFactory.createConfigurationParameterName(
        XMIAnnotator.class,
        "xmiDirectory");

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