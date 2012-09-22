package org.cleartk.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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
   * @parameter property="project"
   * @required
   * @readonly
   */
  private MavenProject project;

  public void execute() throws MojoExecutionException, MojoFailureException {
    String typeSystemPath = this.typeSystem;
    boolean isFile = false;
    try {
      URL url = new URL(this.typeSystem);
      url.toURI();
    } catch (MalformedURLException e) {
      isFile = true;
    } catch (URISyntaxException e) {
      isFile = true;
    }
    if (isFile) {
      typeSystemPath = new File(this.project.getBasedir(), this.typeSystem).getAbsolutePath();
    }
    Jg jCasGen = new Jg();
    String[] args = new String[] {
        "-jcasgeninput",
        typeSystemPath,
        "-jcasgenoutput",
        this.outputDirectory.getAbsolutePath() };
    int result = jCasGen.main1(args);
    if (result != 0) {
      throw new MojoFailureException(
          "JCasGen failed, see the \"JCasGen\" lines in the logged output for details");
    }
    this.project.addCompileSourceRoot(this.outputDirectory.getPath());
  }
}
