/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */
public class ParametersTestUtil {

  public static void testParameterDefinitions(String outputDirectory, String... excludeFiles)
      throws ClassNotFoundException {
    IOFileFilter includeFilter = new SuffixFileFilter(".java");

    if (excludeFiles != null) {
      IOFileFilter excludeFilter = FileFilterUtils.notFileFilter(new SuffixFileFilter(excludeFiles));
      includeFilter = FileFilterUtils.and(excludeFilter, includeFilter);
    }

    Iterator<File> files = org.apache.commons.io.FileUtils.iterateFiles(
        new File(outputDirectory),
        includeFilter,
        TrueFileFilter.INSTANCE);
    testParameterDefinitions(files);
  }

  public static void testParameterDefinitions(Iterator<File> files) throws ClassNotFoundException {
    List<String> badParameters = new ArrayList<String>();
    List<String> missingParameterNameFields = new ArrayList<String>();

    while (files.hasNext()) {
      File file = files.next();
      String className = file.getPath();
      className = className.substring(14);
      className = className.substring(0, className.length() - 5);
      className = className.replace(File.separatorChar, '.');
      Class<?> cls = Class.forName(className);
      Field[] fields = cls.getDeclaredFields();
      for (Field field : fields) {
        if (ConfigurationParameterFactory.isConfigurationParameterField(field)) {
          org.apache.uima.fit.descriptor.ConfigurationParameter annotation = field.getAnnotation(org.apache.uima.fit.descriptor.ConfigurationParameter.class);
          String parameterName = annotation.name();
          String expectedName = field.getName();
          if (!expectedName.equals(parameterName)) {
            badParameters.add("'" + parameterName + "' should be '" + expectedName + "'");
          }

          expectedName = className+"."+field.getName(); 
          String fieldName = getParameterNameField(expectedName);
          try {
            Field fld = cls.getDeclaredField(fieldName);
            if ((fld.getModifiers() & Modifier.PUBLIC) == 0
                || (fld.getModifiers() & Modifier.FINAL) == 0
                || (fld.getModifiers() & Modifier.PUBLIC) == 0) {
              missingParameterNameFields.add(expectedName);
            } else if (!fld.get(null).equals(expectedName.substring(expectedName.lastIndexOf(".")+1))) {
              missingParameterNameFields.add(expectedName);
            }
          } catch (Exception e) {
            missingParameterNameFields.add(expectedName);
          }
        }
      }
    }

    if (badParameters.size() > 0 || missingParameterNameFields.size() > 0) {
      String message = String.format(
          "%d descriptor parameters with bad names and %d descriptor parameters with no name field. ",
          badParameters.size(),
          missingParameterNameFields.size());
      System.err.println(message);
      System.err.println("descriptor parameters with bad names: ");
      for (String badParameter : badParameters) {
        System.err.println(badParameter);
      }
      System.err.println("each configuration parameter should have a public static final String that specifies its name.  The missing fields are: ");
      for (String missingParameterNameField : missingParameterNameFields) {
        System.err.println(missingParameterNameField + " should be named by "
            + missingParameterNameField.substring(0, missingParameterNameField.lastIndexOf('.'))
            + "." + getParameterNameField(missingParameterNameField));
      }
      Assert.fail(message);
    }
  }

  private static String getParameterNameField(String parameterName) {
    String parameterNameField = "PARAM";

    String fieldName = parameterName.substring(parameterName.lastIndexOf('.') + 1);
    String[] fieldNameParts = fieldName.split("(?=[A-Z]++)");
    for (String fieldNamePart : fieldNameParts) {
      parameterNameField += "_" + fieldNamePart.toUpperCase();
    }
    return parameterNameField;
  }

}
