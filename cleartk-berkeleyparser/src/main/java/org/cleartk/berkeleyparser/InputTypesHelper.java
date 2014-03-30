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

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * InputTypesHelper allows constituent wrappers to abstract away the input token and sentence types
 * that it expects. The default implementation uses the ClearTK token and sentence types, but by
 * extending this class you could specify your own input types from your type system.
 * 
 * @author Philip Ogren
 */

public abstract class InputTypesHelper<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> {
  public abstract List<TOKEN_TYPE> getTokens(JCas jCas, SENTENCE_TYPE sentence);

  public abstract List<SENTENCE_TYPE> getSentences(JCas jCas);

  public abstract String getPosTag(TOKEN_TYPE token);

  public abstract void setPosTag(TOKEN_TYPE token, String tag);

}
