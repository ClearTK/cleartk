---
title: Documentation
order: 2
---

<div class="row">
<div class="col-md-6">

# API Documentation

API documentation (Javadoc) for the latest release:

[ClearTK 2.0.0 API](http://cleartk.github.io/cleartk/apidocs/2.0.0/){: class="btn btn-info btn-lg" role="button"}

You can also view the [ClearTK 1.4.1 API](http://cleartk.github.io/cleartk/apidocs/1.4.1/) documentation for the previous release, though this release is no longer supported.

</div>
<div class="col-md-6">

# Mailing Lists

The best place to ask questions about ClearTK is the main mailing list:

[ClearTK-Users](https://groups.google.com/group/cleartk-users/){: class="btn btn-primary btn-lg" role="button"}

We also have a separate mailing list, [ClearTK-Developers](https://groups.google.com/group/cleartk-developers/), primarily for those with commit access to the ClearTK repository.

</div>
</div>

# Maven Setup

ClearTK is built with [Apache Maven](http://maven.apache.org/) and we strongly recommend that you also build your ClearTK-based projects with Maven. See the [Maven Getting Started Guide](http://maven.apache.org/guides/getting-started/) if you are not already familiar with Maven.

To use ClearTK in your Maven-based project, add something like the following to your pom.xml:

    <properties>
      <cleartk.version>2.0.0</cleartk.version>
    </properties>
    ...
    <dependencies>
      <dependency>
        <groupId>org.cleartk</groupId>
        <artifactId>cleartk-ml</artifactId>
        <version>${cleartk.version}</version>
      </dependency>
      <dependency>
        <groupId>org.cleartk</groupId>
        <artifactId>cleartk-ml-liblinear</artifactId>
        <version>${cleartk.version}</version>
      </dependency>
      ...
    </dependencies>

The example above declares dependencies on `cleartk-ml`, the main ClearTK machine learning interfaces, and `cleartk-ml-liblinear`, one of the more efficient implementations of these interfaces. For the full listing of ClearTK modules, see below.

# ClearTK Modules

## Machine learning ##

* cleartk-ml: the core machine learning APIs: classifiers, features, instances, feature extractors, feature encoders, etc.
* cleartk-ml-crfsuite: wrappers to use [CRFsuite](http://www.chokkan.org/software/crfsuite/) as a ClearTK classifier
* cleartk-ml-liblinear: wrappers to use [LIBLINEAR](http://www.csie.ntu.edu.tw/~cjlin/liblinear/) as a ClearTK classifier
* cleartk-ml-libsvm: wrappers to use [LIBSVM](http://www.csie.ntu.edu.tw/~cjlin/libsvm/) as a ClearTK classifier
* cleartk-ml-mallet: wrappers to use [MALLET](http://mallet.cs.umass.edu/) and [GRMM](http://mallet.cs.umass.edu/grmm/index.php) as ClearTK classifiers
* cleartk-ml-opennlp-maxent: wrappers to use [OpenNLP-maxent](http://incubator.apache.org/opennlp/) as a ClearTK classifier
* cleartk-ml-svmlight: wrappers to use [SVM-light](http://svmlight.joachims.org/) as a ClearTK classifier
* cleartk-ml-tksvmlight: wrappers to use [Tree Kernels in SVM-light](http://disi.unitn.it/moschitti/Tree-Kernel.htm) as a ClearTK classifier

## Evaluation ##

* cleartk-eval: classes for evaluating pipelines - train/test, cross-validation, etc.

## Type system ##

* cleartk-type-system: the official UIMA type system for ClearTK
* cleartk-corpus: readers and writers that operate on corpora using the ClearTK type system, including support for ACE 2005, CoNLL 2003, CoNLL 2005, Genia, PennTreebank, PropBank, TimeML, etc.
* cleartk-feature: feature extractors for the ClearTK type system, e.g. for extracting features from ClearTK tokens or ClearTK parse trees

## Wrappers for external components ##

* cleartk-snowball: a wrapper around the Snowball stemmer
* cleartk-opennlp-tools: wrappers around the [OpenNLP](http://incubator.apache.org/opennlp/) sentence segmenter, part-of-speech tagger, and syntactic parser
* cleartk-berkeleyparser: a wrapper around the [Berkeley syntactic parser](http://code.google.com/p/berkeleyparser/)
* cleartk-clearnlp: a wrapper around [ClearNLP](http://code.google.com/p/clearnlp), the successor to clearparser.  Includes wrappers for its tokenizer, POS tagger, morphological analyzer (lemmatizer), dependency parser, and semantic role labeler.
* cleartk-maltparser: a wrapper around the [Malt dependency parser](http://maltparser.org/)
* cleartk-stanford-corenlp: a wrapper around the [Stanford CoreNLP](http://nlp.stanford.edu/software/corenlp.shtml) sentence segmenter, tokenizer, part-of-speech tagger, named-entity tagger, syntactic parser, dependency parser and coreference resolution system.

## Home-grown components ##

* cleartk-token: sentence segmenters, tokenizers and part-of-speech taggers, including a segmenter based on java.text.BreakIterator, a PennTreebankTokenizer
* cleartk-timeml: models for extracting events, times and temporal relations, trained on TempEval 2013 data

## Utility modules ##

* cleartk-test-util: test case base classes and utilities for testing licenses, parameter names, etc.
* cleartk-util: simple type-system agnostic readers and writers, and various utilities used by other ClearTK modules

## Example code ##

*Note: This module is only provided as source code. The code may change at any time. Never add cleartk-examples as a dependency.*

* cleartk-examples: example code for part-of-speech tagging, BIO-chunking, bag-of-words document classification, etc.
