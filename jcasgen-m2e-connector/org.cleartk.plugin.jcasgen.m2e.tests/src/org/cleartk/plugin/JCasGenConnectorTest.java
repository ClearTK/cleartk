package org.cleartk.plugin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;

@SuppressWarnings("restriction")
public class JCasGenConnectorTest extends AbstractMavenProjectTestCase {
  public void testSimple() throws Exception {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject project = importProject("projects/jcasgen/simple/pom.xml", configuration);
    waitForJobsToComplete();
    assertNoErrors(project);

    project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
    waitForJobsToComplete();
    assertNoErrors(project);

    // make sure the Java sources were generated
    String prefix = "target/generated-sources/jcasgen/type/";
    assertTrue(project.getFile(prefix + "span/Sentence.java").isAccessible());
    assertTrue(project.getFile(prefix + "span/Sentence_Type.java").isAccessible());
    assertTrue(project.getFile(prefix + "span/Token.java").isAccessible());
    assertTrue(project.getFile(prefix + "span/Token_Type.java").isAccessible());
    assertTrue(project.getFile(prefix + "relation/Dependency.java").isAccessible());
    assertTrue(project.getFile(prefix + "relation/Dependency_Type.java").isAccessible());

    // make sure the files are all synchronized in the workspace
    int zero = IResource.DEPTH_ZERO;
    assertTrue(project.getFile(prefix + "span/Sentence.java").isSynchronized(zero));
    assertTrue(project.getFile(prefix + "span/Sentence_Type.java").isSynchronized(zero));
    assertTrue(project.getFile(prefix + "span/Token.java").isSynchronized(zero));
    assertTrue(project.getFile(prefix + "span/Token_Type.java").isSynchronized(zero));
    assertTrue(project.getFile(prefix + "relation/Dependency.java").isSynchronized(zero));
    assertTrue(project.getFile(prefix + "relation/Dependency_Type.java").isSynchronized(zero));

    // make sure the generated sources are on the classpath
    Set<String> classpathEntries = new HashSet<String>();
    for (IClasspathEntry cpEntry : JavaCore.create(project).getRawClasspath()) {
      classpathEntries.add(cpEntry.getPath().toPortableString());
    }
    assertTrue(classpathEntries.contains("/simple/src/main/java"));
    assertTrue(classpathEntries.contains("/simple/target/generated-sources/jcasgen"));
  }

  public void testEmptyOutputDirectory() throws Exception {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject project = importProject("projects/jcasgen/simple/pom.xml", configuration);
    waitForJobsToComplete();
    assertNoErrors(project);

    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
    waitForJobsToComplete();
    assertNoErrors(project);

    // make sure the Java sources were generated
    assertTrue(project.getFolder("target/generated-sources/jcasgen").exists());

    // remove the generated directory
    project.getFolder("target/generated-sources/jcasgen").delete(IProject.FORCE, monitor);
    assertFalse(project.getFolder("target/generated-sources/jcasgen").exists());

    // re-build
    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
    waitForJobsToComplete();
    assertNoErrors(project);

    // make sure the Java sources were generated
    assertTrue(project.getFolder("target/generated-sources/jcasgen").exists());
  }

  public void testTypeSystemReferences() throws Exception {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject project = importProject("projects/jcasgen/classpath/pom.xml", configuration);
    waitForJobsToComplete();
    assertNoErrors(project);

    project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
    waitForJobsToComplete();
    assertNoErrors(project);

    String prefix = "target/generated-sources/jcasgen/type/";
    IFile sentenceFile = project.getFile(prefix + "span/Sentence.java");
    IFile tokenFile = project.getFile(prefix + "span/Token.java");
    IFile dependencyFile = project.getFile(prefix + "relation/Dependency.java");

    // make sure the Java sources were generated
    assertTrue(sentenceFile.isAccessible());
    assertTrue(tokenFile.isAccessible());
    assertTrue(dependencyFile.isAccessible());

    // record the modification times
    long sentenceTime = sentenceFile.getModificationStamp();
    long tokenTime = tokenFile.getModificationStamp();
    long dependencyTime = dependencyFile.getModificationStamp();

    // build incrementally
    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
    waitForJobsToComplete();
    assertNoErrors(project);

    // modify something unrelated to the type system
    IFile pomFile = project.getFile("pom.xml");
    pomFile.touch(monitor);

    // make sure no type system files were changed
    assertEquals(sentenceTime, sentenceFile.getModificationStamp(), 1e-10);
    assertEquals(tokenTime, tokenFile.getModificationStamp(), 1e-10);
    assertEquals(dependencyTime, dependencyFile.getModificationStamp(), 1e-10);

    // now modify the token descriptor which is indirectly referenced from TypeSystem.xml
    IFile tokenSourceFile = project.getFile("src/main/resources/type/Token.xml");
    tokenSourceFile.touch(monitor);

    // re-run the build
    project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
    waitForJobsToComplete();
    assertNoErrors(project);

    // make sure all generated files were re-generated
    assertTrue(sentenceFile.getModificationStamp() > sentenceTime);
    assertTrue(tokenFile.getModificationStamp() > tokenTime);
    assertTrue(dependencyFile.getModificationStamp() > dependencyTime);

  }
}
