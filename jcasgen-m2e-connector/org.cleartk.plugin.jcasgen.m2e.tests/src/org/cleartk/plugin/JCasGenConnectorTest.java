package org.cleartk.plugin;

import java.util.HashSet;
import java.util.Set;

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
}
