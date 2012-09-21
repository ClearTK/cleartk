package org.cleartk.plugin;

import java.io.File;

import junit.framework.Assert;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;

public class JCasGenMojoTest extends AbstractMojoTestCase {

  /**
   * @throws Exception
   *           if any
   */
  public void testSomething() throws Exception {

    File baseDirectory = getTestFile("target/test/simple");
    File pomFile = getTestFile("src/test/resources/simple/pom.xml");
    assertNotNull(pomFile);
    assertTrue(pomFile.exists());

    // create the MavenProject from the pom.xml file
    MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
    ProjectBuildingRequest buildingRequest = executionRequest.getProjectBuildingRequest();
    ProjectBuilder projectBuilder = this.lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(pomFile, buildingRequest).getProject();
    assertNotNull(project);

    // set the base directory (or it will write to src/test/resources/)
    project.getModel().getBuild().setDirectory(baseDirectory.getPath());

    // load the Mojo
    JCasGenMojo generate = (JCasGenMojo) this.lookupConfiguredMojo(project, "generate");
    assertNotNull(generate);

    // set the MavenProject on the Mojo (AbstractMojoTestCase does not do this by default)
    setVariableValueToObject(generate, "project", project);

    // execute the Mojo
    generate.execute();

    // check that the Java files have been generated
    File outputDirectory = new File(baseDirectory, "generated-sources/jcasgen");
    Assert.assertTrue(new File(outputDirectory + "/type/span/Sentence.java").exists());
    Assert.assertTrue(new File(outputDirectory + "/type/span/Sentence_Type.java").exists());
    Assert.assertTrue(new File(outputDirectory + "/type/span/Token.java").exists());
    Assert.assertTrue(new File(outputDirectory + "/type/span/Token_Type.java").exists());
    Assert.assertTrue(new File(outputDirectory + "/type/relation/Dependency.java").exists());
    Assert.assertTrue(new File(outputDirectory + "/type/relation/Dependency_Type.java").exists());

    // check that the generated sources are on the compile path
    Assert.assertTrue(project.getCompileSourceRoots().contains(outputDirectory.getAbsolutePath()));
  }
}
