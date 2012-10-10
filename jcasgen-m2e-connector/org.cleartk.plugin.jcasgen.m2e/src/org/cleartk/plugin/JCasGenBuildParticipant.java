package org.cleartk.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.Import_impl;
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

public class JCasGenBuildParticipant extends MojoExecutionBuildParticipant {

  public JCasGenBuildParticipant(MojoExecution execution) {
    super(execution, true);
  }

  @Override
  public Set<IProject> build(int kind, IProgressMonitor monitor) throws Exception {
    IMaven maven = MavenPlugin.getMaven();
    BuildContext buildContext = getBuildContext();

    // get the Mojo parameters
    String typeSystem = maven.getMojoParameterValue(
        getSession(),
        getMojoExecution(),
        "typeSystem",
        String.class);
    File generatedSourcesDirectory = maven.getMojoParameterValue(
        getSession(),
        getMojoExecution(),
        "outputDirectory",
        File.class);

    // only run the Mojo if the type system was updated (or the sources were not yet generated)
    if (generatedSourcesDirectory != null && generatedSourcesDirectory.exists()) {
      boolean filesHaveChanged = false;
      for (MavenProject project : this.getSession().getProjects()) {
        for (File typeSystemFile : getTypeSystemFiles(project, typeSystem)) {

          // scan for any changes since the last build
          Scanner scanner = buildContext.newScanner(typeSystemFile);
          scanner.scan();
          String[] includedFiles = scanner.getIncludedFiles();
          if (includedFiles != null && includedFiles.length > 0) {
            filesHaveChanged = true;
            break;
          }
        }
      }
      if (!filesHaveChanged) {
        return null;
      }
    }

    // execute mojo
    Set<IProject> result = super.build(kind, monitor);

    // tell m2e builder to refresh generated files
    if (generatedSourcesDirectory != null) {
      buildContext.refresh(generatedSourcesDirectory);
    }

    return result;
  }

  private static Set<File> getTypeSystemFiles(MavenProject project, String typeSystemPath)
      throws Exception {
    File buildOutputDirectory = new File(project.getBuild().getOutputDirectory());

    // map each resource from its target location to its source location
    Map<File, File> targetToSource = new HashMap<File, File>();
    for (Resource resource : project.getResources()) {
      File resourceDir = new File(resource.getDirectory());
      if (resourceDir.exists()) {

        // scan for the resource files
        List<String> includes = resource.getIncludes();
        if (includes.isEmpty()) {
          includes = Arrays.asList("**");
        }
        List<String> excludes = resource.getExcludes();
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(resourceDir);
        scanner.setIncludes(includes.toArray(new String[includes.size()]));
        scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
        scanner.scan();

        // map each of the resources from its target location to its source location
        String targetPath = resource.getTargetPath();
        for (String filePath : scanner.getIncludedFiles()) {
          File sourceFile = new File(resourceDir, filePath);
          File baseDirectory = targetPath != null
              ? new File(buildOutputDirectory, targetPath)
              : buildOutputDirectory;
          File targetFile = new File(baseDirectory, filePath);
          targetToSource.put(targetFile, sourceFile);
        }
      }
    }

    // add prefix to type system path if necessary
    boolean isFile = false;
    File typeSystemFile = null;
    try {
      URL url = new URL(typeSystemPath);
      url.toURI();
    } catch (MalformedURLException e) {
      isFile = true;
    } catch (URISyntaxException e) {
      isFile = true;
    }
    if (isFile) {
      typeSystemFile = new File(project.getBasedir(), typeSystemPath);
      typeSystemPath = typeSystemFile.getAbsolutePath();
    }

    // assemble the classpath
    StringBuilder classpath = new StringBuilder();
    for (String element : project.getCompileClasspathElements()) {
      if (classpath.length() > 0) {
        classpath.append(File.pathSeparatorChar);
      }
      classpath.append(element);
    }

    // load the type system and resolve imports
    TypeSystemDescription typeSystemDescription = new TypeSystemDescription_impl();
    Import imp = new Import_impl();
    imp.setLocation(typeSystemPath);
    typeSystemDescription.setImports(new Import[] { imp });
    ResourceManager resourceManager = UIMAFramework.newDefaultResourceManager();
    resourceManager.setDataPath(classpath.toString());
    typeSystemDescription.resolveImports(resourceManager);

    // collect the source files for all type system descriptions in the project
    Set<File> typeSystemFiles = new HashSet<File>();
    if (isFile) {
      typeSystemFiles.add(typeSystemFile);
    }
    for (TypeDescription type : typeSystemDescription.getTypes()) {
      URL typeSystemURL = type.getSourceUrl();
      if (typeSystemURL != null) {
        try {
          File sourceFile = targetToSource.get(new File(typeSystemURL.toURI()));
          if (sourceFile != null) {
            typeSystemFiles.add(sourceFile);
          }
        } catch (IllegalArgumentException e) {
          // the URL is not a file, so do nothing
        }
      }
    }
    return typeSystemFiles;
  }
}
