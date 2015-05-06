---
title: Feature Extraction Tutorial
menu: Tutorials
menu-order: 3
---

# Feature Extraction Tutorial

Preliminaries:

* Working versions of the code snippets found in this tutorial can be found in [cleartk-ml/src/test/java/org/cleartk/classifier/feature/FeatureExtractionTutorialTest.java](https://github.com/ClearTK/cleartk/blob/master/cleartk-ml/src/test/java/org/cleartk/ml/feature/FeatureExtractionTutorialTest.java)
* This tutorial is strictly about feature _extraction_ (how to dream up and create features) and will not cover the equally important concept of feature _selection_ which addresses the question of determining what an optimal set of features is for a given learning problem.
* The term _feature_ has a machine learning specific meaning that is used in this tutorial as described above.  In UIMA a _feature_ can refer to an attribute of a type in a type system.  We will refer to this type of feature as a **type system feature**.

## Introduction

A core step for a typical statistical NLP component is to convert raw or annotated text into **features**, which give a machine learning model a simpler, more focused view of the text.
A **feature** in ClearTK is a simple object that contains a name and a value and is implemented by the class `org.cleartk.classifier.Feature` (or a subclass of it.)
Because features are so varied and task-specific, it is somewhat problematic to attempt to guide a developer on which features she should try for a given scenario.
However, there are a number of tasks that use similar features and so it is worthwhile to enumerate common features that are extracted.
For example, one very common feature used in a variety of information extraction tasks is a part-of-speech label assigned to a token.
If a part-of-speech tagger has already been run on the text and the part-of-speech labels are in the CAS, then part-of-speech features can be extracted quite easily by querying the CAS.
A corresponding feature could be created with the following code:

{% highlight java %}
new Feature("part-of-speech", "VBZ")
{% endhighlight %}

We will see below that there are better ways to create part-of-speech features.

There are basically two places where features are needed:

* `org.cleartk.classifier.Classifier.classify()` which takes a list of features, performs classification against the model, and returns an outcome.
* `org.cleartk.classifier.DataWriter.write()` which takes an object of type org.cleartk.classifier.Instance which is composed of an outcome and a list of features.  This highlights the fact that we collect a variety of features for each "thing" we are classifying.  That thing might be an annotation (e.g. a token or sentence), a document, a relationship between two annotations, etc.

In a typical use case, a class that extends `CleartkAnnotator` or `CleartkSequenceAnnotator` will have a single place in the code where feature extraction is performed.
The resulting features will either be sent to the classifier or the data writer depending on the context.
In both cases, the ClearTK machinery that underpins both calls knows how to take the features collected and turn them into a feature vector (which is typically a float point array) appropriate for the target machine learning library and either perform classification or write it out to a file for subsequent training.
This is a key advantage to the programmer who does not need to worry about the details of converting, for example, a part-of-speech tag feature into a floating point number and where to put it in the feature vector.

The process of creating features for a given learning or classification instance is called **feature extraction**.
A **feature extractor** is any piece of code, perhaps a method or a class, that performs feature extraction.
However, in most cases you will likely benefit from the feature extraction infrastructure that ClearTK provides to accomplish a wide variety of common tasks.
Feature extraction typically involves querying the CAS for information about existing annotations and, perhaps, applying additional analysis.
Because features are typically many in number, short lived, and dynamic in nature (e.g. features can derive from previous classifications), they are **not added to the CAS** but rather are created only as they are needed as simple Java objects directly before classification takes place.
ClearTK provides feature extractors that produce features common to a wide variety of NLP tasks.
However, it is easy to create new feature extractors that may require specialized code for a given task.
Our object oriented approach to feature extraction may not be ideal for scenarios that have very high performance requirements.
Though it is our experience that this extra overhead is relatively minimal.

## A simple example

Here's a simple example that is a bit contrived and arbitrary.
Suppose we are building a classifier that determines how many syllables are in a word.
What features would be useful to extract?
Here are a few possibilities:

* the length of the word:
{% highlight java %}
new Feature("token length", token.getCoveredText().length())
{% endhighlight %}
* the number of spans of contiguous vowels:
{% highlight java %}
new Feature("vowel groups", vowelGroupings(token.getCoveredText()))
{% endhighlight %}
* the number of spans of contiguous consonants:
{% highlight java %}
new Feature("consonant groups", consonantGroupings(token.getCoveredText()))
{% endhighlight %}
* what type of character is the first letter?:
{% highlight java %}
new Feature("first letter type", firstLetterType(token.getCoveredText()))
{% endhighlight %}
* Is the first letter a 'c'?
{% highlight java %}
new Feature("is first letter 'c'?", token.getCoveredText().charAt(0) == 'c')
{% endhighlight %}

Again, this example is a bit silly for many reasons but it illustrates how simple feature extraction can be.
For the thing you are classifying (in this case individual tokens) what are possible features we might dream up that might help a classifier learn a better model?
This gets at the heart of the art of applied machine learning.

## CoveredTextExtractor

A very common feature to use is the covered text of the annotation for which we are extracting annotations for (i.e. for scenarios in which an annotation corresponds to the thing being classified.)
Using this feature extractor roughly corresponds to whether or not you can consider the model to be _lexicalized_.
Having a lexicalized model may be advantageous if the training data is sampled from the target data to be classified but may cause overfitting problems when it is not.
Using the `CoveredTextExtractor` is really quite simple:

{% highlight java %}
CoveredTextExtractor<Token> extractor = new CoveredTextExtractor<Token>();
List<Feature> features = extractor.extract(this.jCas, token);
{% endhighlight %}

The returned list will have a single feature in it whose value is the text of the token.
The method returns a list in order to implement an interface that allows this feature extractor to be used in more sophisticated ways such as in `CleartkExtractor` as described below.
Note that because the `CoveredTextExtractor` is so commonly used, it can be thought of as a "default" feature.
Thus there is no need for the feature to be named and the feature value effectively serves as its name.
Features generated by this extractor have the value `null` for the name.
Also, we note that because each unique value of each feature extracted corresponds to a new feature this means that using this feature extractor can greatly increase the feature space of the resulting model.

If what you really want is the covered text after it has been normalized for case (i.e. lowercased), then you can use the `CoveredTextExtractor` in conjunction with a feature function that handles converting the covered text features to lower case as in the following code:

{% highlight java %}
FeatureExtractor1<Token> extractor = new FeatureFunctionExtractor<Token>(extractor, false, new LowerCaseFeatureFunction());
List<Feature> features = extractor.extract(this.jCas, token);
{% endhighlight %}

## TypePathExtractor

The `TypePathExtractor` provides a simple way to extract features from annotations corresponding type system type features (please see definition of _type system feature_ above.)
For example, to extract features corresponding to a token's part-of-speech label you could use code like this:

{% highlight java %}
FeatureExtractor1<Token> extractor = new TypePathExtractor<Token>(Token.class, "pos");
List<Feature> features = extractor.extract(this.jCas, token);
{% endhighlight %}

This will create a single feature whose name is `"TypePath(Pos)"` and whose value is a part-of-speech tag (e.g. `"JJ"` or `"VBZ"`).

The `TypePathExtractor` can handle more complicated scenarios where the type of a type system feature may be another annotation or other subtype of `TOP` which may in turn have type system features from which we would like to extract features.
In such cases it is helpful to specify a path to the type system feature of interest.
The path is a simple slash delimited sequence of type system feature names of the form `"pathname1/pathname2/..."`.
For example, if you were performing feature extraction on annotations of type `NamedEntityMention` in the ClearTK type system, then you could extract named entity types with the path `"mentionedEntity/entityType"`.

## FeatureFunctionExtractor

There are a variety of scenarios in which it is advantageous to reuse features that have just been extracted to create additional features.  ClearTK provides a `FeatureFunction` interface that has the following method:

{% highlight java %}
public List<Feature> apply(Feature feature)
{% endhighlight %}

ClearTK provides a variety of `FeatureFunction` implementations such as `CapitalTypeFeatureFunction` which generates features that correspond to the kind of capitalization pattern that is seen in the feature (one of "all uppercase", "all lowercase", "initial uppercase", and "mixed case").  You can extract features like these with the following code:

{% highlight java %}
FeatureExtractor1<Token> extractor = new FeatureFunctionExtractor<Token>(new CoveredTextExtractor<Token>(), new CapitalTypeFeatureFunction());
List<Feature> features = extractor.extract(this.jCas, token);
{% endhighlight %}

This will create two features: one corresponding to the covered text and another for the capitalization type of the covered text whose name is `"CapitalType"`.
Technical detail: the name of features generated by `CapitalTypeFeatureFunction` will be more explicit when used with another `FeatureExtractor` that actually gives its features names (i.e. `CoveredTextExtractor` does not give its features names.)
Also, note that `FeatureFunctionExtractor` may be instantiated such that the base features (i.e. the feature generated by the passed in feature extractor) are not included.

### List of FeatureFunction implementations

The following is a list of some of the FeatureFunction implementations provided by ClearTK:

* `CharacterCategoryFeatureFunction` - generates a pattern based on the [Unicode categories](http://unicode.org/reports/tr49/) of each of the characters in the feature text. For example, "A-z0" is an uppercase letter, followed by a dash, followed by a lowercase letter, followed by a digit, and so would get the pattern "LuPdLlNd" as a feature.
* `CharacterNgramFeatureFunction` - creates features corresponding to character ngrams.  This is great for prefix and suffix features corresonding to character bigrams or trigrams.  While the implementation supports a wide variety of possible character ngrams a typical usage would be for something like a trigram suffix which can be defined as: `new CharacterNgramFeatureFunction(Orientation.RIGHT_TO_LEFT, 0, 3)`
* `LowerCaseFeatureFunction` as discussed with `CoveredTextExtractor` above
* `ContainsHyphenFeatureFunction` a trivial implementation that generates a feature if there is a hyphen in the input feature
*` NumericTypeFeatureFunction` characterizes the input feature with respect to whether or not it contains digits

The FeatureFunctionExtractor takes an arbitrary number of FeatureFunction objects to allow a large number of different kinds of features to be generated from a single feature extractor.

## FeatureExtractor1 and FeatureExtractor2

All of the above feature extractors implement an interface called `FeatureExtractor1` which is so named because its only method _extract_ has an arity of 1.
This interface is essential for the `CleartkExtractor` which is described next but we have already seen it in the constructor of `FeatureFunctionExtractor`.
It is also used in `CombinedExtractor1` which simply aggregates some number of `FeatureExtractor1` implementations into a single feature extractor.
This would allow, for example, one to aggregate a `CoveredTextExtractor` and a `TypePathExtractor` into a `CombinedExtractor1` that could then be used to create a single `FeatureFunctionExtractor` in order to perform the feature functions on features resulting from both extractors.

It follows that the interface `FeatureExtractor2` has an arity of 2 for its only method _extract_.
An example of an implementation of `FeatureExtractor2` is the `DistanceExtractor` which creates a feature out of the distance between two annotations where distance is defined as the number of annotations (whose type is of your choosing) are between two annotations.

For most feature extractors you implement on your own it is appropriate to implement one of these two interfaces.

## CleartkExtractor

The `CleartkExtractor` is an extremely powerful and flexible feature extractor that takes an annotation class which determines what kinds of annotations to perform feature extraction on, an implementation of `FeatureExtractor1` (see above), and one or more contexts.
A context represents a location for where to look for annotations to perform feature extraction.
For example, a context initialized by `new Preceding(2)` would tell the `CleartkExtractor` to look at the two annotations to the left of the focus annotation.
This could be used to get preceding part-of-speech tags on tokens, for example.
`CleartkExtractor` provides an `extract` method which allows the context to look for annotations in an unrestricted way.
Another method `extractWithin` takes a "bounds" annotation that restricts the context to look for annotations within a fixed boundary specified by that annotation.
A common scenario is to perform `extractWithin` on token annotations using a sentence annotation as the "bounds" annotation.

Here are four examples provided in the javadocs for CleartkExtractor:

### Covered text example

The following code example creates a feature extractor that gets the text of the 2 tokens before a focus token.
The names of the two extracted features will be `Preceding_0_2_1` and `Preceding_0_2_0` and the values will be the respective covered texts of the two tokens preceding the focus token.

{% highlight java %}
CleartkExtractor<Token, Token> extractor = new CleartkExtractor<Token, Token>(
    Token.class,
    new CoveredTextExtractor<Token>(),
    new Preceding(2));

List<Feature> features = extractor.extract(jCas, focusToken);
{% endhighlight %}

### Part-of-speech tags example

This example feature extractor gets the part-of-speech tags of the 3 tokens after a focus annotation of type `Chunk`.
Note that this extractor is generically typed to extract features for annotations of type `Chunk` (i.e. we will pass the extractor chunks) using annotations of type `Token` that follow our chunk annotations.
The names of the three extracted features will be `"Following_0_3_0_TypePath(Pos)"`, `"Following_0_3_1_TypePath(Pos)"`, and `"Following_0_3_2_TypePath(Pos)"` and the values of the three features will be the respective part-of-speech tags given to the respective tokens.

{% highlight java %}
CleartkExtractor<Chunk, Token> extractor = new CleartkExtractor<Chunk, Token>(
    Token.class,
    new TypePathExtractor<Token>(Token.class, "pos"),
    new Following(3));

    List<Feature> features = extractor.extract(jCas, chunk);
    List<Feature> features2 = extractor.extractWithin(jCas, chunk, sentence);
{% endhighlight %}

As noted above, the `extractWithin` provides bounds on which the `Following` context can look for token annotations for feature extraction.

### Bag-of-words example

This feature extractor gets the tokens after a focus annotation, beginning 2 after and ending 5 after, as a bag of words.  Each of the tokens will have the name `"Bag_Following_2_5"` and the values will be the covered texts of the three tokens that fall in the specified range.

{% highlight java %}
CleartkExtractor<Chunk, Token> extractor = new CleartkExtractor<Chunk, Token>(
    Token.class,
    new CoveredTextExtractor<Token>(),
    new Bag(new Following(2, 5)));
{% endhighlight %}

### N-gram example

This feature extractor gets an ngram concatenating the lemma of the word preceding the focus token and the focus token.  This feature extractor will extract a single feature whose name is `"Ngram_Preceding_0_1_Focus_TypePath(LemmaValue)"` and whose value corresponds to an ngram consisting of the preceding token's lemma and the focus token's lemma (e.g. "this feature" for the second word of this sentence.)

{% highlight java %}
CleartkExtractor<Token, Token> extractor = new CleartkExtractor<Token, Token>(
    Token.class,
    new TypePathExtractor<Token>(Token.class, "lemma/value"),
    new Ngram(new Preceding(1), new Focus()));
{% endhighlight %}

### Discussion

Note that for each of the above examples only one context object was passed into the `CleartkExtractor` constructor.  However, it is common to pass in several contexts to the constructor.

## Trainable feature extractors

Please see the separate tutorial on [trainable feature extractors](trainable_feature_extractors.html) for building feature extractors which require "training" of some kind.