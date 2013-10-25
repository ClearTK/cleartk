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
package org.cleartk.examples.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewUriUtil;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class ExamplePosPlainTextWriter extends JCasAnnotator_ImplBase {

  public static final String DEFAULT_OUTPUT_DIRECTORY = "target/examples/pos";

  public static final String PARAM_OUTPUT_DIRECTORY_NAME = "outputDirectoryName";

  @ConfigurationParameter(
      name = PARAM_OUTPUT_DIRECTORY_NAME,
      mandatory = true,
      defaultValue = DEFAULT_OUTPUT_DIRECTORY,
      description = "provides the directory where the token/pos text files will be written")
  private String outputDirectoryName;

  protected File outputDir;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    this.outputDir = new File(outputDirectoryName);
    if (!this.outputDir.exists()) {
      this.outputDir.mkdirs();
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String id = new File(ViewUriUtil.getURI(jCas)).getName();
    PrintWriter outputWriter;
    try {
      outputWriter = new PrintWriter(new File(this.outputDir, id + ".pos"));
    } catch (FileNotFoundException e) {
      throw new AnalysisEngineProcessException(e);
    }
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      for (Token token : JCasUtil.selectCovered(jCas, Token.class, sentence)) {
        outputWriter.print(token.getCoveredText());
        outputWriter.print('/');
        outputWriter.print(token.getPos());
        outputWriter.print(' ');
      }
      outputWriter.println();
    }
    outputWriter.close();
  }

  public void setOutputDirectoryName(String outputDirectoryName) {
    this.outputDirectoryName = outputDirectoryName;
  }

}
