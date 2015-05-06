---
title: ClearTK-ML
menu: Documentation
menu-order: 3
---

# ClearTK-ML

The ClearTK-ML module provides the core APIs for building machine-learning-based annotators in UIMA.

## CleartkAnnotator

In ClearTK, machine-learning-based annotators are subclasses of  [CleartkAnnotator](https://github.com/ClearTK/cleartk/blob/master/cleartk-ml/src/main/java/org/cleartk/ml/CleartkAnnotator.java) (or for sequence-labeling tasks, its sibling, [CleartkSequenceAnnotator](https://github.com/ClearTK/cleartk/blob/master/cleartk-ml/src/main/java/org/cleartk/ml/CleartkSequenceAnnotator.java)). The [POS tagger tutorial]({{ site.baseurl }}/docs/tutorial/sequence_classifier.html) and the [BIO tagging tutorial]({{ site.baseurl }}/docs/tutorial/chunking_classifier.html) give detailed examples of creating such annotators. In short though, a `CleartkAnnotator` is just a UIMA annotator (i.e. a subclass of `JCasAnnotator_ImplBase`) where the `process(JCas)` method extracts features and passes them to a classifier. `CleartkAnnotator` (and `CleartkSequenceAnnotator`) provides useful methods and instance variables that are available to subclasses:

 * `isTraining()` - indicates whether the annotator is being used to write training data or to classify new instances
 * `dataWriter` - an implementation of the `DataWriter` (or `SequenceDataWriter`, for `CleartkSequenceAnnotator`) interface, which allows `Instance` objects, composed of features and outcomes (a.k.a. labels), to be written out in an appropriate data format for classifier training. Available only when `isTraining()` returns true.
 * `classifier` - an implementation of the `Classifier` (or `SequenceClassifier`) interface, which accepts a list of features and predicts an outcome for those features. Available only when `isTraining()` is false.

### CleartkAnnotator Parameters

As is usual for UIMA, an annotator must be wrapped in an `AnalysisEngine` before it can be used in a pipeline. The `AnalysisEngine` must specify any parameters needed by whatever annotator it wraps. `CleartkAnnotator` (and `CleartkSequenceAnnotator`) have a number of parameters that must be specified:

 * `CleartkAnnotator.PARAM_IS_TRAINING` (or for `CleartkSequenceAnnotator`, `CleartkSequenceAnnotator.PARAM_IS_TRAINING`) - A boolean indicating whether the annotator will be used to write training data or to make new classifications.

When `PARAM_IS_TRAINING` is true, the following parameters are required:

 * `DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY` - A `File` that indicates where training data should be written.
 * `DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME` (or for `CleartkSequenceAnnotator`, `DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME`) - A `DataWriter` (or `SequenceDataWriter`) class object or class name that Indicates what type of classifier will be trained from the training data. For example, `org.cleartk.classifier.liblinear.LibLinearStringOutcomeDataWriter`.

When `PARAM_IS_TRAINING` is false, the following parameters are required:

* GenericJarClassifierFactory.PARAM\_CLASSIFIER\_JAR\_PATH - A `File`, `URL` or classpath path that locates the trained `model.jar` that should be loaded for the classifier.

_(Side note: You may have noticed that many of these parameters are not directly on the `CleartkAnnotator` class. That's because `CleartkAnnotator` allows you to specify arbitrary factory classes for creating `DataWriter`s or `Classifier`s via its parameters `PARAM_DATA_WRITER_FACTORY_CLASS_NAME` and `PARAM_CLASSIFIER_FACTORY_CLASS_NAME`. The default factory classes are where most of the above required parameters come from. For typical use of ClearTK, the default factory classes will almost always be what you want.)_

### CleartkAnnotator AnalysisEngines

Putting the above all together, a typical pipeline that prepares training data for a classifier will include code that looks like:

    AnalysisEngineFactory.createPrimitiveDescription(
        <name-of-your-cleartk-annotator>.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        true,
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        <your-output-directory-file>,
        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
        <name-of-your-selected-classifier's-data-writer>.class);

And a pipeline that uses the classifier to classify new instances will typically include code that looks like:

    AnalysisEngineFactory.createPrimitiveDescription(
        <name-of-your-cleartk-annotator>.class,
        CleartkAnnotator.PARAM_IS_TRAINING,
        true,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        <path-to-your-model.jar-file>);

For more detailed examples, see the [POS tagger tutorial]({{ site.baseurl }}/docs/tutorial/sequence_classifier.html) and the [BIO tagging tutorial]({{ site.baseurl }}/docs/tutorial/chunking_classifier.html).