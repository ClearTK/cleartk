package org.cleartk.plugin;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;

public class JCasGenProjectConfigurator extends AbstractJavaProjectConfigurator {
  @Override
  public AbstractBuildParticipant getBuildParticipant(
      IMavenProjectFacade projectFacade,
      MojoExecution execution,
      IPluginExecutionMetadata executionMetadata) {
    return new JCasGenBuildParticipant(execution);
  }
}
