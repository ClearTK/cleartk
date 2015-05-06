---
title: Frequently Asked Questions
menu: Documentation
menu-order: 2
---

# Frequently Asked Questions #
{:.no_toc}

* Automatically replaced with table of contents
{:toc}

## Configuration Parameters ##

### How do I find the UIMA configuration parameters for annotators in ClearTK? ###

For `CleartkAnnotator` and `CleartkSequenceAnnotator` parameters, see the description on the [ClearTK-ML module page](cleartk_ml.html).

In general, most annotators that take parameters will have public static fields that start with *`PARAM`*. For example:

* `org.cleartk.classifier.CleartkAnnotator.PARAM_IS_TRAINING`
* `org.cleartk.token.stem.snowball.SnowballStemmer.PARAM_STEMMER_NAME`
* `org.cleartk.corpus.timeml.TempEval2013Writer.PARAM_OUTPUT_DIRECTORY`

For more information on how to set configuration parameters, refer to the uimaFIT documentation for [ConfigurationParameterFactory](http://uimafit.googlecode.com/svn/tags/uimafit-parent-1.2.0/apidocs/index.html)

## Setting Up Classifiers ##

Most ClearTK users should probably start by using the ClearTK bindings for LIBLINEAR or OpenNLP Maxent, which should work out of the box.
However some of the other machine learning libraries supported by ClearTK require installation of separate executables.
Instructions for each such library is given below.

### How do I install SVMLight? ###

Download:  The executables can be downloaded from [the SVMLight home page](http://www.cs.cornell.edu/People/tj/svm_light/).

Installation: Add the SVMLight binary executables (e.g., svm_learn.exe) to your system path.
Verify that they are on your path by typing `svm_learn` from the command line.
Please refer to the SVMlight website for additional details.

### How do I install SVMRank? ###

[SVMrank](http://www.cs.cornell.edu/People/tj/svm_light/svm_rank.html) is a different implementation of an SVMLight training algorithm, for training Ranking SVMs.  

Download: The executables can be downloaded from [the SVMlight SVMrank page](http://www.cs.cornell.edu/People/tj/svm_light/svm_rank.html)

Installation: Follow the instructions that come with the SVMrank download.
Make sure that the SVMlight binaries (`svm_rank_learn` / `svm_rank_classify`) are in the system path when running ClearTK.

### How do I install TK-SVMlight? ###
[SVM-LIGHT-TK](http://disi.unitn.it/moschitti/Tree-Kernel.htm) adds Tree Kernel functionality to svm-light.  This is needed to run code within the cleartk-ml-tksvmlight module.

Download: The source code can be downloaded from [the TK-SVMlight home page](http://disi.unitn.it/moschitti/Tree-Kernel.htm)

Installation: Follow the instruction to build the source code.
Because svm-light-TK builds binaries with the same names as svm-light, you will need to rename `svm_classify` to `tk_svm_classify` and `svm_learn` to `tk_svm_learn`.
After this is done, ensure these two binaries are in your system path when running ClearTK.

## Classifier Training Parameters ##

Training arguments depend on the specific classifier library.  For detailed argument information refer to the links below.  *Note:* ClearTK will provide the arguments for the input training file.

### What parameters does LIBLINEAR accept? ###

Refer to [the LIBLINEAR home page](http://www.csie.ntu.edu.tw/~cjlin/liblinear/).

### What parameters does LIBSVM accept? ###

Refer to the  [the LIBSVM home page](http://www.csie.ntu.edu.tw/~cjlin/libsvm/).

### What parameters do the Mallet classifiers accept? ###

The first argument is a factory class found in org.cleartk.classifier.factory:

* C45
* MaxEnt
* MCMaxEnt
* NaiveBayes

Each of these take their own parameters.  Refer to the factory methods for details.

### What parameters does OpenNLP MaxEnt accept? ###

OpenNLP MaxEnt takes two optional parameters iterations and cutoff.  The eventfie is provided by cleartk.  For more information visit the [ RealValueFileEventStream documentation](http://incubator.apache.org/opennlp/documentation/1.5.2-incubating/apidocs/opennlp-maxent/opennlp/model/RealValueFileEventStream.html)

### What parameters does SVM-light accept? ###

Refer to the [the SVMLight home page](http://www.cs.cornell.edu/People/tj/svm_light/)

### What parameters does SVM-rank accept? ###

Refer to the [the SVMrank home page](http://www.cs.cornell.edu/People/tj/svm_light/svm_rank.html)