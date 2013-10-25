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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
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
import org.cleartk.classifier.liblinear.LibLinearStringOutcomeDataWriter;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.eval.Evaluation_ImplBase;
import org.cleartk.syntax.opennlp.ParserAnnotator;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.corpus.timeml.PlainTextTlinkGoldAnnotator;
import org.cleartk.corpus.timeml.TempEval2013Writer;
import org.cleartk.corpus.timeml.TimeMlGoldAnnotator;
import org.cleartk.timeml.event.EventAnnotator;
import org.cleartk.timeml.event.EventAspectAnnotator;
import org.cleartk.timeml.event.EventClassAnnotator;
import org.cleartk.timeml.event.EventModalityAnnotator;
import org.cleartk.timeml.event.EventPolarityAnnotator;
import org.cleartk.timeml.event.EventTenseAnnotator;
import org.cleartk.timeml.time.TimeAnnotator;
import org.cleartk.timeml.time.TimeTypeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkAnnotator_ImplBase;
import org.cleartk.timeml.tlink.TemporalLinkEventToDocumentCreationTimeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToSameSentenceTimeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToSubordinatedEventAnnotator;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.type.Text;
import org.cleartk.timeml.type.Time;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.ViewUriUtil;
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
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
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
public class TempEval2013Evaluation
    extends
    Evaluation_ImplBase<File, ImmutableTable<Model<?>, Model.Params, AnnotationStatistics<String>>> {

  interface Options {

    @Option(longName = "train-dirs")
    List<File> getTrainDirectories();

    @Option(longName = "test-dirs", defaultToNull = true)
    List<File> getTestDirectories();

    @Option(longName = "inferred-tlinks", defaultToNull = true)
    List<File> getInferredTLinksDirectories();

    @Option(longName = "verb-clause-tlinks")
    boolean getVerbClauseTLinks();
    
    @Option(longName = "relations-only")
    boolean getRelationsOnly();

    @Option(longName = "tune", defaultToNull = true)
    String getNameOfModelToTune();
    
    @Option(longName = "train-only")
    boolean getTrainOnly();
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);

    List<File> trainFiles = listAllFiles(options.getTrainDirectories());
    List<File> testFiles = listAllFiles(options.getTestDirectories());

    // map names to models
    List<Model<?>> allModels = Lists.<Model<?>> newArrayList(
        TIME_EXTENT_MODEL,
        TIME_TYPE_MODEL,
        EVENT_EXTENT_MODEL,
        EVENT_ASPECT_MODEL,
        EVENT_CLASS_MODEL,
        EVENT_MODALITY_MODEL,
        EVENT_POLARITY_MODEL,
        EVENT_TENSE_MODEL,
        TLINK_EVENT_DOCTIME_MODEL,
        TLINK_EVENT_SENTTIME_MODEL,
        TLINK_EVENT_SUBORDEVENT_MODEL);
    Map<String, Model<?>> nameToModel = Maps.newHashMap();
    for (Model<?> model : allModels) {
      nameToModel.put(model.name, model);
    }

    // determine which parameters each model should be trained with
    ImmutableMultimap.Builder<Model<?>, Model.Params> modelsBuilder = ImmutableMultimap.builder();
    String nameOfModelToTune = options.getNameOfModelToTune();
    if (nameOfModelToTune == null) {
      for (Model<?> model : allModels) {
        if (!options.getRelationsOnly() || model.name.startsWith("tlink")) {
          modelsBuilder.put(model, model.bestParams);
        }
      }
    } else {
      Model<?> modelToTune = nameToModel.get(nameOfModelToTune);
      if (modelToTune == null) {
        throw new IllegalArgumentException("No such model: " + nameOfModelToTune);
      }
      for (Model<?> model : getSortedPrerequisites(modelToTune)) {
        if (!options.getRelationsOnly() || model.name.startsWith("tlink")) {
          modelsBuilder.put(model, model.bestParams);
        }
      }
      for (Model.Params params : modelToTune.paramsToSearch) {
        modelsBuilder.put(modelToTune, params);
      }
    }
    ImmutableMultimap<Model<?>, Model.Params> models = modelsBuilder.build();

    // create the evaluation manager
    File evalDir = new File("target/tempeval2013");
    TempEval2013Evaluation evaluation = new TempEval2013Evaluation(
        evalDir,
        models,
        options.getInferredTLinksDirectories(),
        options.getVerbClauseTLinks(),
        options.getRelationsOnly());

    // just train a model
    if (options.getTrainOnly()) {
      if (!testFiles.isEmpty()) {
        throw new IllegalArgumentException("Cannot specify test files when only training");
      }
      evaluation.train(evaluation.getCollectionReader(trainFiles), Model.DEFAULT_DIRECTORY);
      for (Model<?> model : models.keySet()) {
        for (Model.Params params : models.get(model)) {
          model.cleanTrainingFiles(Model.DEFAULT_DIRECTORY, params);
        }
      }
    } else {

      // run a simple train-and-test
      ImmutableTable<Model<?>, Model.Params, AnnotationStatistics<String>> modelStats;
      if (!testFiles.isEmpty()) {
        modelStats = evaluation.trainAndTest(trainFiles, testFiles);
      }
  
      // run a cross-validation
      else {
        List<ImmutableTable<Model<?>, Model.Params, AnnotationStatistics<String>>> foldStats;
        foldStats = evaluation.crossValidation(trainFiles, 2);
  
        // prepare a table of stats for all models and parameters
        ImmutableTable.Builder<Model<?>, Model.Params, AnnotationStatistics<String>> modelStatsBuilder = ImmutableTable.builder();
        for (Model<?> model : models.keySet()) {
          for (Model.Params params : models.get(model)) {
            modelStatsBuilder.put(model, params, new AnnotationStatistics<String>());
          }
        }
        modelStats = modelStatsBuilder.build();
  
        // combine all fold stats into a single overall stats
        for (Table<Model<?>, Model.Params, AnnotationStatistics<String>> foldTable : foldStats) {
          for (Table.Cell<Model<?>, Model.Params, AnnotationStatistics<String>> cell : foldTable.cellSet()) {
            modelStats.get(cell.getRowKey(), cell.getColumnKey()).addAll(cell.getValue());
          }
        }
      }
  
      // print out all model performance
      for (Model<?> model : models.keySet()) {
        for (Model.Params params : modelStats.row(model).keySet()) {
          System.err.printf("== %s %s ==\n", model.name, params);
          System.err.println(modelStats.get(model, params));
        }
      }
    }
  }

  private static List<File> listAllFiles(List<File> directories) {
    List<File> files = Lists.newArrayList();
    if (directories != null) {
      for (File dir : directories) {
        for (File file : dir.listFiles()) {
          if (!file.getName().startsWith(".") && !file.isHidden()) {
            files.add(file);
          }
        }
      }
    }
    return files;
  }
  
  private static Set<Model<?>> getPrerequisites(Model<?> model) {
    Set<Model<?>> prereqs = Sets.newLinkedHashSet();
    for (Model<?> prereq : model.prerequisites) {
      prereqs.add(prereq);
      prereqs.addAll(getPrerequisites(prereq));
    }
    return prereqs;
  }

  private static LinkedHashSet<Model<?>> getSortedPrerequisites(Model<?> model) {
    Queue<Model<?>> todo = Queues.newArrayDeque();
    Multimap<Model<?>, Model<?>> following = LinkedHashMultimap.create();
    for (Model<?> prereq : getPrerequisites(model)) {
      if (prereq.prerequisites.isEmpty()) {
        todo.add(prereq);
      } else {
        for (Model<?> preprereq : prereq.prerequisites) {
          following.put(preprereq, prereq);
        }
      }
    }
    LinkedHashSet<Model<?>> models = Sets.newLinkedHashSet();
    while (!todo.isEmpty()) {
      Model<?> next = todo.iterator().next();
      todo.remove(next);
      models.add(next);
      for (Model<?> prereq : following.removeAll(next)) {
        if (!following.containsKey(prereq)) {
          todo.add(prereq);
        }
      }
    }
    return models;
  }

  private static Function<TemporalLink, List<Integer>> TEMPORAL_LINK_TO_SPANS = new Function<TemporalLink, List<Integer>>() {
    @Override
    public List<Integer> apply(TemporalLink temporalLink) {
      // order source and target indexes, left-to-right
      Anchor source = temporalLink.getSource();
      Anchor target = temporalLink.getTarget();
      return source.getBegin() < target.getBegin()
          ? Lists.newArrayList(source.getBegin(), source.getEnd(), target.getBegin(), target.getEnd())
          : Lists.newArrayList(target.getBegin(), target.getEnd(), source.getBegin(), source.getEnd());
    }
  };
  
  private static Function<TemporalLink, String> TEMPORAL_LINK_TO_RELATION = new Function<TemporalLink, String>() {
    @Override
    public String apply(TemporalLink temporalLink) {
      // match relation with left-to-right ordering of indexes
      Anchor source = temporalLink.getSource();
      Anchor target = temporalLink.getTarget();
      return source.getBegin() < target.getBegin()
          ? temporalLink.getRelationType()
          : TemporalLinkAnnotator_ImplBase.REVERSE_RELATION.get(temporalLink.getRelationType());
    }
  };

  private static List<Model.Params> SEQUENCE_CLASSIFIER_PARAM_SEARCH_SPACE = Lists.newArrayList(
      // L2-regularized L2-loss support vector classification (dual)
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 1, "-c", "0.1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 1, "-c", "0.5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 1, "-c", "1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 1, "-c", "5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 1, "-c", "10", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 1, "-c", "50", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 2, "-c", "0.1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 2, "-c", "0.5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 2, "-c", "1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 2, "-c", "5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 2, "-c", "10", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 2, "-c", "50", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 3, "-c", "0.1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 3, "-c", "0.5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 3, "-c", "1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 3, "-c", "5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 3, "-c", "10", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 3, "-c", "50", "-s", "1"));
//      // default is --iterations 500 --gaussian-variance 10
//      new Model.Params(MalletCRFStringOutcomeDataWriter.class),
//      new Model.Params(MalletCRFStringOutcomeDataWriter.class, "--forbidden", "O,I"),
//      new Model.Params(MalletCRFStringOutcomeDataWriter.class, "--iterations", "100"),
//      new Model.Params(MalletCRFStringOutcomeDataWriter.class, "--iterations", "1000"),
//      new Model.Params(MalletCRFStringOutcomeDataWriter.class, "--gaussian-variance", "1"),
//      new Model.Params(MalletCRFStringOutcomeDataWriter.class, "--gaussian-variance", "100"));

//  private static final String priorFlag = "--gaussianPriorVariance";

  private static List<Model.Params> CLASSIFIER_PARAM_SEARCH_SPACE = Lists.newArrayList(
//      // default is --gaussianPriorVariance 1
//      new Model.Params(MalletStringOutcomeDataWriter.class, "MaxEnt"),
//      new Model.Params(MalletStringOutcomeDataWriter.class, "MaxEnt", priorFlag, "0.1"),
//      new Model.Params(MalletStringOutcomeDataWriter.class, "MaxEnt", priorFlag, "10"),
//      // default is [iterations cutoff] 100 5
//      new Model.Params(MaxentStringOutcomeDataWriter.class),
//      new Model.Params(MaxentStringOutcomeDataWriter.class, "100", "10"),
//      new Model.Params(MaxentStringOutcomeDataWriter.class, "500", "5"),
      // L2-regularized logistic regression (primal)
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.1", "-s", "0"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.5", "-s", "0"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "1", "-s", "0"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "5", "-s", "0"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "10", "-s", "0"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "50", "-s", "0"),
      // L2-regularized L2-loss support vector classification (dual)
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "1", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "5", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "10", "-s", "1"),
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "50", "-s", "1"));

  private static final Model<Time> TIME_EXTENT_MODEL = new Model<Time>(
      "time-extent",
      Lists.<Model<?>> newArrayList(),
      TimeAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, 1, "-c", "0.1", "-s", "1"),
      SEQUENCE_CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Time.class,
      AnnotationStatistics.<Time> annotationToSpan(),
      AnnotationStatistics.<Time, String> annotationToNull(),
      null);

  private static final Model<Time> TIME_TYPE_MODEL = new Model<Time>(
      "time-type",
      Lists.<Model<?>> newArrayList(TIME_EXTENT_MODEL),
      TimeTypeAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "5", "-s", "0"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Time.class,
      AnnotationStatistics.<Time> annotationToSpan(),
      AnnotationStatistics.<Time> annotationToFeatureValue("timeType"),
      "timeType");

  private static final Model<Event> EVENT_EXTENT_MODEL = new Model<Event>(
      "event-extent",
      Lists.<Model<?>> newArrayList(),
      EventAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.1", "-s", "1"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Event.class,
      AnnotationStatistics.<Event> annotationToSpan(),
      AnnotationStatistics.<Event, String> annotationToNull(),
      null);

  private static final Model<Event> EVENT_ASPECT_MODEL = new Model<Event>(
      "event-aspect",
      Lists.<Model<?>> newArrayList(EVENT_EXTENT_MODEL),
      EventAspectAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "5", "-s", "0"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Event.class,
      AnnotationStatistics.<Event> annotationToSpan(),
      AnnotationStatistics.<Event> annotationToFeatureValue("aspect"),
      "aspect");

  private static final Model<Event> EVENT_CLASS_MODEL = new Model<Event>(
      "event-class",
      Lists.<Model<?>> newArrayList(EVENT_EXTENT_MODEL),
      EventClassAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "5", "-s", "0"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Event.class,
      AnnotationStatistics.<Event> annotationToSpan(),
      AnnotationStatistics.<Event> annotationToFeatureValue("eventClass"),
      "eventClass");

  private static final Model<Event> EVENT_MODALITY_MODEL = new Model<Event>(
      "event-modality",
      Lists.<Model<?>> newArrayList(EVENT_EXTENT_MODEL),
      EventModalityAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "1", "-s", "1"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Event.class,
      AnnotationStatistics.<Event> annotationToSpan(),
      AnnotationStatistics.<Event> annotationToFeatureValue("modality"),
      "modality");

  private static final Model<Event> EVENT_POLARITY_MODEL = new Model<Event>(
      "event-polarity",
      Lists.<Model<?>> newArrayList(EVENT_EXTENT_MODEL),
      EventPolarityAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "1", "-s", "1"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Event.class,
      AnnotationStatistics.<Event> annotationToSpan(),
      AnnotationStatistics.<Event> annotationToFeatureValue("polarity"),
      "polarity");

  private static final Model<Event> EVENT_TENSE_MODEL = new Model<Event>(
      "event-tense",
      Lists.<Model<?>> newArrayList(EVENT_EXTENT_MODEL),
      EventTenseAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.5", "-s", "1"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.NORMAL,
      Model.LoggingType.NONE,
      Event.class,
      AnnotationStatistics.<Event> annotationToSpan(),
      AnnotationStatistics.<Event> annotationToFeatureValue("tense"),
      "tense");

  private static final Model<TemporalLink> TLINK_EVENT_DOCTIME_MODEL = new Model<TemporalLink>(
      "tlink-event-doctime",
      Lists.<Model<?>> newArrayList(
          EVENT_EXTENT_MODEL,
          EVENT_ASPECT_MODEL,
          EVENT_CLASS_MODEL,
          EVENT_MODALITY_MODEL,
          EVENT_POLARITY_MODEL,
          EVENT_TENSE_MODEL),
      TemporalLinkEventToDocumentCreationTimeAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "50", "-s", "1"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.INTERSECTED_SPANS,
      Model.LoggingType.NONE,
      TemporalLink.class,
      TEMPORAL_LINK_TO_SPANS,
      TEMPORAL_LINK_TO_RELATION,
      null);

  private static final Model<TemporalLink> TLINK_EVENT_SENTTIME_MODEL = new Model<TemporalLink>(
      "tlink-event-senttime",
      Lists.<Model<?>> newArrayList(
          TIME_EXTENT_MODEL,
          TIME_TYPE_MODEL,
          EVENT_EXTENT_MODEL,
          EVENT_ASPECT_MODEL,
          EVENT_CLASS_MODEL,
          EVENT_MODALITY_MODEL,
          EVENT_POLARITY_MODEL,
          EVENT_TENSE_MODEL),
      TemporalLinkEventToSameSentenceTimeAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.5", "-s", "1"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.INTERSECTED_SPANS,
      Model.LoggingType.NONE,
      TemporalLink.class,
      TEMPORAL_LINK_TO_SPANS,
      TEMPORAL_LINK_TO_RELATION,
      null);

  private static final Model<TemporalLink> TLINK_EVENT_SUBORDEVENT_MODEL = new Model<TemporalLink>(
      "tlink-event-subordevent",
      Lists.<Model<?>> newArrayList(
          EVENT_EXTENT_MODEL,
          EVENT_ASPECT_MODEL,
          EVENT_CLASS_MODEL,
          EVENT_MODALITY_MODEL,
          EVENT_POLARITY_MODEL,
          EVENT_TENSE_MODEL),
      TemporalLinkEventToSubordinatedEventAnnotator.class,
      new Model.Params(LibLinearStringOutcomeDataWriter.class, "-c", "0.1", "-s", "1"),
      CLASSIFIER_PARAM_SEARCH_SPACE,
      Model.EvaluationType.INTERSECTED_SPANS,
      Model.LoggingType.NONE,
      TemporalLink.class,
      TEMPORAL_LINK_TO_SPANS,
      TEMPORAL_LINK_TO_RELATION,
      null);

  private ImmutableMultimap<Model<?>, Model.Params> models;

  private List<File> inferredTLinksDirectories;
  
  private boolean useVerbClauseTlinks;
  
  private boolean relationsOnly;

  public TempEval2013Evaluation(
      File baseDirectory,
      ImmutableMultimap<Model<?>, Model.Params> models,
      List<File> inferredTLinksDirectories,
      boolean useVerbClauseTlinks,
      boolean relationsOnly) {
    super(baseDirectory);
    this.models = models;
    this.inferredTLinksDirectories = inferredTLinksDirectories;
    this.useVerbClauseTlinks = useVerbClauseTlinks;
    this.relationsOnly = relationsOnly;
  }

  @Override
  protected CollectionReader getCollectionReader(List<File> files) throws Exception {
    return UriCollectionReader.getCollectionReaderFromFiles(files);
  }
  
  @Override
  public void train(CollectionReader reader, File directory) throws Exception {
    AggregateBuilder builder = new AggregateBuilder();

    // read the manual TimeML annotations into the CAS
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME));
    builder.add(
        UriToDocumentTextAnnotator.getDescription(),
        CAS.NAME_DEFAULT_SOFA,
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME);
    builder.add(TimeMlGoldAnnotator.getDescription());
    if (this.inferredTLinksDirectories != null) {
      builder.add(AnalysisEngineFactory.createPrimitiveDescription(
          UseInferredTlinks.class,
          UseInferredTlinks.PARAM_INFERRED_TLINKS_DIRECTORIES,
          this.inferredTLinksDirectories));
    }
    if (this.useVerbClauseTlinks) {
      builder.add(PlainTextTlinkGoldAnnotator.getDescription());
    }
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(FixTimeML.class));

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

    // add a data write for each model and its various parameters
    for (Model<?> model : this.models.keySet()) {
      for (Model.Params params : this.models.get(model)) {
        builder.add(model.getWriterDescription(directory, params));
      }
    }

    // run the pipeline
    SimplePipeline.runPipeline(reader, builder.createAggregate());

    // train each model with each of its various parameters
    for (Model<?> model : this.models.keySet()) {
      for (Model.Params params : this.models.get(model)) {
        System.err.printf("Training: %s %s\n", model.name, params);
        model.train(directory, params);
      }
    }
  }

  @Override
  protected ImmutableTable<Model<?>, Model.Params, AnnotationStatistics<String>> test(
      CollectionReader reader,
      File directory) throws Exception {
    String goldViewName = "GoldView";
    AggregateBuilder preprocess = new AggregateBuilder();

    // read the manual TimeML annotations into the gold view
    preprocess.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME));
    preprocess.add(
        UriToDocumentTextAnnotator.getDescription(),
        CAS.NAME_DEFAULT_SOFA,
        TimeMlGoldAnnotator.TIMEML_VIEW_NAME);
    preprocess.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        goldViewName));
    preprocess.add(TimeMlGoldAnnotator.getDescription(), CAS.NAME_DEFAULT_SOFA, goldViewName);
    if (this.inferredTLinksDirectories != null) {
      preprocess.add(AnalysisEngineFactory.createPrimitiveDescription(
          UseInferredTlinks.class,
          UseInferredTlinks.PARAM_INFERRED_TLINKS_DIRECTORIES,
          this.inferredTLinksDirectories), CAS.NAME_DEFAULT_SOFA, goldViewName);
    }
    if (this.useVerbClauseTlinks) {
      preprocess.add(
          PlainTextTlinkGoldAnnotator.getDescription(),
          CAS.NAME_DEFAULT_SOFA,
          goldViewName);
    }
    preprocess.add(
        AnalysisEngineFactory.createPrimitiveDescription(FixTimeML.class),
        CAS.NAME_DEFAULT_SOFA,
        goldViewName);
    preprocess.add(AnalysisEngineFactory.createPrimitiveDescription(
        CopyTextAndDocumentCreationTime.class,
        CopyTextAndDocumentCreationTime.PARAM_SOURCE_VIEW,
        goldViewName));
    if (this.relationsOnly) {
      preprocess.add(AnalysisEngineFactory.createPrimitiveDescription(
          CopyEventsAndTimes.class,
          CopyEventsAndTimes.PARAM_SOURCE_VIEW,
          goldViewName));
    } 

    // only add sentences and other annotations under <TEXT>
    preprocess.add(AnalysisEngineFactory.createPrimitiveDescription(
        SentenceAnnotator.class,
        SentenceAnnotator.PARAM_SENTENCE_MODEL_PATH,
        "/models/en-sent.bin",
        SentenceAnnotator.PARAM_WINDOW_CLASS_NAMES,
        new Class<?>[] { Text.class }));
    preprocess.add(TokenAnnotator.getDescription());
    preprocess.add(PosTaggerAnnotator.getDescription());
    preprocess.add(DefaultSnowballStemmer.getDescription("English"));
    preprocess.add(ParserAnnotator.getDescription());
    AnalysisEngine preprocessEngine = preprocess.createAggregate();

    // finalize TLINK ids and write out TimeML files
    AggregateBuilder postprocess = new AggregateBuilder();
    postprocess.add(AnalysisEngineFactory.createPrimitiveDescription(ShrinkTimesContainingEvents.class));
    postprocess.add(AnalysisEngineFactory.createPrimitiveDescription(SetTemporalLinkIDs.class));
    postprocess.add(TempEval2013Writer.getDescription(new File(this.baseDirectory, "timeml")));
    AnalysisEngine postprocessEngine = postprocess.createAggregate();

    // create one AnalysisEngine and one AnnotationStatistics for each model/parameters combination
    ImmutableTable.Builder<Model<?>, Model.Params, AnalysisEngine> enginesBuilder = ImmutableTable.builder();
    ImmutableTable.Builder<Model<?>, Model.Params, AnnotationStatistics<String>> statsBuilder = ImmutableTable.builder();
    for (Model<?> model : this.models.keySet()) {
      for (Model.Params params : this.models.get(model)) {
        AnalysisEngineDescription desc = model.getAnnotatorDescription(directory, params);
        enginesBuilder.put(model, params, AnalysisEngineFactory.createPrimitive(desc));
        statsBuilder.put(model, params, new AnnotationStatistics<String>());
      }
    }
    ImmutableTable<Model<?>, Model.Params, AnalysisEngine> engines = enginesBuilder.build();
    ImmutableTable<Model<?>, Model.Params, AnnotationStatistics<String>> stats = statsBuilder.build();

    // evaluate each CAS in the test data
    for (JCas jCas : new JCasIterable(reader, preprocessEngine)) {

      // evaluate each model/parameters combination
      JCas goldView = jCas.getView(goldViewName);
      JCas systemView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      for (Model<?> model : engines.rowKeySet()) {
        // remove any annotations from previous models that would interfere with the evaluation
        Map<? extends TOP, String> annotations = model.removeModelAnnotations(jCas);
        
        // apply and evaluate the model with each set of parameters
        for (Map.Entry<Model.Params, AnalysisEngine> entry : engines.row(model).entrySet()) {
          Model.Params params = entry.getKey();
          AnalysisEngine engine = entry.getValue();
  
          // remove any annotations from this model with other parameter settings
          model.removeModelAnnotations(jCas);
          
          // process and evaluate
          engine.process(jCas);
          model.evaluate(goldView, systemView, stats.get(model, params));
        }
        
        // restore any annotations from previous models
        model.restoreModelAnnotations(jCas, annotations);
      }

      postprocessEngine.process(jCas);
    }
    return stats;
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

  public static class CopyEventsAndTimes extends JCasAnnotator_ImplBase {

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
      for (Event sourceEvent : JCasUtil.select(sourceView, Event.class)) {
        Event event = (Event) copier.copyFs(sourceEvent);
        event.setFeatureValue(sofaFeature, jCas.getSofa());
        if (event.getEventInstanceID() ==  null) {
          event.setEventInstanceID(event.getId().replaceAll("^e", "ei"));
        }
        event.addToIndexes();
      }
      for (Time sourceTime : JCasUtil.select(sourceView, Time.class)) {
        if (!(sourceTime instanceof DocumentCreationTime)) {
          Time time = (Time) copier.copyFs(sourceTime);
          time.setFeatureValue(sofaFeature, jCas.getSofa());
          time.addToIndexes();
        }
      }
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
      
      // remove overlap relations
      for (TemporalLink tlink : Lists.newArrayList(JCasUtil.select(jCas, TemporalLink.class))) {
        if ("OVERLAP".equals(tlink.getRelationType())) {
          tlink.removeFromIndexes();
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

  public static class ShrinkTimesContainingEvents extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      String text = jCas.getDocumentText();
      for (Time time : JCasUtil.select(jCas, Time.class)) {
        List<Event> events = JCasUtil.selectCovered(jCas, Event.class, time);
        if (!events.isEmpty()) {
          int eventsBegin = events.get(0).getBegin();
          int eventsEnd = events.get(events.size() - 1).getEnd();
          int timeBegin, timeEnd;
          if (time.getBegin() - eventsBegin > time.getEnd() - eventsEnd) {
            timeBegin = time.getBegin();
            timeEnd = eventsBegin - 1;
            while (timeEnd > timeBegin && Character.isWhitespace(text.charAt(timeEnd))) {
              --timeEnd;
            }
          } else {
            timeEnd = time.getEnd();
            timeBegin = eventsEnd;
            while (timeBegin < timeEnd && Character.isWhitespace(text.charAt(timeBegin))) {
              ++timeBegin;
            }
          }
          String oldText = time.getCoveredText();
          time.setBegin(timeBegin);
          time.setEnd(timeEnd);
          String newText = time.getCoveredText();
          this.getLogger().warn(String.format("shrinking \"%s\" to \"%s\"", oldText, newText));
        }
      }

    }
  }

  public static class UseInferredTlinks extends JCasAnnotator_ImplBase {

    public static final String PARAM_INFERRED_TLINKS_DIRECTORIES = "InferredTLinksDirectories";

    @ConfigurationParameter(name = PARAM_INFERRED_TLINKS_DIRECTORIES, mandatory = true)
    private List<File> inferredTLinksDirectories;
    private Map<String, File> fileNameToFile;
    

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
      super.initialize(context);
      this.fileNameToFile = Maps.newHashMap();
      for (File dir : this.inferredTLinksDirectories) {
        for (File file : dir.listFiles()) {
          String fileName = file.getName();
          if (fileName.endsWith(".tml")) {
            String extension = String.format("[.]%s[.]tml$",  dir.getName());
            this.fileNameToFile.put(fileName.replaceAll(extension, ".tml"), file);
          }
        }
      }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      String fileName = new File(ViewUriUtil.getURI(jCas).getPath()).getName();
      File inferredTLinksFile = this.fileNameToFile.get(fileName);

      if (inferredTLinksFile == null) {
        this.getLogger().warn("No inferred TLINKs found for " + fileName);
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
            this.getLogger().warn(
                errorString(jCas, linkElem, "No annotation found for source of %s"));
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
            this.getLogger().warn(
                errorString(jCas, linkElem, "No annotation found for target of %s"));
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

    private static String errorString(JCas jCas, Element element, String message)
        throws AnalysisEngineProcessException {
      URI uri = ViewUriUtil.getURI(jCas);
      String elemString = new XMLOutputter().outputString(element);
      return String.format("In %s: " + message, uri, elemString);
    }

    private static void error(JCas jCas, Element element, String message)
        throws AnalysisEngineProcessException {
      throw new IllegalArgumentException(errorString(jCas, element, message));
    }
  }
}
