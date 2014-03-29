/** 
 * Copyright (c) 2013, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

package org.cleartk.test.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 */

public class CamelCaseTest {

  @Test
  public void testCamelCase() throws Exception {
    Pattern nonCamelCasePattern = Pattern.compile("[A-Z][A-Z]");
    Matcher nonCamelCaseMatcher;
    List<String> excludedNames = Arrays.asList(
        "ScoredTOP.java", // matches UIMA convention
        "ScoredTOP_Type.java", // matches UIMA convention
        "TempEval2010TaskAAttributes.java",
        "TempEval2010TaskAExtents.java",
        "TempEval2010TaskBAttributes.java",
        "TempEval2010TaskBExtents.java",
        "XReader.java",
        "XReaderTest.java",
        "JCasGenM2ETest.java",
        "JCasGenMojo.java",
        "JCasGenMojoTest.java"
        );

    Iterator<?> files = org.apache.commons.io.FileUtils.iterateFiles(
        new File(".."),
        new SuffixFileFilter(".java"),
        TrueFileFilter.INSTANCE);

    List<String> poorlyNamedFiles = new ArrayList<String>();

    while (files.hasNext()) {
      File file = (File) files.next();
      String fileName = file.getName();
      if (excludedNames.contains(fileName)) {
        continue;
      }
      nonCamelCaseMatcher = nonCamelCasePattern.matcher(fileName);
      if (nonCamelCaseMatcher.find()) {
        poorlyNamedFiles.add(file.getPath());
      }
    }

    if (poorlyNamedFiles.size() > 0) {
      String message = String.format(
          "%d source files with names that do not conform to camel case naming convention.. ",
          poorlyNamedFiles.size());
      System.err.println(message);
      Collections.sort(poorlyNamedFiles);
      for (String path : poorlyNamedFiles) {
        System.err.println(path);
      }
      Assert.fail(message);
    }

  }
}