---
title: Chunking Classifier Tutorial
menu: Tutorials
menu-order: 2
---

# Chunking Classifier Tutorial

This tutorial will walk you through the process of creating a new UIMA annotator that uses a machine learning classifier to identify named entities.
This classifier uses a Begin, Inside, Outside (BIO) chunking representation that allows the model to classify individual tokens, and have the tokens combined together to identify the named entities. At the end of this tutorial, you should understand:

  * How to define a ClearTK annotator that uses a chunking classifier
  * How to train a chunking classifier and apply the model to new data

Note: all of the code below is available in [cleartk-examples/src/main/java/org/cleartk/examples/chunking](https://github.com/ClearTK/cleartk/tree/master/cleartk-examples/src/main/java/org/cleartk/examples/chunking).

## What is a Chunking Classifier?

For tasks such as [part-of-speech tagging](sequence_classifier.html), the classification task is simple: take a token as input, and produce a part-of-speech tag as output. Many other annotation tasks do not have this simple one-to-one mapping. For example, named entities (persons, organizations, locations, etc.) often consist of multiple words, e.g. "Bill Gates", "Hewlett-Packard" or "South Korea". So what should be the input and output for a named entity classifier?

One solution to this problem is a chunking classifier, which turns annotations over arbitrary spans (such as the span of a named entity) to annotations over individual tokens. For example, a BIO chunking classifier (one of the more common types of chunking classifiers), labels each token as being the Beginning of (B), the Inside of (I) or entirely Outside (O) of a span of interest.

For an example from named entity tagging, consider the sentence:

  * _Minjun is from South Korea._

And the annotations:

  * _Minjun_ is a "Person"
  * _South Korea_ is a "Location"

A BIO chunking classifier would produce the following token-level annotations:

<div class="row">
<div class="col-md-3">

| Word     | Outcome    |
|----------|------------|
| _Minjun_ | B-Person   |
| _is_     | O          |
| _from_   | O          |
| _South_  | B-Location |
| _Korea_  | I-Location |
| _._      | O          |
{: .table}

</div>
</div>

In this tutorial, we will develop a named entity annotator using a BIO chunking classifier.

## Defining a UIMA Annotator for a Chunking Classifier

Creating an annotator for a chunking classifier follows the same basic approach to creating a classifier annotator as was shown in the [part-of-speech tagging tutorial](sequence_classifier.html).
We still begin by defining a subclass of `CleartkSequenceAnnotator`:

{% highlight java %}
public class NamedEntityChunker extends CleartkSequenceAnnotator<String> {
...
}
{% endhighlight %}

And we define the various features that we intend to extract for each token:

{% highlight java %}
...
@Override
public void initialize(UimaContext context) throws ResourceInitializationException {
  super.initialize(context);

  // the token feature extractor: text, char pattern (uppercase, digits, etc.), and part-of-speech
  this.extractor = new CombinedExtractor(
      new CoveredTextExtractor(),
      new CharacterCategoryPatternExtractor(PatternType.REPEATS_MERGED),
      new TypePathExtractor(Token.class, "pos"));

  // the context feature extractor: the features above for the 3 preceding and 3 following tokens
  this.contextExtractor = new CleartkExtractor(
      Token.class,
      this.extractor,
      new Preceding(3),
      new Following(3));
  ...
}
{% endhighlight %}

For a chunking classifier, we will also need a utility class that helps us convert between tokens and named entities.
Such classes can be found in the `org.cleartk.classifier.chunking` package, and in particular, we will use the [BIOChunking](https://github.com/ClearTK/cleartk/blob/master/cleartk-ml/src/main/java/org/cleartk/ml/chunking/BioChunking.java) class:

{% highlight java %}
private BIOChunking<Token, NamedEntityMention> chunking;

@Override
public void initialize(UimaContext context) throws ResourceInitializationException {
  ...
  // the chunking definition: Tokens will be combined to form NamedEntityMentions, with labels
  // from the "mentionType" attribute so that we get B-location, I-person, etc.
  this.chunking = new BIOChunking<Token, NamedEntityMention>(
      Token.class,
      NamedEntityMention.class,
      "mentionType");
}
{% endhighlight %}

Now we're ready to define how this annotator will process documents.
The approach here starts in a similar to the `CleartkSequenceAnnotator` in the [part-of-speech tagging tutorial](sequence_classifier.html) - we'll first extract features for each of the tokens we want to classify:

{% highlight java %}
@Override
public void process(JCas jCas) throws AnalysisEngineProcessException {
  for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

    // extract features for each token in the sentence
    List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
    List<List<Feature>> featureLists = new ArrayList<List<Feature>>();
    for (Token token : tokens) {
      List<Feature> features = new ArrayList<Feature>();
      features.addAll(this.extractor.extract(jCas, token));
      features.addAll(this.contextExtractor.extract(jCas, token));
      featureLists.add(features);
    }
    ...
  }
}
{% endhighlight %}

The main difference when using a chunking classifier is that the labels for each token are no longer a simple attribute of the `Token` CAS object. Instead, we must use the `BIOChunking` object to get token labels from `NamedEntityMentions` during training and to convert tokens and their labels into `NamedEntityMentions` during classification:

{% highlight java %}
@Override
public void process(JCas jCas) throws AnalysisEngineProcessException {
  ...
  // during training, convert NamedEntityMentions in the CAS into expected classifier outcomes
  if (this.isTraining()) {

    // extract the gold (human annotated) NamedEntityMention annotations
    List<NamedEntityMention> namedEntityMentions = JCasUtil.selectCovered(
        jCas,
        NamedEntityMention.class,
        sentence);

    // convert the NamedEntityMention annotations into token-level BIO outcome labels
    List<String> outcomes = this.chunking.createOutcomes(jCas, tokens, namedEntityMentions);

    // write the features and outcomes as training instances
    this.dataWriter.write(Instances.toInstances(outcomes, featureLists));
  }

  // during classification, convert classifier outcomes into NamedEntityMentions in the CAS
  else {

    // get the predicted BIO outcome labels from the classifier
    List<String> outcomes = this.classifier.classify(featureLists);

    // create the NamedEntityMention annotations in the CAS
    this.chunking.createChunks(jCas, tokens, outcomes);
  }
  ...
}
{% endhighlight %}

## Training the Classifier on Manual Annotations

To train the classifier, we'll need to assemble a pipeline that reads in named entities that have been manually annotated.
For this purpose, we're using [MASC](http://www.anc.org/MASC/), which annotates dates, locations, organizations and persons in texts from a variety of different domains.
First we set up the pipeline to read in MASC files:

{% highlight java %}
// a reader that loads the URIs of the training files
CollectionReaderDescription reader = UriCollectionReader.getDescriptionFromDirectory(
    options.trainDirectory, ...);

// assemble the training pipeline
AggregateBuilder aggregate = new AggregateBuilder();

// an annotator that loads the text from the training file URIs
aggregate.add(UriToDocumentTextAnnotator.getDescription());

// an annotator that parses and loads MASC named entity annotations (and tokens)
aggregate.add(MASCGoldAnnotator.getDescription());

// an annotator that adds part-of-speech tags (so we can use them for features)
aggregate.add(PosTaggerAnnotator.getDescription());
{% endhighlight %}

We add to this pipeline the `NamedEntityChunker` that we just defined, configuring it to write training data.
Here we've decided to write Mallet CRF training data so that we can use Mallet CRF as our classifier, but any other `SequenceDataWriter` would have been fine here as well.
_(Note that Mallet CRF has LGPL dependencies. See the `org.cleartk.classifier.viterbi` package for an alterative `SequenceDataWriter` without LGPL dependencies.)_

{% highlight java %}
// our NamedEntityChunker annotator, configured to write Mallet CRF training data
aggregate.add(AnalysisEngineFactory.createPrimitiveDescription(
    NamedEntityChunker.class,
    CleartkSequenceAnnotator.PARAM_IS_TRAINING,
    true,
    DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
    options.modelDirectory,
    DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
    MalletCRFStringOutcomeDataWriter.class));
{% endhighlight %}

Now the whole pipeline for writing training data has been assembled, so we just need to run it over the MASC documents, and then train the classifier on the data that the pipeline has written:

{% highlight java %}
// run the pipeline over the training corpus
SimplePipeline.runPipeline(reader, aggregate.createAggregateDescription());

// train a Mallet CRF model on the training data
Train.main(options.modelDirectory);
{% endhighlight %}

Running this code will train the classifier and store it in a "model.jar" file in the given directory.

Note that MASC includes a fair amount of data, and CRF training is slow, so you'll probably have to wait 5-10 minutes for the model training to complete.
Mallet will produce a variety of progress messages as the training moves along.

## Running the Trained Classifier on New Text

We have now trained a BIO chunking classifier for named entity recognition!
To apply this classifier to new documents, we just need to assemble a pipeline that runs our annotator in classification mode.

Our pipeline will first load the new plain text files, and pre-process them to add sentences, tokens and parts-of-speech (the requirements for our `NamedEntityChunker` to be able to run and extract its features):

{% highlight java %}
// a reader that loads the URIs of the text file
CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(...);

// assemble the classification pipeline
AggregateBuilder aggregate = new AggregateBuilder();

// an annotator that loads the text from the training file URIs
aggregate.add(UriToDocumentTextAnnotator.getDescription());

// annotators that identify sentences, tokens and part-of-speech tags in the text
aggregate.add(SentenceAnnotator.getDescription());
aggregate.add(TokenAnnotator.getDescription());
aggregate.add(PosTaggerAnnotator.getDescription());
{% endhighlight %}

Next, we'll add the `NamedEntityChunker` annotator, and configure it to load the classifier jar that we built previously:

{% highlight java %}
// our NamedEntityChunker annotator, configured to classify on the new texts
aggregate.add(AnalysisEngineFactory.createPrimitiveDescription(
    NamedEntityChunker.class,
    CleartkSequenceAnnotator.PARAM_IS_TRAINING,
    false,
    GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
    new File(options.modelDirectory, "model.jar")));
{% endhighlight %}

That's it! Now we can run the pipeline and new `NamedEntityMention` annotations will be added to the `CAS`:

{% highlight java %}
// run the classification pipeline on the new texts
SimplePipeline.runPipeline(reader, aggregate.createAggregateDescription());
{% endhighlight %}