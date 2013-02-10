/*
 * Copyright (c) 2013, Regents of the University of Colorado 
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
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.cleartk.classifier.jar.Train;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.syntax.opennlp.ParserAnnotator;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.timeml.TimeMLViewName;
import org.cleartk.timeml.corpus.TempEval2013Writer;
import org.cleartk.timeml.corpus.TimeMLGoldAnnotator;
import org.cleartk.timeml.event.EventAnnotator;
import org.cleartk.timeml.event.EventAspectAnnotator;
import org.cleartk.timeml.event.EventClassAnnotator;
import org.cleartk.timeml.event.EventModalityAnnotator;
import org.cleartk.timeml.event.EventPolarityAnnotator;
import org.cleartk.timeml.event.EventTenseAnnotator;
import org.cleartk.timeml.time.TimeAnnotator;
import org.cleartk.timeml.time.TimeTypeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToDocumentCreationTimeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToSameSentenceTimeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToSubordinatedEventAnnotator;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Text;
import org.cleartk.timeml.type.Time;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

/**
 * Trains and evaluates event, time and temporal relation models on the TempEval 2013 data.
 * 
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2013Evaluation extends
    Evaluation_ImplBase<File, TempEval2013Evaluation.ModelStats> {

  private static Function<TemporalLink, List<Integer>> TEMPORAL_LINK_TO_SPANS = new Function<TemporalLink, List<Integer>>() {
    @Override
    public List<Integer> apply(TemporalLink temporalLink) {
      return Arrays.asList(
          temporalLink.getSource().getBegin(),
          temporalLink.getSource().getEnd(),
          temporalLink.getTarget().getBegin(),
          temporalLink.getTarget().getEnd());
    }
  };

  static final Model<?>[] MODELS = new Model<?>[] {
      new Model<Time>(
          "time-extent",
          TimeAnnotator.FACTORY,
          new String[0],
          Model.EvaluationType.NORMAL,
          Time.class,
          AnnotationStatistics.<Time> annotationToSpan(),
          AnnotationStatistics.<Time, String> annotationToNull()),
      new Model<Time>(
          "time-type",
          TimeTypeAnnotator.FACTORY,
          new String[0],
          Model.EvaluationType.NORMAL,
          Time.class,
          AnnotationStatistics.<Time> annotationToSpan(),
          AnnotationStatistics.<Time> annotationToFeatureValue("timeType")),
      new Model<Event>(
          "event-extent",
          EventAnnotator.FACTORY,
          new String[] { "MaxEnt" },
          Model.EvaluationType.NORMAL,
          Event.class,
          AnnotationStatistics.<Event> annotationToSpan(),
          AnnotationStatistics.<Event, String> annotationToNull()),
      new Model<Event>(
          "event-aspect",
          EventAspectAnnotator.FACTORY,
          new String[0],
          Model.EvaluationType.NORMAL,
          Event.class,
          AnnotationStatistics.<Event> annotationToSpan(),
          AnnotationStatistics.<Event> annotationToFeatureValue("aspect")),
      new Model<Event>(
          "event-class",
          EventClassAnnotator.FACTORY,
          new String[0],
          Model.EvaluationType.NORMAL,
          Event.class,
          AnnotationStatistics.<Event> annotationToSpan(),
          AnnotationStatistics.<Event> annotationToFeatureValue("eventClass")),
      new Model<Event>(
          "event-modality",
          EventModalityAnnotator.FACTORY,
          new String[0],
          Model.EvaluationType.NORMAL,
          Event.class,
          AnnotationStatistics.<Event> annotationToSpan(),
          AnnotationStatistics.<Event> annotationToFeatureValue("modality")),
      new Model<Event>(
          "event-polarity",
          EventPolarityAnnotator.FACTORY,
          new String[0],
          Model.EvaluationType.NORMAL,
          Event.class,
          AnnotationStatistics.<Event> annotationToSpan(),
          AnnotationStatistics.<Event> annotationToFeatureValue("polarity")),
      new Model<Event>(
          "event-tense",
          EventTenseAnnotator.FACTORY,
          new String[0],
          Model.EvaluationType.NORMAL,
          Event.class,
          AnnotationStatistics.<Event> annotationToSpan(),
          AnnotationStatistics.<Event> annotationToFeatureValue("tense")),
      new Model<TemporalLink>(
          "tlink-event-doctime",
          TemporalLinkEventToDocumentCreationTimeAnnotator.FACTORY,
          new String[0],
          // links are not consistently annotated, so only evaluate precision
          Model.EvaluationType.PRECISION_ONLY,
          TemporalLink.class,
          TEMPORAL_LINK_TO_SPANS,
          AnnotationStatistics.<TemporalLink> annotationToFeatureValue("relationType")),
      new Model<TemporalLink>(
          "tlink-event-senttime",
          TemporalLinkEventToSameSentenceTimeAnnotator.FACTORY,
          new String[0],
          // links are not consistently annotated, so only evaluate precision
          Model.EvaluationType.PRECISION_ONLY,
          TemporalLink.class,
          TEMPORAL_LINK_TO_SPANS,
          AnnotationStatistics.<TemporalLink> annotationToFeatureValue("relationType")),
      new Model<TemporalLink>(
          "tlink-event-subordevent",
          TemporalLinkEventToSubordinatedEventAnnotator.FACTORY,
          new String[0],
          // links are not consistently annotated, so only evaluate precision
          Model.EvaluationType.PRECISION_ONLY,
          TemporalLink.class,
          TEMPORAL_LINK_TO_SPANS,
          AnnotationStatistics.<TemporalLink> annotationToFeatureValue("relationType")), };

  interface Options {
    
    @Option(longName = "train-dirs")
    List<File> getTrainDirectories();
    
    @Option(longName = "test-dirs")
    List<File> getTestDirectories();
    
    @Option(longName = "inferred-tlinks", defaultToNull=true)
    File getInferredTLinksFile();
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);

    List<File> trainFiles = listAllFiles(options.getTrainDirectories());
    List<File> testFiles = listAllFiles(options.getTestDirectories());

    File evalDir = new File("target/tempeval2013");
    TempEval2013Evaluation evaluation = new TempEval2013Evaluation(evalDir, options.getInferredTLinksFile());
    ModelStats modelStats;
    if (testFiles.isEmpty()) {
      List<ModelStats> foldStats = evaluation.crossValidation(trainFiles, 2);
      modelStats = ModelStats.addAll(foldStats);
    } else {
      modelStats = evaluation.trainAndTest(trainFiles, testFiles);
    }
    for (Model<?> model : MODELS) {
      System.err.printf("== %s ==\n", model.name);
      System.err.println(modelStats.get(model));
    }
  }
  
  private static List<File> listAllFiles(List<File> directories) {
    List<File> files = Lists.newArrayList();
    for (File dir : directories) {
      for (File file : dir.listFiles()) {
        files.add(file);
      }
    }
    return files;
  }
  
  private File inferredTLinksFile;

  public TempEval2013Evaluation(File baseDirectory, File inferredTLinksFile) {
    super(baseDirectory);
    this.inferredTLinksFile = inferredTLinksFile;
  }

  @Override
  protected CollectionReader getCollectionReader(List<File> files) throws Exception {
    return UriCollectionReader.getCollectionReaderFromFiles(files);
  }

  @Override
  protected void train(CollectionReader reader, File directory) throws Exception {
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        TimeMLViewName.TIMEML));
    builder.add(
        UriToDocumentTextAnnotator.getDescription(),
        CAS.NAME_DEFAULT_SOFA,
        TimeMLViewName.TIMEML);
    builder.add(TimeMLGoldAnnotator.getDescription());
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(FixTimeML.class));
    if (this.inferredTLinksFile != null) {
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(
          UseInferredTlinks.class,
          UseInferredTlinks.PARAM_INFERRED_TLINKS_DIRECTORY,
          this.inferredTLinksFile));
    }
    // only add sentences and other annotations under <TEXT>
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        SentenceAnnotator.class,
        SentenceAnnotator.PARAM_SENTENCE_MODEL_PATH,
        "/models/en-sent.bin",
        SentenceAnnotator.PARAM_WINDOW_CLASS_NAMES,
        new Class<?>[] { Text.class }));
    builder.add(TokenAnnotator.getDescription());
    builder.add(PosTaggerAnnotator.getDescription());
    builder.add(DefaultSnowballStemmer.getDescription("English"));
    builder.add(ParserAnnotator.getDescription());
    for (Model<?> model : MODELS) {
      builder.add(model.getWriterDescription(directory));
    }

    SimplePipeline.runPipeline(reader, builder.createAggregate());

    for (Model<?> model : MODELS) {
      System.err.println("Training: " + model.name);
      model.train(directory);
    }
  }

  @Override
  protected ModelStats test(CollectionReader reader, File directory) throws Exception {
    String goldViewName = "GoldView";
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        TimeMLViewName.TIMEML));
    builder.add(
        UriToDocumentTextAnnotator.getDescription(),
        CAS.NAME_DEFAULT_SOFA,
        TimeMLViewName.TIMEML);
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        goldViewName));
    builder.add(TimeMLGoldAnnotator.getDescription(), CAS.NAME_DEFAULT_SOFA, goldViewName);
    builder.add(
        AnalysisEngineFactory.createPrimitiveDescription(FixTimeML.class),
        CAS.NAME_DEFAULT_SOFA,
        goldViewName);
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        CopyTextAndDocumentCreationTime.class,
        CopyTextAndDocumentCreationTime.PARAM_SOURCE_VIEW,
        goldViewName));
    // only add sentences and other annotations under <TEXT>
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        SentenceAnnotator.class,
        SentenceAnnotator.PARAM_SENTENCE_MODEL_PATH,
        "/models/en-sent.bin",
        SentenceAnnotator.PARAM_WINDOW_CLASS_NAMES,
        new Class<?>[] { Text.class }));
    builder.add(TokenAnnotator.getDescription());
    builder.add(PosTaggerAnnotator.getDescription());
    builder.add(DefaultSnowballStemmer.getDescription("English"));
    builder.add(ParserAnnotator.getDescription());
    for (Model<?> model : MODELS) {
      builder.add(model.getAnnotatorDescription(directory));
    }
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(SetTemporalLinkIDs.class));
    builder.add(TempEval2013Writer.getDescription(new File(directory, "timeml")));

    ModelStats stats = new ModelStats();
    for (JCas jCas : new JCasIterable(reader, builder.createAggregate())) {
      JCas goldView = jCas.getView(goldViewName);
      JCas systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);

      for (Model<?> model : MODELS) {
        model.evaluate(goldView, systemView, stats.get(model));
      }
    }
    return stats;
  }

  static class ModelStats extends HashMap<Model<?>, AnnotationStatistics<String>> {
    private static final long serialVersionUID = 1L;

    public ModelStats() {
      for (Model<?> model : MODELS) {
        this.put(model, new AnnotationStatistics<String>());
      }
    }

    public static ModelStats addAll(Iterable<ModelStats> modelStatsIter) {
      ModelStats result = new ModelStats();
      for (ModelStats stats : modelStatsIter) {
        for (Model<?> model : MODELS) {
          result.get(model).addAll(stats.get(model));
        }
      }
      return result;
    }
  }

  private static class Model<ANNOTATION_TYPE extends TOP> {
    public enum EvaluationType {
      NORMAL, PRECISION_ONLY
    }

    public String name;

    private CleartkInternalModelFactory factory;

    private String[] trainingArguments;

    private EvaluationType evaluationType;

    private Class<ANNOTATION_TYPE> annotationClass;

    private Function<ANNOTATION_TYPE, ?> annotationToSpan;

    private Function<ANNOTATION_TYPE, String> annotationToOutcome;

    public Model(
        String name,
        CleartkInternalModelFactory factory,
        String[] trainingArguments,
        EvaluationType evaluationType,
        Class<ANNOTATION_TYPE> annotationClass,
        Function<ANNOTATION_TYPE, ?> annotationToSpan,
        Function<ANNOTATION_TYPE, String> annotationToOutcome) {
      this.name = name;
      this.factory = factory;
      this.trainingArguments = trainingArguments;
      this.evaluationType = evaluationType;
      this.annotationClass = annotationClass;
      this.annotationToSpan = annotationToSpan;
      this.annotationToOutcome = annotationToOutcome;
    }

    public AnalysisEngineDescription getWriterDescription(File directory)
        throws ResourceInitializationException {
      return this.factory.getWriterDescription(this.getModelDirectory(directory));
    }

    public void train(File directory) throws Exception {
      Train.main(this.getModelDirectory(directory), this.trainingArguments);
    }

    public AnalysisEngineDescription getAnnotatorDescription(File directory)
        throws ResourceInitializationException {
      return this.factory.getAnnotatorDescription(new File(
          this.getModelDirectory(directory),
          "model.jar").getPath());
    }

    public void evaluate(JCas goldView, JCas systemView, AnnotationStatistics<String> stats) {
      switch (this.evaluationType) {
        case PRECISION_ONLY:
          Collection<ANNOTATION_TYPE> goldRelations = JCasUtil.select(
              goldView,
              this.annotationClass);
          Set<Object> goldSpans = Sets.newHashSet();
          for (ANNOTATION_TYPE annotation : goldRelations) {
            goldSpans.add(this.annotationToSpan.apply(annotation));
          }
          List<ANNOTATION_TYPE> systemRelations = Lists.newArrayList();
          for (ANNOTATION_TYPE annotation : JCasUtil.select(systemView, this.annotationClass)) {
            if (goldSpans.contains(this.annotationToSpan.apply(annotation))) {
              systemRelations.add(annotation);
            }
          }
          stats.add(goldRelations, systemRelations, this.annotationToSpan, this.annotationToOutcome);
          break;
        case NORMAL:
          stats.add(
              JCasUtil.select(goldView, this.annotationClass),
              JCasUtil.select(systemView, this.annotationClass),
              this.annotationToSpan,
              this.annotationToOutcome);
      }
    }

    private File getModelDirectory(File directory) {
      return new File(directory, this.name);
    }
  }

  public static class CopyTextAndDocumentCreationTime extends JCasAnnotator_ImplBase {

    public static final String PARAM_SOURCE_VIEW = "SourceView";

    @ConfigurationParameter(name = PARAM_SOURCE_VIEW)
    private String sourceViewName;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      JCas sourceView;
      try {
        sourceView = jCas.getView(this.sourceViewName);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
      CasCopier copier = new CasCopier(sourceView.getCas(), jCas.getCas());
      Feature sofaFeature = jCas.getTypeSystem().getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);

      // copy document text
      jCas.setDocumentText(sourceView.getDocumentText());

      // copy text annotation
      Text sourceText = JCasUtil.selectSingle(sourceView, Text.class);
      Text text = (Text) copier.copyFs(sourceText);
      text.setFeatureValue(sofaFeature, jCas.getSofa());
      text.addToIndexes();

      // copy document creation time
      DocumentCreationTime sourceTime = JCasUtil.selectSingle(
          sourceView,
          DocumentCreationTime.class);
      DocumentCreationTime time = (DocumentCreationTime) copier.copyFs(sourceTime);
      time.setFeatureValue(sofaFeature, jCas.getSofa());
      time.addToIndexes();
    }
  }

  public static class FixTimeML extends JCasAnnotator_ImplBase {

    private static Set<String> IS_SIMULTANEOUS = Sets.newHashSet("DURING", "DURING_INV", "IDENTITY");

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      // add missing event attributes
      for (Event event : JCasUtil.select(jCas, Event.class)) {
        if (event.getAspect() == null) {
          event.setAspect("NONE");
        }
        if (event.getModality() == null) {
          event.setModality("none");
        }
        String modality = event.getModality();
        event.setModality(modality.toLowerCase().replaceAll("_", " ").replaceAll("^'d$", "would"));
        if (event.getPolarity() == null) {
          event.setPolarity("POS");
        }
        if (event.getTense() == null) {
          event.setTense("NONE");
        }
      }

      // simplify simultaneous relations
      for (TemporalLink tlink : JCasUtil.select(jCas, TemporalLink.class)) {
        if (IS_SIMULTANEOUS.contains(tlink.getRelationType())) {
          tlink.setRelationType("SIMULTANEOUS");
        }
      }
    }
  }
  
  public static class SetTemporalLinkIDs extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      int index = 1;
      for (TemporalLink tlink : JCasUtil.select(jCas, TemporalLink.class)) {
        tlink.setId(String.format("l%d", index));
        ++index;
      }
      
    }
  }
  
  public static class UseInferredTlinks extends JCasAnnotator_ImplBase {
    
    public static final String PARAM_INFERRED_TLINKS_DIRECTORY = "InferredTLinksDirectory";
    @ConfigurationParameter(name = PARAM_INFERRED_TLINKS_DIRECTORY, mandatory=true)
    private File inferredTLinksDirectory;
    
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      String fileName = new File(ViewURIUtil.getURI(jCas).getPath()).getName();
      String suffix = this.inferredTLinksDirectory.getName();
      String inferredFileName = fileName.replaceAll("[.]tml$", "." + suffix + ".tml");
      File inferredTLinksFile = new File(this.inferredTLinksDirectory, inferredFileName);
      
      if (!inferredTLinksFile.exists()) {
        this.getLogger().warn("No inferred TLINKs file " + inferredTLinksFile);
      } else {
        
        // remove existing temporal links
        for (TemporalLink tlink : Lists.newArrayList(JCasUtil.select(jCas, TemporalLink.class))) {
          tlink.removeFromIndexes();
        }

        // parse the XML document
        SAXBuilder builder = new SAXBuilder();
        Document xml;
        try {
          xml = builder.build(inferredTLinksFile);
        } catch (JDOMException e) {
          throw new AnalysisEngineProcessException(e);
        } catch (IOException e) {
          throw new AnalysisEngineProcessException(e);
        }
        
        // index all anchors by their IDs
        Map<String, Anchor> idToAnchor = Maps.newHashMap();
        for (Anchor anchor : JCasUtil.select(jCas, Anchor.class)) {
          idToAnchor.put(anchor.getId(), anchor);
          if (anchor instanceof Event) {
            idToAnchor.put(((Event) anchor).getEventInstanceID(), anchor);
          }
        }
        
        // create a TemporalLink for each TLINK in the file
        int offset = jCas.getDocumentText().length();
        for (Element linkElem : xml.getDescendants(Filters.element("TLINK"))) {
          // get the relation
          String relationType = linkElem.getAttributeValue("relType");
          if (relationType == null) {
            error(jCas, linkElem, "No relation type specified in %s");
          }
          
          // get the source
          String sourceEventID = linkElem.getAttributeValue("eventInstanceID");
          String sourceTimeID = linkElem.getAttributeValue("timeID");
          if (!(sourceEventID == null ^ sourceTimeID == null)) {
            error(jCas, linkElem, "Expected exactly 1 source attribute, found %s");
          }
          String sourceID = sourceEventID != null ? sourceEventID : sourceTimeID;
          Anchor source = idToAnchor.get(sourceID);
          if (source == null) {
            this.getLogger().warn(errorString(jCas, linkElem, "No annotation found for source of %s"));
            continue;
          }
          
          // get the target
          String targetEventID = linkElem.getAttributeValue("relatedToEventInstance");
          String targetTimeID = linkElem.getAttributeValue("relatedToTime");
          if (!(targetEventID == null ^ targetTimeID == null)) {
            error(jCas, linkElem, "Expected exactly 1 target attribute, found %s");
          }
          String targetID = targetEventID != null ? targetEventID : targetTimeID;
          Anchor target = idToAnchor.get(targetID);
          if (target == null) {
            this.getLogger().warn(errorString(jCas, linkElem, "No annotation found for target of %s"));
            continue;
          }
          
          // add the temporal link
          TemporalLink link = new TemporalLink(jCas, offset, offset);
          link.setRelationType(relationType);
          link.setSource(source);
          link.setTarget(target);
          link.addToIndexes();
        }
      }
    }

    private static String errorString(JCas jCas, Element element, String message) throws AnalysisEngineProcessException {
      URI uri = ViewURIUtil.getURI(jCas);
      String elemString = new XMLOutputter().outputString(element);
      return String.format("In %s: " + message, uri, elemString);
    }
    
    private static void error(JCas jCas, Element element, String message) throws AnalysisEngineProcessException {
      throw new IllegalArgumentException(errorString(jCas, element, message));
    }
  }
}