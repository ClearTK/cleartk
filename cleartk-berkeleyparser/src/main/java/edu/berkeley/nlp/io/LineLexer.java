/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */

package edu.berkeley.nlp.io;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Majid Laali
 */
public class LineLexer extends PTBLexer {

  public LineLexer() {
    this(new FeatureLabelTokenFactory(), false, true);
  }
  
  public LineLexer(FeatureLabelTokenFactory featureLabelTokenFactory, boolean tokenizeCRs,
      boolean suppressEscaping) {
    super((java.io.Reader) null, featureLabelTokenFactory, tokenizeCRs, suppressEscaping);
  }

  public List<String> tokenizeLine(String line) throws IOException {
    LinkedList<String> tokenized = new LinkedList<String>();
    if (line == null)
      return tokenized;
    zzBuffer = line.toCharArray();// new char[nEl+1];
    // for(int i=0;i<nEl;i++) yy_buffer[i] = array[i];
    // yy_buffer[nEl] = (char)YYEOF;
    zzStartRead = 0;
    zzEndRead = zzBuffer.length;
    zzAtBOL = true;
    zzAtEOF = false;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
    while (zzMarkedPos < zzEndRead) {
      FeatureLabel token = next();
      if (token != null)
        tokenized.add(token.word());
    }
    return tokenized;
  }

}
