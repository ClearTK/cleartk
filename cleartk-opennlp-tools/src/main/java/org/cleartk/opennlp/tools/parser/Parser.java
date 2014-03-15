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

package org.cleartk.opennlp.tools.parser;

import org.cleartk.opennlp.tools.ParserAnnotator;

import com.google.common.annotations.Beta;

import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSTagger;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 * 
 *         This class provides a simple extension to {@link opennlp.tools.parser.chunking.Parser}
 *         which has an additional constructor which takes a POSTagger which replaces the POSTagger
 *         that will be loaded from the {@link ParserModel}. This is used by {@link ParserAnnotator}
 *         in conjunction with the {@link CasPosTagger} so that the parser can make use of
 *         part-of-speech tags in the CAS.
 * 
 */
@Beta
public class Parser extends opennlp.tools.parser.chunking.Parser {

  public Parser(ParserModel model, int beamSize, double advancePercentage) {
    super(model, beamSize, advancePercentage);
  }

  public Parser(ParserModel model, int beamSize, double advancePercentage, POSTagger tagger) {
    super(model, beamSize, advancePercentage);
    this.tagger = tagger;
  }

}
