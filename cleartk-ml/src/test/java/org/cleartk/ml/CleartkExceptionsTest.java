/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.ml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.features.FeatureEncoder;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Sanity checks that we're matching the arguments up right and people aren't getting malformed
 * exception messages.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CleartkExceptionsTest {

  @Test
  public void testCleartkProcessingException() {
    CleartkProcessingException e;

    List<? extends Exception> exceptions = Arrays.asList(
        new IllegalArgumentException("hello!"),
        new UnsupportedOperationException());
    e = CleartkProcessingException.multipleExceptions(exceptions);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("hello!"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("1 other exception"));

    List<?> features = Arrays.asList("XXX", "0101");
    e = CleartkProcessingException.noInstanceOutcome(features);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("outcome"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("XXX"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("0101"));

    e = CleartkProcessingException.unsupportedOperationSetParameter(
        new NullPointerException(),
        "OBJECT",
        "METHOD",
        "PARAM",
        "VALUE");
    Assert.assertTrue(e.getMessage(), e.getMessage().contains(String.class.getName()));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("support"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("METHOD"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("PARAM"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("VALUE"));
  }

  @Test
  public void testCleartkExtractorException() {
    CleartkExtractorException e;

    e = CleartkExtractorException.invalidTypePath("foo.bar.baz", null);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("valid"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("foo.bar.baz"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("null"));

    e = CleartkExtractorException.moreThanOneName("abc", "def");
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("name"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("abc"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("def"));

    e = CleartkExtractorException.noAnnotationInWindow(Annotation.class, null);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("window"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains(Annotation.class.getName()));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("null"));

    e = CleartkExtractorException.noAnnotationMatchingWindow(Annotation.class, null);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("window"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains(Annotation.class.getName()));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("null"));

    Feature feature = new Feature() {
      @Override
      public int compareTo(Feature o) {
        return 0;
      }

      @Override
      public boolean isMultipleReferencesAllowed() {
        return false;
      }

      @Override
      public String getShortName() {
        return null;
      }

      @Override
      public Type getRange() {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public Type getDomain() {
        return null;
      }
    };

    e = CleartkExtractorException.notPrimitive(feature);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("primitive"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("domain=null"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("range=null"));

    e = CleartkExtractorException.notPrimitiveArray(feature);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("primitive array"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("domain=null"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("range=null"));

    e = CleartkExtractorException.wrongAnnotationType(Annotation.class, null);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("type"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains(Annotation.class.getName()));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("null"));
  }

  @Test
  public void testCleartkEncoderException() {

    CleartkEncoderException e = CleartkEncoderException.invalidFeatureVectorValue(
        new NullPointerException(),
        42,
        -1.0);
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("valid"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("42:-1"));

    e = CleartkEncoderException.noMatchingEncoder(
        new org.cleartk.ml.Feature("Spam!"),
        Collections.<FeatureEncoder<?>> emptyList());
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("encoder"));
    Assert.assertTrue(e.getMessage(), e.getMessage().contains("Spam!"));
  }
}
