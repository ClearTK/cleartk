/*
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
package org.cleartk.eval.provider;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.resource.ResourceConfigurationException;
import org.cleartk.eval.Evaluation_ImplBase;
import org.junit.Test;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. <br>
 * 
 * @author Steven Bethard
 * @deprecated Tested classes have been replaced by {@link Evaluation_ImplBase}
 */
@Deprecated
public class NameBasedReaderProviderTest {

  public static class Provider extends NameBasedReaderProvider {

    public List<String> currentNames;

    public Provider(List<String> trainNames, List<String> testNames) {
      super(trainNames, testNames, null);
    }

    @Override
    protected void configureReader(List<String> names) throws ResourceConfigurationException {
      this.currentNames = names;
    }

  }

  @Test
  public void test() throws Exception {
    Provider provider = new Provider(Arrays.asList("1", "2", "3"), Arrays.asList("4", "5", "6"));

    // train and test files
    provider.getTrainReader();
    Assert.assertEquals(Arrays.asList("1", "2", "3"), provider.currentNames);
    provider.getTestReader();
    Assert.assertEquals(Arrays.asList("4", "5", "6"), provider.currentNames);

    // cross validation files
    provider.setNumberOfFolds(3);
    provider.getTestReader(0);
    Assert.assertEquals(Arrays.asList("1"), provider.currentNames);
    provider.getTrainReader(0);
    Assert.assertEquals(Arrays.asList("2", "3"), provider.currentNames);
    provider.getTestReader(1);
    Assert.assertEquals(Arrays.asList("2"), provider.currentNames);
    provider.getTrainReader(1);
    Assert.assertEquals(Arrays.asList("1", "3"), provider.currentNames);
    provider.getTestReader(2);
    Assert.assertEquals(Arrays.asList("3"), provider.currentNames);
    provider.getTrainReader(2);
    Assert.assertEquals(Arrays.asList("1", "2"), provider.currentNames);

    // all files
    provider.getReader();
    Assert.assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6"), provider.currentNames);
  }

}
