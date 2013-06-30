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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.CollectionReaderFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
@SofaCapability(outputSofas = { Conll2005Constants.CONLL_2005_VIEW, ViewURIUtil.URI })
public class Conll2005GoldReader extends JCasCollectionReader_ImplBase {

  public static CollectionReader getCollectionReader(String conll2005DataFile)
      throws ResourceInitializationException {
    return CollectionReaderFactory.createCollectionReader(
        Conll2005GoldReader.class,
        PARAM_CONLL2005_DATA_FILE,
        conll2005DataFile);
  }

  @ConfigurationParameter(name = PARAM_CONLL2005_DATA_FILE, mandatory = true, description = "the path of the CoNLL 2005 data file")
  private File conll2005DataFile;

  public static final String PARAM_CONLL2005_DATA_FILE = "conll2005DataFile";

  private BufferedReader reader;

  private boolean finished = false;

  private int documentNumber;

  private int totalDocuments;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    try {
      this.reader = this.getBufferedReader();
      String line;
      this.totalDocuments = 0;
      do {
        line = this.reader.readLine();
        while (line != null && line.trim().length() == 0) {
          line = this.reader.readLine();
        }
        if (line == null) {
          break;
        }
        this.totalDocuments += 1;
        while (line != null && line.trim().length() > 0) {
          line = this.reader.readLine();
        }
      } while (line != null);
      this.reader.close();

      this.reader = this.getBufferedReader();
      documentNumber = 0;

    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  private BufferedReader getBufferedReader() throws IOException {
    InputStream in;
    if (this.conll2005DataFile.getName().endsWith(".gz")) {
      in = new GZIPInputStream(new FileInputStream(this.conll2005DataFile));
    } else {
      in = new FileInputStream(this.conll2005DataFile);
    }
    return new BufferedReader(new InputStreamReader(in));
  }

  public void getNext(JCas jCas) throws IOException, CollectionException {
    try {
      JCas conllView = jCas.createView(Conll2005Constants.CONLL_2005_VIEW);

      String lineBuffer;
      StringBuffer docBuffer = new StringBuffer();

      lineBuffer = reader.readLine();
      while (lineBuffer != null && lineBuffer.trim().length() == 0) {
        lineBuffer = reader.readLine();
      }

      if (lineBuffer == null) {
        throw new CollectionException("unexpected end of input", null);
      }

      while (lineBuffer != null && lineBuffer.trim().length() != 0) {
        docBuffer.append(lineBuffer.trim());
        docBuffer.append("\n");
        lineBuffer = reader.readLine();
      }

      documentNumber += 1;

      if (documentNumber == totalDocuments) {
        finished = true;
      }

      conllView.setSofaDataString(docBuffer.toString(), "text/plain");
      URI fileURI = this.conll2005DataFile.toURI();
      String fragment = String.valueOf(this.documentNumber);
      URI uri;
      try {
        uri = new URI(fileURI.getScheme(), fileURI.getHost(), fileURI.getPath(), fragment);
      } catch (URISyntaxException e) {
        // should never reach this; fragment should always be valid since it's just a number
        throw new RuntimeException(e);
      }
      ViewURIUtil.setURI(jCas, uri);
    } catch (CASException e) {
      throw new CollectionException(e);
    }
  }

  public void close() throws IOException {
    reader.close();
  }

  public Progress[] getProgress() {
    return new Progress[] { new ProgressImpl(documentNumber, totalDocuments, Progress.ENTITIES) };
  }

  public boolean hasNext() throws IOException, CollectionException {
    return !finished;
  }
}
