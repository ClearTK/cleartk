package org.cleartk.plugin;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
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
      for (MavenProject project : this.getSession().getProjects()) {
        File typeSystemFile = new File(project.getBasedir(), typeSystem);
        if (typeSystemFile.exists()) {

          // scanner only sees files with content changes since the last build
          Scanner scanner = buildContext.newScanner(typeSystemFile);
          scanner.scan();

          // if no files have changed, don't run the Mojo
          String[] includedFiles = scanner.getIncludedFiles();
          if (includedFiles == null || includedFiles.length <= 0) {
            return null;
          }
        }
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
}
