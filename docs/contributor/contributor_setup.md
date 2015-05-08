---
title: Typical Development Environment
menu: Contributors
menu-order: 1
---

# Typical Development Environment for Contributors
{: .no_toc}

* Automatically replaced with table of contents
{:toc}

## Building with Eclipse

It is not necessary to build ClearTK with the following tools as you can do everything from the command line with git and maven clients.
However, several core developers use Eclipse along with two plugins, subclipse and m2eclipse.
Specifically:

* [Eclipse](http://eclipse.org/) 3.7 or higher.  You can download "Eclipse Classic"
* [m2e](http://www.eclipse.org/m2e/) 1.0.0 or higher.
  * update site: http://download.eclipse.org/technology/m2e/releases
  * Select the following module: m2e
* [EGit](http://www.eclipse.org/egit/) 2.0.0 or higher.
  * update site: http://download.eclipse.org/egit/updates
  * Select the following module: Eclipse EGit
* m2e connectors for the Maven plugins used by ClearTK.
  * Go to **Window** -> **Preferences** -> **Maven** -> **Discovery** -> **Open Catalog**
  * Select the following "Lifecycle Mappings": buildhelper

### Checking out and Compiling ClearTK in Eclipse

To check out ClearTK:

1.  Go to **File** -> **Import...** -> **Git** -> **Projects from Git**.
2.  Select **URI** and then click **Next**.
3.  Paste **`https://code.google.com/p/cleartk/`** as the URI and then click **Next**.
4.  The **master** branch will be selected. Click **Next**.
5.  The default local storage directory should be fine. Click **Next**.
6.  Wait several minutes while the project is cloned.
7.  Make sure "Import existing projects" is selected and click **Next**.
8.  Click **Finish**.

You should now see the root `cleartk` project in your Eclipse workspace and all of its sub-projects.
All of the sub-projects should successfully compile.
Please close the `cleartk` project by right-clicking on it and selecting "Close Project".


### Running tests with Eclipse

All ClearTK module have tests defined in their `src/main/test` directory. To run them:

* Right click on the project, then go to **Run as** -> **JUnit Test**

It is common for some tests to fail if you have limited memory or have not [installed all the classifiers]({{ site.baseurl }}/docs/user_faq.html#setting-up-classifiers) in your environment.
To ignore known sets of problematic tests do the following:

* Go to **Run** -> **Run Configurations...**
* Select the JUnit run configuration that was created for your project
* On the **Arguments** tab, add **VM arguments** such as: `-Dcleartk.skipTests=long,bigMem`.
  See the contributor FAQ for [more details on flags for skipping tests](contributor_faq.html#how-do-i-skip-failing-tests-that-i-want-to-skip).

### Troubleshooting Eclipse Failures to Build

Sometimes you may find that the projects fail to build for no apparent reason.  Here are some troubleshooting tips that you can try:

* Go to **Project** -> **Clean**, select **Clean all projects** and hit **Ok**
* In the "Package Explorer" view, right click on the cleartk project and select **Run As** -> **Maven Generate Sources**
* Right click, then go to **Refresh** to sync up with the local filesystem
* Right click, then go to **Maven2** -> **Update Project Configuration**
* Right click, then go to **Maven2** -> **Update Dependencies**
* Build the project from the command line using Maven.

Sometimes Eclipse will complain that the persisted container within a project references a missing libraries.
This is an issue with workspace resolution caused by mismatches between m2(eclipse) and Eclipse.
To fix it try:

* Right click,  **Close Project** and then right click, **Open Project**.

(This is assuming that you use the _workspace resolution_ feature of the m2e plugin, which automatically handles inter-project dependencies between the ClearTK sub-projects.
If you are **not** using this feature, or if you don't have all of the ClearTK sub-projects in your Eclipse workspace, you'll have to build manually with maven.)

Importing the `consistent-versions-plugin` project is not necessary for developing ClearTK.
If you already have imported it and it is causing compile errors, then you can simply close the project as a way to ignore these errors.


## Building on the Command Line

To build ClearTK on the command line, you'll need to have [Git](http://git-scm.com/) and [Maven](http://maven.apache.org/download.html) installed.
For those old schoolers out there, roughly speaking, Git is a replacement for the CVS version control system, and Maven is a replacement for the Ant build system.
There is a wide variety of documentation for Git and Maven online, so here we just summarize the steps you need to get going with ClearTK.


### Checking out ClearTK with Git

The ClearTK code is kept in a [Git](http://git-scm.com/) repository. To check out the code run:

    git clone https://code.google.com/p/cleartk/

This will create a `cleartk` directory in your current directory, and fill it with the contents of the ClearTK code base.


### Compiling ClearTK with Maven

When you check out ClearTK from the Git repository you will almost always end up with a _snapshot version_ (e.g. `0.5.0-SNAPSHOT`).
You can tell if you have a snapshot version by looking for `<version>...-SNAPSHOT</version>` in the `pom.xml` in the `cleartk` root directory.

Snapshot versions are not deployed to the ClearTK repository, so if you try to compile from a single sub-module (e.g. `cleartk-ml`), maven will not be able to find the dependencies (e.g. `org.cleartk:cleartk-util:0.7.0-SNAPSHOT`).
However, the top level `cleartk` module knows about all the snapshot versions of the sub-modules `cleartk-ml`, `cleartk-util`, etc.
Therefore, you can (should) always compile snapshot versions from the top level `cleartk` module using:

    mvn compile

### Running tests with Maven

From the top level `cleartk` module, run the standard:

    mvn test

It is common for some tests to fail if you have limited memory or have not [installed all the classifiers]({{ site.baseurl }}/docs/user_faq.html#setting-up-classifiers) in your environment.
See the contributor FAQ for [details on how to skip problematic tests](contributor_faq.html#how-do-i-skip-failing-tests-that-i-want-to-skip).
