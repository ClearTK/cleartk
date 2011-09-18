package org.cleartk.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Check that all dependency versions in a multi-module project are consistent.
 * 
 * @goal check-version-consistency
 * @phase validate
 * @aggregator true
 */
public class ConsistentVersionsMojo extends AbstractMojo {

  /**
   * The Maven Project.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject mavenProject;

  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException, MojoFailureException {
    // Create a map from dependency names without versions to dependency names
    // with versions to the projects that declared those versions
    Map<String, Map<String, Set<MavenProject>>> dependencyVersionProjects;
    dependencyVersionProjects = new HashMap<String, Map<String, Set<MavenProject>>>();

    // iterate over the dependencies of all projects within this aggregator
    for (Object projectObject : this.mavenProject.getCollectedProjects()) {
      MavenProject project = (MavenProject) projectObject;
      List<Dependency> dependencies = new ArrayList<Dependency>();
      dependencies.addAll(project.getDependencies());
      DependencyManagement dependencyManagement = project.getDependencyManagement();
      if (dependencyManagement != null) {
        dependencies.addAll(dependencyManagement.getDependencies());
      }
      for (Object dependencyObject : dependencies) {
        Dependency dependency = (Dependency) dependencyObject;

        // get the dependency name, both with and without the version
        String key = dependency.getManagementKey();
        String version = String.format("%s:%s", key, dependency.getVersion());

        // add the dependency -> version -> project entry to the map
        if (!dependencyVersionProjects.containsKey(key)) {
          dependencyVersionProjects.put(key, new HashMap<String, Set<MavenProject>>());
        }
        if (!dependencyVersionProjects.get(key).containsKey(version)) {
          dependencyVersionProjects.get(key).put(version, new HashSet<MavenProject>());
        }
        dependencyVersionProjects.get(key).get(version).add(project);
      }
    }

    // if any dependency has multiple versions, create an error message
    StringBuilder error = new StringBuilder();
    for (String dependency : dependencyVersionProjects.keySet()) {
      Map<String, Set<MavenProject>> versionProjects = dependencyVersionProjects.get(dependency);
      if (versionProjects.size() > 1) {

        // the message lists all the versions of this dependency and the projects containing them
        error.append('\n');
        error.append(String.format("Found multiple versions of %s:\n", dependency));
        for (String version : versionProjects.keySet()) {
          for (MavenProject project : versionProjects.get(version)) {
            error.append(String.format("  %s in %s\n", version, project.getName()));
          }
        }
      }
    }

    // only throw an exception if there were dependencies with multiple versions
    if (error.length() > 0) {
      throw new MojoFailureException(error.toString());
    }
  }
}
