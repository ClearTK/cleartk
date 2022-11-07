# ClearTK

![example workflow](https://github.com/ClearTK/cleartk/actions/workflows/build-snapshot.yml/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dkpro.core/dkpro-core/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/org.cleartk/cleartk)

## Introduction

ClearTK provides a framework for developing statistical natural language 
processing (NLP) components in Java and is built on top of Apache UIMA. It is 
developed by the Center for Computational Language and Education Research 
(CLEAR) at the University of Colorado at Boulder.

ClearTK is built with Maven and we recommend that you build your project that
depends on ClearTK with Maven.  This will allow you to add dependencies for only
the parts of ClearTK that you are interested and automatically pull in only 
those dependencies that those parts depend on.  The zip file you have downloaded
is provided as a convenience to those who are unable to build with Maven.  It 
provides jar files for each of the sub-projects of ClearTK as well as all the 
dependencies that each of those sub-projects uses.  To use ClearTK in your 
Java project, simply add all of these jar files to your classpath.  If you are
only interested in one (or a few) sub-project of ClearTK, then you may not want
to add every jar file provided here.  Please consult the maven build files to 
determine which jar files are required for the parts of ClearTK you want to use.   

Please see the section titled "Dependencies" below for important licensing information.

## License

Copyright (c) 2007-2014, Regents of the University of Colorado 
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE. 

## Dependencies

ClearTK depends on a variety of different open source libraries that are
redistributed here subject to the respective licensing terms provided by 
each library.  We have been careful to use only libraries that are 
commercially friendly.  Please see the notes below for exceptions.  For a 
complete listing of the dependencies and their respective licenses please
see the file licenses/index.html.

## GPL Dependencies

ClearTK has two sub-projects that depend on GPL licensed libraries: 
 * cleartk-syntax-berkeley
 * cleartk-stanford-corenlp
Neither of these projects nor their dependencies are provided in this release.
To obtain these projects, please manually download them from our googlecode
hosted maven repository:

http://cleartk.googlecode.com/svn/repo/org/cleartk/cleartk-syntax-berkeley/
http://cleartk.googlecode.com/svn/repo/org/cleartk/cleartk-stanford-corenlp/

## SVMLIGHT

ClearTK also has two projects called cleartk-ml-svmlight and cleartk-ml-tksvmlight
which have special licensing considerations. The ClearTK project does not 
redistribute SVMlight. ClearTK does, however, facilitate the building of SVMlight 
models via the ClassifierBuilder interface. In order to use the implementations of
this interface to good effect you will need to have SVMlight installed on your
machine. The ClassifierBuilders for SVMlight simply call the executable "svm_learn"
provided by the SVMlight distribution. ClearTK does not use SVMlight at
classification time - it only uses the models that are build by SVMlight. Instead,
ClearTK provides its own code for classification that makes use of an SVMlight
generated model.  This code is provided with ClearTK and is available with the
above BSD license as is all of the other code written for ClearTK. Therefore, be
advised that while ClearTK is not required (or compelled) to redistribute the code
or license of SVMlight or to comply with it (i.e. the noncommercial license
provided by SVMlight is not compatible with our BSD License) - it would be very
difficult to use the SVMlight wrappers we provide in a commercial setting without
obtaining a license for SVMlight directly from its authors.  

## LGPL

The cleartk-ml-mallet project depends on Mallet (http://mallet.cs.umass.edu/),
which depends on trove4j (http://trove.starlight-systems.com/), which is
released under the LGPL license. If you do not need Mallet classifiers and would
like to avoid the LGPL license, you can omit the cleartk-ml-mallet dependency.
