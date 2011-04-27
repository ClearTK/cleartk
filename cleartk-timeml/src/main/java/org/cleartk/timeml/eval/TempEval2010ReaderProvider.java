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
package org.cleartk.timeml.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.eval.provider.NameBasedReaderProvider;
import org.cleartk.timeml.corpus.TempEval2010CollectionReader;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TempEval2010ReaderProvider extends NameBasedReaderProvider {

  public TempEval2010ReaderProvider(String trainDir, String testDir)
      throws ResourceInitializationException, IOException {
    this(new File(trainDir), new File(testDir));
  }

  public TempEval2010ReaderProvider(File trainDir, File testDir)
      throws ResourceInitializationException, IOException {
    super(
        TempEval2010CollectionReader.getAnnotatedFileNames(trainDir),
        TempEval2010CollectionReader.getAnnotatedFileNames(testDir),
        TempEval2010CollectionReader.getCollectionReader(
            Arrays.asList(trainDir, testDir),
            Collections.<String> emptySet()));
  }

  @Override
  protected void configureReader(List<String> names) throws ResourceConfigurationException {
    this.reader.setConfigParameterValue(
        TempEval2010CollectionReader.PARAM_SELECTED_FILE_NAMES,
        names.toArray(new String[names.size()]));
    this.reader.reconfigure();
  }
}