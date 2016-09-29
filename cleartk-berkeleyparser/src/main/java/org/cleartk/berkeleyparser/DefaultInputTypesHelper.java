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
package org.cleartk.berkeleyparser;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.apache.uima.fit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class DefaultInputTypesHelper implements InputTypesHelper<Token, Sentence> {

  public List<Token> getTokens(JCas jCas, Sentence sentence) {
    return JCasUtil.selectCovered(jCas, Token.class, sentence);
  }

  @Override
  public String getPosTag(Token token) {
    return token.getPos();
  }

  @Override
  public void setPosTag(Token token, String tag) {
    token.setPos(tag);
  }

  public List<Sentence> getSentences(JCas jCas) {
    return new ArrayList<Sentence>(JCasUtil.select(jCas, Sentence.class));
  }



  public Token buildToken(JCas jCas, int begin, int end) {
    return new Token(jCas, begin, end);
  }

}
