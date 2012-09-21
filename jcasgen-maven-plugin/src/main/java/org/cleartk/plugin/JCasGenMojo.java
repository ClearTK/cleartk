package org.cleartk.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.uima.tools.jcasgen.Jg;

/**
 * Applies JCasGen to create Java files from XML type system descriptions.
 * 
 * @goal generate
 * @phase generate-sources
 */
public class JCasGenMojo extends AbstractMojo {

  /**
   * The path to the XML type system description.
   * 
   * @parameter
   * @required
   * @readonly
   */
  private String typeSystem;

  /**
   * The directory where the generated sources will be written.
   * 
   * @parameter default-value="${project.build.directory}/generated-sources/jcasgen"
   * @required
   * @readonly
   */
  private File outputDirectory;

  /**
   * The Maven Project.
   * 
   * @parameter
   * @required
   * @readonly
   */
  private MavenProject project;

  public void execute() throws MojoExecutionException, MojoFailureException {
    Jg jCasGen = new Jg();
    String[] args = new String[] {
        "-jcasgeninput",
        this.typeSystem,
        "-jcasgenoutput",
        this.outputDirectory.getPath() };
    jCasGen.main1(args);
    this.project.addCompileSourceRoot(this.outputDirectory.getPath());
  }
}
