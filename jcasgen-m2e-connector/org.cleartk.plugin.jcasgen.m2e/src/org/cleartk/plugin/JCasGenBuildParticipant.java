package org.cleartk.plugin;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
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

    // check if the type system file has changed
    String typeSystem = maven.getMojoParameterValue(
        getSession(),
        getMojoExecution(),
        "typeSystem",
        String.class);
    File typeSystemFile = new File(typeSystem);
    if (typeSystemFile.exists()) {
      // scanner only sees files with content changes since the last build
      Scanner scanner = buildContext.newScanner(typeSystemFile);
      scanner.scan();
      String[] includedFiles = scanner.getIncludedFiles();
      if (includedFiles == null || includedFiles.length <= 0) {
        return null;
      }
    }

    // execute mojo
    Set<IProject> result = super.build(kind, monitor);

    // tell m2e builder to refresh generated files
    File generatedSourcesDirectory = maven.getMojoParameterValue(
        getSession(),
        getMojoExecution(),
        "outputDirectory",
        File.class);
    if (generatedSourcesDirectory != null) {
      buildContext.refresh(generatedSourcesDirectory);
    }

    return result;
  }
}
