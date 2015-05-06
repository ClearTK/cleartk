---
title: ClearTK-TimeML
menu: Documentation
menu-order: 4
---

# ClearTK-TimeML

The ClearTK-TimeML module provides annotators for extracting events, times and temporal relations. This module includes code and pre-built models for the top ranking temporal relation identification system in [TempEval 2013](http://www.cs.york.ac.uk/semeval-2013/task1/). Roughly, the system identifies events and times with around 80% precision and 75% recall, and identifies temporal relations with around 85% accuracy.

The ClearTK-TimeML module also includes some code for other related tasks, such as [TempEval 2007](http://www.timeml.org/tempeval/), [TempEval 2010](http://www.timeml.org/tempeval2/) and [the annotated data of (Bethard et. al. 2007)](http://www.cis.uab.edu/bethard/data.html#verb-clause-temporal-relations), though pre-built models are not included for these tasks, and this code is less actively maintained.

## Running the pre-built models

If you just want to run the pre-built models on a piece of text, then the [TimeMlAnnotate](https://github.com/ClearTK/cleartk/blob/master/cleartk-timeml/src/main/java/org/cleartk/timeml/TimeMlAnnotate.java) class is the place to start. This class can be run at the command line, and takes an input file (or directory of input files) and an output directory to which the events, times and temporal relations will be output in [TimeML format](http://timeml.org/site/publications/timeMLdocs/timeml_1.2.1.html).

However, most users will probably want to add the annotators to their own pipeline and then work directly with the CAS rather than writing out TimeML documents. The key elements of the TimeML pipeline are listed in the code:

    ...
    TimeAnnotator.FACTORY.getAnnotatorDescription(),
    TimeTypeAnnotator.FACTORY.getAnnotatorDescription(),
    EventAnnotator.FACTORY.getAnnotatorDescription(),
    EventTenseAnnotator.FACTORY.getAnnotatorDescription(),
    EventAspectAnnotator.FACTORY.getAnnotatorDescription(),
    EventClassAnnotator.FACTORY.getAnnotatorDescription(),
    EventPolarityAnnotator.FACTORY.getAnnotatorDescription(),
    EventModalityAnnotator.FACTORY.getAnnotatorDescription(),
    ...
    TemporalLinkEventToDocumentCreationTimeAnnotator.FACTORY.getAnnotatorDescription(),
    TemporalLinkEventToSameSentenceTimeAnnotator.FACTORY.getAnnotatorDescription(),
    TemporalLinkEventToSubordinatedEventAnnotator.FACTORY.getAnnotatorDescription(),
    ...

These annotators will generate `Event`, `Time` and `TemporalLink` annotations in the CAS that you can then use as you like within your pipeline. When creating your own pipelines including these annotators, be sure to check the full pipeline in TimeMlAnnotate, as the models require a variety of pre-processing annotators to provide sentences, tokens, part-of-speech tags, stems, syntactic parses, etc.

## Annotators

As shown in the previous section, the ClearTK-TimeML module provides annotators for various tasks related to identifying and categorizing events, time expressions and temporal relations. All of these are machine learning models trained with a small number of linguistic features. Here is a brief description of the main tasks performed by each module:

* `TimeAnnotator` - Identifies time expressions in the text and adds `Time` annotations to the CAS
* `TimeTypeAnnotator` - Sets the `timeType` feature of `Time` annotations to `DATE`, `TIME`, `DURATION`, etc.
* `EventAnnotator` - Identifies event expressions in the text and adds `Event` annotators to the CAS
* `EventTenseAnnotator` - Sets the `tense` feature of `Event` annotations to `PAST`, `PRESENT`, `FUTURE`, etc.
* `EventAspectAnnotator` - Sets the `aspect` feature of `Event` annotations to `PERFECTIVE`, `PROGRESSIVE`, etc.
* `EventClassAnnotator` - Sets the `eventClass` feature of `Event` annotations to `OCCURRENCE`, `REPORTING`, `PERCEPTION`, etc.
* `EventPolarityAnnotator` - Sets the `polarity` feature of `Event` annotations to `NEG` or `POS`.
* `EventModalityAnnotator` - Sets the `modality` feature of `Event` annotations to `will`, `should`, `must`, etc.
* `TemporalLinkEventToDocumentCreationTimeAnnotator` - Identifies temporal relations between `Event` annotations and the `DocumentCreationTime` annotation and adds corresponding `TemporalLink` annotations to the CAS. **Note**: This annotator requires that the CAS already contain a `DocumentCreationTime`, which you must supply. The pipeline in `TimeMlAnnotate` creates an empty (fake) document creation time, but for any real application, you should create a `DocumentCreationTime` in the CAS based on your file metadata.
* `TemporalLinkEventToSameSentenceTimeAnnotator` - Identifies temporal relations between `Event` and `Time` annotations in the same sentence (for some, not all, syntactic relations between events and times) and adds corresponding `TemporalLink` annotations to the CAS.
* `TemporalLinkEventToSubordinatedEventAnnotator` - Identifies temporal relations between `Event` annotations where one `Event` is syntactically dominated by another, and adds corresponding `TemporalLink` annotations to the CAS.