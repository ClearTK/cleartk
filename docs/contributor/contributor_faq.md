---
title: Frequently Asked Questions
menu: Contributors
menu-order: 1
---

# Frequently Asked Questions for Contributors
{: .no_toc}

* Automatically replaced with table of contents
{:toc}

## Basics

### I would like to contribute to ClearTK. What now?

ClearTK has benefited from contributions from a growing number of people and we would like to encourage new contributors to help us make ClearTK better.
Because of the limited scope of the BSD license with respect to defining contributions and contributors, we require that you agree to a contributor's license.
If you represent a corporation or institution, then please refer to the [Corporate Contributor Agreement](https://github.com/ClearTK/cleartk/blob/master/doc/admin/CU%20Corporate%20Contributor%20License%20Agreement.doc?raw=true).
If you represent yourself, then please refer to the [Individual Contributor Agreement](https://github.com/ClearTK/cleartk/blob/master/doc/admin/CU%20Individual%20Contributor%20License%20Agreement%20click%20through.doc?raw=true).
If you are a paid employee of the University of Colorado at Boulder, then the contributor's agreement does not apply to you.
Here are the instructions for satisfying the individual contributor's license agreement:

1.  Email [cleartk-developers@googlegroups.com](mailto:cleartk-developers@googlegroups.com) and express interest in being a contributor.
    We will email back an email address for the CU Tech Transfer Office.
2.  Compose an email to CU Tech Transfer Office and attach the individual contributor's license agreement (as is, no need to fill it out).
    The body of your message should say something like this: "I agree to the terms of the attached contributor's license agreement"

Once you have followed these steps, then you can contribute code to ClearTK.

### How do I make my first code contribution?

First [become a contributor](#i-would-like-to-contribute-to-cleartk-what-now). Then:

1.  Create an issue on the [ClearTK issue tracker](https://github.com/ClearTK/cleartk/issues) that describes in detail the problem you plan to solve.
2.  Fork the [ClearTK repository on GitHub](https://github.com/ClearTK/cleartk).
3.  [Create a branch](https://help.github.com/articles/creating-and-deleting-branches-within-your-repository/) with the name of your issue, e.g., "issue/432"
4.  [Clone your fork locally](http://git-scm.com/book/en/v2/Git-Basics-Getting-a-Git-Repository) and [check out your branch](http://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging)
5.  Make the necessary fixes to ClearTK.
6.  [Run the tests](#im-about-to-commit-code-how-do-i-run-all-the-tests).
7.  [Push your changes](http://git-scm.com/book/en/v2/Git-Basics-Working-with-Remotes#Pushing-to-Your-Remotes) to your fork on GitHub.
8.  [Create a pull request](https://help.github.com/articles/using-pull-requests/).
    Be sure that your pull request includes [the proper syntax to associate your commits with the issue](https://help.github.com/articles/closing-issues-via-commit-messages/), e.g., "Fixes #432"

Once you have submitted your pull request, a core developer will review it and either commit it directly, or comment on the pull request to request additional changes.

### I would like to become a ClearTK committer.  What now?

After you have successfully [contributed code](#how-do-i-make-my-first-code-contribution) several times you may be considered for commit privileges to the ClearTK repository.
Please email [cleartk-developers@googlegroups.com](mailto:cleartk-developers@googlegroups.com) if you have interest in becoming a committer.

### I'm a committer. What mailing lists should I subscribe do?

You should subscribe to:

* [cleartk-users](http://groups.google.com/group/cleartk-users), the mailing list where users of ClearTK ask questions.
  If you're a committer, you're knowledgeable enough to help out here.

* [cleartk-developers](http://groups.google.com/group/cleartk-developers), the main mailing list for ClearTK contributors and committers.
  This list is for general discussion of the ongoing development of the ClearTK APIs.
  Discussions about specific, numbered issues should take place in the issue tracker and not in this list.

You should also examine your [GitHub notification settings](https://github.com/settings/notifications) to make sure you get appropriate notifications when ClearTK issues are filed.

## Writing Code

### How do I throw exceptions in ClearTK annotators?

Most ClearTK annotators are UIMA annotators and therefore you should be throwing UIMA exceptions such as ResourceInitializationException and AnalysisEngineProcessException.
UIMA has [a complicated scheme for throwing exceptions](http://uima.apache.org/d/uimaj-2.6.0/tutorials_and_users_guides.html#ugr.tug.aae.throwing_exceptions_from_annotators) that involves creating a properties file with templates for your exception messages, and as a result it's impossible to throw an exception with the usual Java paradigm: `throw new SomeException("my message here")`.

To make throwing UIMA-style exceptions easier, ClearTK has a few of its own exceptions that subclass `ResourceInitializationException` and `AnalysisEngineProcessException` and that provide utility methods for creating exceptions with appropriately formatted messages:

* `CleartkInitializationException` - a subclass of `ResourceInitializationException` available in cleartk-util that is most often useful in the `initialize(UimaContext)` methods of annotators.
  It provides methods such as `parameterLessThan`, which indicates that a parameter value was too small, or `invalidParameterValueSelectFrom`, which indicates that a parameter should have been one of a set of known values.

* `CleartkProcessingException` - a subclass of `AnalysisEngineProcessException` available in cleartk-ml that is thrown by `Classifier.classify`, `DataWriter.write`, etc.
  Since it's an `AnalysisEngineProcessException`, there is no need to catch and re-throw this exception in annotators.

* `CleartkEncoderException` - a subclass of `CleartkProcessingException` in cleartk-ml that is thrown by feature extractors.
  It provides methods such as `noAnnotationInWindow`, which indicates that no annotation was found in the window specified to the feature extractor.

* `CleartkExtractorException` - a subclass of `CleartkProcessingException` in cleartk-ml that is thrown by feature encoders.
  It provides methods such as `noMatchingEncoder`, which indicates that no encoder has been registered with the `DataWriter` for handling the particular type of feature value.

If you find that you need new exception messages, see the source for these classes - they list the name of the properties file and show how to properly invoke UIMA exception constructors.

### How should I name UIMA configuration parameters for ClearTK annotators?

Naming of ClearTK configuration parameters is enforced by [ParametersTestUtil.java](https://github.com/ClearTK/cleartk/blob/master/cleartk-test-util/src/main/java/org/cleartk/test/util/ParametersTestUtil.java).
Parameter naming convention requires that parameters:

* are all uppercase
* begin with `PARAM_`
* have a camel-cased variable corresponding to the remainder of the name.

Example:

{% highlight java %}
public static final String PARAM_DATA_WRITER_CLASS_NAME = "dataWriterClassName";

@ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS_NAME)
private String dataWriterClassName;
{% endhighlight %}

### What is our deprecation policy?

We try to follow [Semantic Versioning](http://semver.org/), which means that:

* Version x.y.(z+1) is backwards compatible with version x.y.z, and includes only bugfixes.
  E.g., version 2.0.1 will contain only bugfixes over version 2.0.0.

* Version x.(y+1).\* is backwards compatible with version x.y.\*, and may include new features and new deprecations of public APIs.
  E.g., version 2.1.0 may contain new APIs and may deprecate some old APIs.

* Version (x+1).\*.\* is not backwards compatible with version x.\*.\*, and typically removes any APIs that were deprecated in version x.\*.\*.
  E.g., version 2.0.0 removed all the deprecated classes from version 1.4.1, in addition to making several other backwards incompatible API changes.

## Running Tests

### I'm about to commit code. How do I run all the tests?

If you are making changes that touch lots of modules, then it is important to run all (that is, most) the tests in ClearTK using maven.
This can be done from the command line by typing the following:

    mvn clean
    mvn test

### How do I skip failing tests that I want to skip?

You can always add `-DskipTests` to any maven command to skip all of the tests.
However, we also provide an additional flags in ClearTK for skipping various groups of tests that developers might want to skip.

To skip tests that run for a long time, run:

    mvn test -Dcleartk.skipTests=long

To skip tests that require several GB of memory, run:

    mvn test -Dcleartk.skipTests=bigMem

To skip tests that require binary executables on your system path (which you may not have installed), run:

    mvn test -Dcleartk.skipTests=svmlight,tksvmlight

A useful shorthand is:

    mvn test -Dcleartk.skipTests=common

Which is equivalent to running the tests with:

    mvn test -Dcleartk.skipTests=long,bigMem,tksvmlight

Running tests with:

    mvn test -Dcleartk.skipTests=all

Is equivalent to running the tests with:

    mvn test -Dcleartk.skipTests=long,bigMem,svmlight,tksvmlight

Note that 'all' in this context means that all of the tests that can be skipped are skipped.
If you actually want to skip all tests, then simply specify `-DskipTests` instead.

## Committing Code

### I'm about to commit code that closes an issue. What should my commit message look like?

The GitHub issue tracker [allows issues to be closed via commit messages](https://help.github.com/articles/closing-issues-via-commit-messages/) by including text like `Fixes #NNN`.

## Building Models

### How do I rebuild the models provided by ClearTK (e.g. after cleartk-ml refactoring)?

Firstly, not all models redistributed are ClearTK-built models.
For example, the models in the cleartk-opennlp-tools project are provided by OpenNLP and should not need to be rebuilt because of a refactoring to ClearTK.

The following list gives the classes that can be used to rebuild models distributed with ClearTK:

* All timeml models can be built with freely available resources using the instructions in the [MODELS](https://github.com/ClearTK/cleartk/blob/master/cleartk-timeml/MODELS) file.

## Adding Dependencies

### What are the guidelines for adding a third party dependency?

All new third party dependencies need to be discussed by the ClearTK development team.
If you are a committer and want to add a new dependency, then please send an email to [cleartk-developers@googlegroups.com](mailto:cleartk-developers@googlegroups.com) so that we can discuss it.
Generally speaking, the things we will consider for allowing a new dependency include:

* License.
  The preferred licenses are Apache Software License (ASL) and BSD.
  The Common Public License (CPL) and Eclipse Public License are also acceptable.
  Third party licenses using GPL and LGPL licenses are generally speaking not allowed.
  Any exceptions to this must be strictly isolated to a ClearTK sub-project so that distributions that do not include dependencies GPL or LGPL license dependencies can be made.
  Such sub-projects will actually be distributed under the GPL or LGPL in compliance with those licenses.
  See the ClearTK subproject [cleartk-berkeleyparser](https://github.com/ClearTK/cleartk/tree/master/cleartk-berkeleyparser) as an example.
  Certainly, we will not allow a GPL licensed third-party dependency to be added to a project like cleartk-util because nearly every other project in ClearTK depends on it.

* Scope.
  We have attempted to limit the scope of third-party dependencies as much as possible so that only the projects that actually require the dependency have it.
  If you are developing your own sub-project in ClearTK that no (or few) other projects depend on, then adding a new dependency is a less significant decision.
  However, adding a dependency to a project like cleartk-util requires more deliberation because nearly all the other ClearTK sub-projects depend on it.
  Keeping dependencies isolated is very important for people who want to use only a small portion of ClearTK and do not want a lot of unnecessary third-party "baggage" to come along with the part of ClearTK they want to use.

* Provenance.
  We would prefer to add third-party dependencies that are produced by people and organizations that we are familiar with.
  For example, projects from apache.org or codehaus.org are much more likely to be reliable than a random project hosted by Google Code or SourceForge.
  Well known projects tend to have a much more consistent quality of software and have unambiguous license agreements.

### How do I add a dependency on a third party module that is not in Maven Central?

You should upload that dependency to Maven Central using the [guide for uploading third-party artifacts to the central repository](http://central.sonatype.org/pages/third-party-components.html).

## Making a Release

### What should the next version of ClearTK be?

ClearTK employs [Semantic Versioning](http://semver.org/) such that a version number consists of three numbers delimited by a dot (e.g. 2.1.0).
The first number is reserved for backwards incompatible changes and/or major milestone releases.
The second number is for versions that have new APIs or backwards compatible extensions of the existing APIs.
The third number is for versions that consist primarily of bug fixes.
So, if the last version of ClearTK was "2.1.0" and you have only fixed bugs since then, then the next version of ClearTK should be "2.1.1".
If any new features have been added, then the next version should instead be "2.2.0".

### How do I release ClearTK?

ClearTK modules are released to Maven Central using the Maven release plugin and the Sonatype OSS Maven Repository.
You should read the [Sonatype OSSRH Guide](http://central.sonatype.org/pages/ossrh-guide.html) if you have not already.

#### Ensure you have all the prerequisities

* Obtain ClearTK committer access.  This is granted by one of the project owners.
* Make sure you are using Maven 3.
* Create a [Sonatype JIRA Account](http://central.sonatype.org/pages/ossrh-guide.html#create-a-ticket-with-sonatype).
* Create a `~/.m2/settings.xml` with your Sonatype username and password as per [Sonatype OSSRH Guide](http://central.sonatype.org/pages/apache-maven.html#distribution-management-and-authentication).
* If not already installed, [download tools to generate GnuPG (gpg) keys](http://www.gnupg.org/).
* Generate a gpg key with the e-mail address specified during creation of the Sonatype JIRA Account.
  If you plan on releasing from a remote system via ssh, it is best if you generate the keys on your local machine and then import them on the remote one.

      gpg --gen-key

* Upload your key to the public pool, so that it is available when when you go to close issues at Sonatype

      gpg --keyserver hkp://pool.sks-keyservers.net --send-keys  <KEY>

#### Decide on a release version

First, take a look at what changes have been made to the module since the last release:

    git log

Using the [ClearTK versioning guidelines](#what-should-the-next-version-of-cleartk-be), decide what the next version number should be.
You don't have to modify the pom.xml at this point - the release process can do that for you.

#### Do a dry run of the release preparation

Now, use the Maven release plugin to see what tags and pom files will be created by the release process:

    mvn release:prepare -DdryRun=true

The process will prompt you for the version number which you decided upon above.
Note that the default versions are only appropriate for bugfix releases.

The `mvn release:prepare -DdryRun=true` command produces a `release.properties` file and several `pom.xml.next` and `pom.xml.tag` files.
Inspect these to ensure that the poms for the release and the next development versions look ok
When done, remove these files so you can prepare for real:

    mvn release:clean

#### Release the modules to the Sonatype staging repository

Then release the modules to the Sonatype OSS staging repository:

    mvn release:prepare
    mvn release:perform -DlocalCheckout=true

The perform command will actually build and release the files to the Sonatype OSS staging repository.
**If you have problems with authentication, make sure you have [configured your settings.xml](http://central.sonatype.org/pages/apache-maven.html#distribution-management-and-authentication)**.

#### Release the modules to Maven Central

At this point, **you have not yet released anything to Maven Central**.
You have simply released modules to the Sonatype OSS staging repository.
To actually release the modules from the staging repository to Maven Central, [you must "Close" and "Release" the staging repository](http://central.sonatype.org/pages/releasing-the-deployment.html) at [oss.sonatype.org](https://oss.sonatype.org).

#### Generate and upload the Javadoc documentation

Next, check out the version of `cleartk` that you just released so that you can generate the online javadoc documentation:

    git checkout cleartk-<version>

This will leave you in 'detached HEAD' state, but that's okay because you won't be committing anything.

Now generate the aggregate Javadoc using the maven-javadoc-plugin:

    mvn javadoc:aggregate

This will create the Javadocs in `target/site/apidocs`. Open `target/site/apidocs/index.html` and make sure everything looks right. Then move the javadocs to the `apidocs/<version>` directory:

    mv target/site/apidocs apidocs/<version>

Then switch back to the master branch, commit the apidocs directory to the repository and push your changes:

    git checkout master
    git add apidocs/<version>
    git commit -m "Adding apidocs for cleartk-<version>"
    git push

#### Update the website

Update the "ClearTK `<version>` API" link on the ClearTK home page.

Update the version in the Maven snippet on the ClearTK documentation page.

#### Announce the release

Make sure that the modules have propagated to Maven Central:

[http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.cleartk%22](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.cleartk%22)

Then you can send an email to `cleartk-users@googlegroups.com` and `cleartk-developers@googlegroups.com` announcing the release. The subject should look like:

> Announcing ClearTK Release `<version>`

The body of the message should begin like:

> The ClearTK development team is pleased to announce the release of ClearTK version `<version>`. Please update your maven build file as described here:
>
> http://cleartk.github.io/cleartk/docs/
>
> Any feedback on the release is greatly appreciated. Please reply to cleartk-users@googlegroups with comments and questions.
>
> List of changes in this version:
>
> https://github.com/ClearTK/cleartk/milestones/VERSION
>
> Major changes:

Replace `VERSION` in the "List of changes" URL with your milestone (e.g. 2.1) and test your URL against the GitHub site.
For "Major changes" you should list any significant changes, and make a special note of any backwards incompatible changes.

Once you've sent this email out, the full ClearTK release is complete. Congratulations!

### How do I revert a failed release?

If you make a mistake and/or the release fails for some reason, you can partially rollback the release:

    mvn release:rollback

However, you'll still have to manually remove any tags created in the Git repository, and if anything was uploaded to the Sonatype OSS staging repository, then you'll need to [go there and "Drop" it](http://central.sonatype.org/pages/releasing-the-deployment.html).

### Are there modules that I should not release?

Yes. Never release `cleartk-examples`. It is intended only to be viewed as source code.
Its `pom.xml` is configured so that it will not be deployed by the maven-release-plugin.

## Troubleshooting

### Why can't Eclipse find type system descriptor files when I run JUnit tests in Eclipse?

One common source of problems with running ClearTK tests is that a required type system descriptor file may not be on the classpath.
Typically the stack trace looks something like this:

    org.apache.uima.resource.ResourceInitializationException: 
    An import could not be resolved.  No .xml file with name 
    "org.cleartk.examples.TypeSystem" was found in the class 
    path or data path. (Descriptor: <unknown>)

In this case the type system for the cleartk-examples project was not found.
This can be determined by the package name of the type system missing (here is is "examples").
The way to get the cleartk-examples type system on the classpath is to perform a clean build of that project.
The next time you run the JUnit tests it should find the type system.