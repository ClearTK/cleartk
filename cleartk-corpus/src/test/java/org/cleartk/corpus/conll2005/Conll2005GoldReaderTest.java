/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.corpus.conll2005;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.test.util.CleartkTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.apache.uima.fit.factory.CollectionReaderFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class Conll2005GoldReaderTest extends CleartkTestBase {

  // pointer to random data file. not an actual conll2005 file.
  private final String oneSentencePath = "src/test/resources/data/propbank-1.0/README";

  @Test
  public void testDescriptor() throws UIMAException {
    try {
      CollectionReaderFactory.createReader(Conll2005GoldReader.class);
      Assert.fail("expected error for missing CoNLL 2005 data file");
    } catch (ResourceInitializationException e) {
    }

    CollectionReader reader = CollectionReaderFactory.createReader(
        Conll2005GoldReader.class,
        Conll2005GoldReader.PARAM_CONLL2005_DATA_FILE,
        this.oneSentencePath);
    Object dataFileName = reader.getConfigParameterValue(Conll2005GoldReader.PARAM_CONLL2005_DATA_FILE);
    Assert.assertEquals(this.oneSentencePath, dataFileName);
  }
}
