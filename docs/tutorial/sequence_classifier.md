---
title: Sequence Classifier Tutorial
menu: Tutorials
menu-order: 1
---

# Sequence Classifier Tutorial

This tutorial will walk you through the process of creating a new machine learning component using ClearTK, a part-of-speech tagger trained on the Penn Treebank corpus.
It assumes your environment is already set up as described on [the getting started page]({{ site.baseurl}}/docs/index.html).
At the end of this tutorial, you should understand:

* How a `CleartkSequenceAnnotator` object extracts features and creates a sequence of instances suitable for model training or classification.
* How to create training data using a `SequenceDataWriter` and train a model using one of ClearTK's supported sequential classifiers.
* How to automatically annotate part-of-speech tags using a `SequenceClassifier`.

## Writing `CleartkSequenceAnnotator` classes

Extensions of `CleartkSequenceAnnotator` (and `CleartkAnnotator`) are at the core of many machine learning components provided by ClearTK and are the recommended construct for creating new components using ClearTK.
`CleartkSequenceAnnotator`s understand how to take a document (represented by a `JCas`) and create machine learning features for a particular task.
They also understand how to take the predictions of a classifier and convert these into annotations over the document (the `JCas`).
Thus, `CleartkSequenceAnnotator` objects serve as the interface between the UIMA annotations and machine learning classifiers.

All of the code discussed here can be found in the [`org.cleartk.example`](https://github.com/ClearTK/cleartk/tree/master/cleartk-examples/src/main/java/org/cleartk/examples/pos) package.
We'll start with a simple `CleartkSequenceAnnotator` designed to create features for a part-of-speech tagging task.
In Eclipse, create a new Java class (File -> New -> Class), set the `Name:` to `ExamplePOSAnnotator`.
Set the superclass to be `org.cleartk.classifier.CleartkSequenceAnnotator<String>`.
This should generate a class like:

{% highlight java %}
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkSequenceAnnotator;

public class ExamplePOSAnnotator extends CleartkSequenceAnnotator<String> {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

    }
}
{% endhighlight %}

Note that the generic type `OUTCOME_TYPE` defined in `CleartkSequenceAnnotator` is parameterized with `String` here.
This is because the outcome of the classifier will be strings corresponding to part-of-speech tags.
The `process` method defines how features and labels are extracted from the annotations of a `JCas`, and how classifier predictions are used to create new `JCas` annotations.
We will also override the `initialize` method which is typically used to initialize feature extractors, reading parameters as necessary from the `UimaContext`.

### Processing a JCas with an `CleartkSequenceAnnotator`

Let's start out by working on the `process` method of our `CleartkSequenceAnnotator`.
This method defines how features are generated from the `Annotation` objects in a `JCas`.
We want to label part-of-speech tags, which in this example are attributes of `Token` annotations.
We are going to do this in a "sequential" fashion by having the classifier tag an entire sequence of tokens at the same time.
The sequence of tokens tagged will correspond to the tokens in one sentence.
For many other tasks, classification will be performed on one item at a time.
In such cases, `CleartkAnnotator` is the appropriate superclass to use for your component.

For each sequence of tokens our `CleartkSequenceAnnotator` needs to know how to do two things:

* When training, extract features and part-of-speech tags from the `Annotation`s in the `JCas`, and pass them to a `SequenceDataWriter`
* When predicting, extract features, pass them to a `SequenceClassifier`, and use the resulting classifications to add/update `Annotation`s to the `JCas`.

The `process` method will perform both of these tasks as they usually share a lot of code.
For our part-of-speech tagging task, we can define the `process` method like this:

{% highlight java %}
private SimpleFeatureExtractor tokenFeatureExtractor;

private CleartkExtractor contextFeatureExtractor;

public void process(JCas jCas) throws AnalysisEngineProcessException {
  // for each sentence in the document, generate training/classification instances
  for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
    List<List<Feature>> tokenFeatureLists = new ArrayList<List<Feature>>();
    List<String> tokenOutcomes = new ArrayList<String>();

    // for each token, extract features and the outcome
    List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
    for (Token token : tokens) {

      // apply the two feature extractors
      List<Feature> tokenFeatures = new ArrayList<Feature>();
      tokenFeatures.addAll(this.tokenFeatureExtractor.extract(jCas, token));
      tokenFeatures.addAll(this.contextFeatureExtractor.extractWithin(jCas, token, sentence));
      tokenFeatureLists.add(tokenFeatures);

      // add the expected token label from the part of speech
      if (this.isTraining()) {
        tokenOutcomes.add(token.getPos());
      }
    }

    // for training, write instances to the data write
    if (this.isTraining()) {
      this.dataWriter.write(Instances.toInstances(tokenOutcomes, tokenFeatureLists));
    }

    // for classification, set the token part of speech tags from the classifier outcomes.
    else {
      List<String> outcomes = this.classifier.classify(tokenFeatureLists);
      Iterator<Token> tokensIter = tokens.iterator();
      for (String outcome : outcomes) {
        tokensIter.next().setPos(outcome);
      }
    }
  }
}
{% endhighlight %}

So, for each `Sentence` in the document we will examine a "sequence" of `Token`s.
For each `Token` we create a new classification instance from the part-of-speech tag label and some features.
(For the moment, we're ignoring exactly what kind of features are generated - these will be discussed further in the next section.)
We then write the training instances to a file via `this.dataWriter` if we are in training mode, or we classify the sequence of tokens via `this.classifier` and add the part-of-speech tags to the CAS if we are in predicting mode.

To make sense of this code, it is helpful to realize that:

* When training:
  * `this.dataWriter` will be instantiated and ready to write sequences of instances
  * calling `token.getPos()` on any token in the sequence will return a non-null part-of-speech tag which will be used to set the expected outcome of the classification instance.
    That is, in training mode the data provides the expected answers.
* When classifying
  * `this.classifier` will be instantiated and ready to classify sequences of instances.
  * calling `token.getPos()` on any token in the sequence will (usually) return a null part-of-speech tag.
    We will use the classifier to help us fill in these "missing" part-of-speech tags.

### Initializing feature extractors in a `CleartkSequenceAnnotator`

Now that we understand how our extension of `CleartkSequenceAnnotator` will be converting the JCas to classification instances, we can introduce some features that will be useful for our task.
Feature extractors are typically created in the `initialize` method, which is invoked before the `process` method is ever called.
Since we're building a part-of-speech tagger, some useful features are:

* the word itself
* the word lowercased
* a categorical label describing the capitalization of the word - e.g. "ALL\_UPPERCASE", "INITIAL\_UPPERCASE", "ALL\_LOWERCASE", "MIXED\_CASE"
* a categorical label describing the use of numbers in the word - e.g. "DIGITS", "YEAR\_DIGITS", "ALPHANUMERIC", etc.
* character bigram suffix of the word
* character trigram suffix of the word
* the two words to the left and right of the word

This is by no means an exhaustive (nor optimized) set of features that can be found in part-of-speech taggers - but these are representative of the kinds of features that are extracted in part-of-speech taggers and are what we will use here for this example tagger.
Here's how we create these feature extractors in our initialize method:

{% highlight java %}
public void initialize(UimaContext context) throws ResourceInitializationException {
  super.initialize(context);

  // a feature extractor that creates features corresponding to the word, the word lower cased
  // the capitalization of the word, the numeric characterization of the word, and character ngram
  // suffixes of length 2 and 3.
  this.tokenFeatureExtractor = new FeatureFunctionExtractor(
      new CoveredTextExtractor(),
      new LowerCaseFeatureFunction(),
      new CapitalTypeFeatureFunction(),
      new NumericTypeFeatureFunction(),
      new CharacterNGramFeatureFunction(Orientation.RIGHT_TO_LEFT, 0, 2),
      new CharacterNGramFeatureFunction(Orientation.RIGHT_TO_LEFT, 0, 3));

  // a feature extractor that extracts the surrounding token texts (within the same sentence)
  this.contextFeatureExtractor = new CleartkExtractor(
      Token.class,
      new CoveredTextExtractor(),
      new Preceding(2),
      new Following(2));
}
{% endhighlight %}

We start with a `CoveredTextExtractor` which simply takes an annotation and returns the text that it covers.
We create a `FeatureFunctionExtractor` that wraps this `CoveredTextExtractor` and introduces some feature functions that take the result of the `CoveredTextExtractor` and produce features like the capitalization, numeric description, and character suffixes.

Note the difference between feature extractors and feature functions here.
Feature extractors take an Annotation from the JCas and extract features from it.
Feature functions take the features produced by ''a feature extractor'' and generate new features from the old ones.
Since feature functions don't need to look up information in the `JCas`, they may be more efficient than feature extractors.
So in our `initialize` method, the `CharacterNGramFeatureFunction`s simply extract suffixes from the text returned by the `CoveredTextExtractor`.

Finally, we create a `CleartkExtractor` which will create features from the surrounding context of a token.
In this case, we are going to retrieve the two words before and after a token.
Note that we will not create features from previous part-of-speech labels because they will not be available at classification time - all part-of-speech tags in a sentence will be predicted simultaneously since we're using a `CleartkSequenceAnnotator` instead of a `CleartkAnnotator`.

And that's it for our first pass of our `ExamplePOSAnnotator` - there is no more code to write.
At this point it is a matter of understanding the other components and how to configure and run them.
Now we are ready to learn about training and using machine learning models.

## Building a part-of-speech model

To train a machine learning model, we'll need to create a UIMA pipeline that reads in some example part-of-speech tagged data and passes this to the `CleartkSequenceAnnotator` we wrote above.
Our example data here comes in treebank format, so we'll use `TreebankGoldAnnotator` to populate a `CAS` with the sentences, tokens and part-of-speech tags that our annotator needs for training.
The code that performs these steps can be found in `org.cleartk.example.pos.BuildTestExamplePosModel` and looks like:

{% highlight java %}
// A collection reader that creates one CAS per file, containing the file's URI
CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(files);

// The pipeline of annotators
AggregateBuilder builder = new AggregateBuilder();

// An annotator that creates an empty treebank view in the CAS
builder.add(AnalysisEngineFactory.createPrimitiveDescription(
    ViewCreatorAnnotator.class,
    ViewCreatorAnnotator.PARAM_VIEW_NAME,
    TreebankConstants.TREEBANK_VIEW));

    // An annotator that reads the treebank-formatted text into the treebank view
builder.add(
    UriToDocumentTextAnnotator.getDescription(),
    CAS.NAME_DEFAULT_SOFA,
    TreebankConstants.TREEBANK_VIEW);

// An annotator that uses the treebank text to add sentences, tokens and POS tags to the CAS
builder.add(TreebankGoldAnnotator.getDescriptionPOSTagsOnly());

// The POS annotator, configured to write training data
builder.add(ExamplePOSAnnotator.getWriterDescription(outputDirectory));

// Run the pipeline of annotators on each of the CASes produced by the reader
SimplePipeline.runPipeline(reader, builder.createAggregateDescription());

// Train a classifier on the training data, and package it into a .jar file
Train.main(outputDirectory);
{% endhighlight %}

Essentially, we create a pipeline that reads in the sentences, tokens and part of speech tags from the treebank files, and runs our example annotator over these.
We run this pipeline, and then we call `Train.main` to train a classifier on the data that our pipeline has written.

The method `ExamplePOSAnnotator.getWriterDescription` is where we create an create an instance of our `CleartkSequenceAnnotator` and configure it with the type of classifier to train.
To train a Mallet CRF model, that method would return something like:

{% highlight java %}
AnalysisEngineFactory.createPrimitiveDescription(
    ExamplePOSAnnotator.class,
    CleartkSequenceAnnotator.PARAM_IS_TRAINING,
    true,
    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
    outputDirectory,
    DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
    MalletCRFStringOutcomeDataWriter.class);
{% endhighlight %}

The current definition of this method actually trains an OpenNLP Maxent model augmented with a viterbi search.
See the code for more details.

Most classifiers take some additional training parameters.
The call `Train.main(outputDirectory)` just uses the default values of all training parameters, but `Train.main(File, String...)` accepts additional `String` arguments for additional training parameters.
Each classifier has a different format for these parameters - you may have to inspect the underlying machine learning library to determine what parameters it accepts.

If you run `org.cleartk.example.pos.BuildTestExamplePosModel` as it is, it will train a part-of-speech tagging classifier and package it up as `target/examples/pos/model.jar`.


## Running the part-of-speech tagger

Now that we have created a model for part-of-speech tagging we are now ready to tag some parts-of-speech! We just need to set up a UIMA pipeline that creates sentences and tokens and then passes these to our `CleartkSequenceAnnotator` which will use the trained model to add the part-of-speech tags.
The code that performs these steps is in `org.cleartk.example.pos.RunExamplePOSAnnotator` and looks like:

{% highlight java %}
// A collection reader that creates one CAS per file, containing the file's URI
CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(files);

// The pipeline of annotators
AggregateBuilder builder = new AggregateBuilder();

// An annotator that reads in the file text
builder.add(UriToDocumentTextAnnotator.getDescription());

// An annotator that adds Sentence annotations
builder.add(SentenceAnnotator.getDescription());

// An annotator that adds Token annotations
builder.add(TokenAnnotator.getDescription());

// The POS annotator, configured to make predictions
builder.add(ExamplePOSAnnotator.getClassifierDescription(ExamplePOSAnnotator.DEFAULT_MODEL));

// Run the pipeline of annotators on each of the CASes produced by the reader
SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
{% endhighlight %}

The only new things in this snippet are the use of `org.cleartk.syntax.opennlp.SentenceAnnotator`, which wraps OpenNLP's sentence segmenter, `org.cleartk.token.tokenizer.TokenAnnotator`, which is a PennTreeBank-style tokenizer, and `ExamplePOSAnnotator.getClassifierDescription`, which just creates an instance of our `` and configures it for prediction, like so:
{% highlight java %}
AnalysisEngineFactory.createPrimitiveDescription(
    ExamplePOSAnnotator.class,
    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
    modelFileName);
{% endhighlight %}

The code in `org.cleartk.example.pos.RunExamplePOSAnnotator` contains one additional annotator to take the part-of-speech tags added to the `CAS` by our annotator and write them to a file.
If you run this class as it is, it will produce a file `target/examples/pos/2008_Sichuan_earthquake.txt.pos`.  Please open this file and observe the results.

You have successfully created a part-of-speech tagger and run it to tag text!
