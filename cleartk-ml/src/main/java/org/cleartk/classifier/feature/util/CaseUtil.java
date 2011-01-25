/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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

package org.cleartk.classifier.feature.util;

import java.util.Locale;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class CaseUtil {

  public static boolean isInitialUppercase(String word) {
    return isInitialUppercase(word, Locale.getDefault());
  }

  public static boolean isInitialUppercase(String word, Locale locale) {
    if (word.length() < 2) {
      return false;
    }
    String firstLetter = word.substring(0, 1);
    String rest = word.substring(1);
    // the first condition checks that capitalization matters at all to the first character - e.g.
    // if the first letter is 1 then the first condition will be true.
    // the third condition checks that capitalization matters at all to the rest of the word - e.g.
    // if the rest of the word is 1234 then the first condition will be true.
    return !firstLetter.equals(firstLetter.toLowerCase(locale))
        && firstLetter.equals(firstLetter.toUpperCase(locale))
        && !rest.equals(rest.toUpperCase(locale)) && rest.equals(rest.toLowerCase(locale));
  }

  public static boolean isAllUppercase(String word) {
    return isAllUppercase(word, Locale.getDefault());
  }

  public static boolean isAllUppercase(String word, Locale locale) {
    return !word.equals(word.toLowerCase(locale)) && word.equals(word.toUpperCase(locale));
  }

}
