package org.cleartk.plugin;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;

public class JCasGenMojoTest extends AbstractMojoTestCase {

  public void testSimple() throws Exception {
    this.test("simple");
  }

  public void testClasspath() throws Exception {
    this.test("classpath");
  }

  public void test(String projectName) throws Exception {

    File projectDirectory = getTestFile("src/test/resources/" + projectName);
    File buildDirectory = getTestFile("target/project-" + projectName + "-test");
    File pomFile = new File(projectDirectory, "/pom.xml");
    assertNotNull(pomFile);
    assertTrue(pomFile.exists());

    // create the MavenProject from the pom.xml file
    MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
    ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest();
    ProjectBuilder projectBuilder = this.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(pomFile, buildingRequest).getProject();
    assertNotNull(project);

    // set the base directory (or it will write to src/test/resources/)
    Build build = project.getModel().getBuild();
    build.setDirectory(buildDirectory.getPath());
    File outputDirectory = new File(buildDirectory, "target/classes");
    build.setOutputDirectory(outputDirectory.getPath());

    // copy resources
    File source = new File(projectDirectory, "src/main/resources");
    if (source.exists()) {
      FileUtils.copyDirectory(source, outputDirectory);
    }

    // load the Mojo
    JCasGenMojo generate = (JCasGenMojo) this.lookupConfiguredMojo(project, "generate");
    assertNotNull(generate);

    // set the MavenProject on the Mojo (AbstractMojoTestCase does not do this by default)
    setVariableValueToObject(generate, "project", project);

    // execute the Mojo
    generate.execute();

    // check that the Java files have been generated
    File jCasGenDirectory = new File(buildDirectory, "generated-sources/jcasgen");
    Assert.assertTrue(new File(jCasGenDirectory + "/type/span/Sentence.java").exists());
    Assert.assertTrue(new File(jCasGenDirectory + "/type/span/Sentence_Type.java").exists());
    Assert.assertTrue(new File(jCasGenDirectory + "/type/span/Token.java").exists());
    Assert.assertTrue(new File(jCasGenDirectory + "/type/span/Token_Type.java").exists());
    Assert.assertTrue(new File(jCasGenDirectory + "/type/relation/Dependency.java").exists());
    Assert.assertTrue(new File(jCasGenDirectory + "/type/relation/Dependency_Type.java").exists());

    // check that the generated sources are on the compile path
    Assert.assertTrue(project.getCompileSourceRoots().contains(jCasGenDirectory.getAbsolutePath()));
  }
}
