---
title: Home
order: 1
---

<div class="text-center jumbotron">

# ClearTK #

## Machine Learning for UIMA ##

ClearTK is a framework for developing machine learning and natural language processing components within the [Apache Unstructured Information Management Architecture](http://uima.apache.org).

[View on GitHub](https://github.com/ClearTK/cleartk){: class="btn btn-primary btn-lg" role="button"}
[Documentation](docs.html){: class="btn btn-info btn-lg" role="button"}

</div>

<div class="row">
<div class="col-md-4">

### Train Classifiers ###

ClearTK provides a common interface and wrappers for popular machine learning libraries such as [SVMlight](http://svmlight.joachims.org/), [LIBSVM](http://www.csie.ntu.edu.tw/~cjlin/libsvm/), [LIBLINEAR](http://www.csie.ntu.edu.tw/~cjlin/liblinear/), [OpenNLP MaxEnt](http://opennlp.apache.org/), and [Mallet](http://mallet.cs.umass.edu/).
ClearTK's interfaces support simple classification, sequence classification, BIO-style chunking classification and more.

</div>
<div class="col-md-4">

### Extract Features ###

ClearTK provides a rich feature extraction library that can be used with any of the machine learning classifiers. Under the covers, ClearTK understands each of the native machine learning libraries and translates your features into a format appropriate to whatever model you're using.

</div>
<div class="col-md-4">

### Parse Language ###
ClearTK provides UIMA wrappers for common natural language processing (NLP) tools including the Snowball stemmer, OpenNLP tools, MaltParser dependency parser, and Stanford CoreNLP.
And it provides UIMA readers for corpora including the Penn Treebank, ACE 2005, CoNLL 2003, Genia, TimeBank and TempEval.

</div>
</div>