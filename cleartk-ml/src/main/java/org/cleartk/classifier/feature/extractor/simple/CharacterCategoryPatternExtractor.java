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
package org.cleartk.classifier.feature.extractor.simple;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

/**
 * A feature extractor that generates a pattern based on the <a
 * href="http://unicode.org/reports/tr49/">Unicode categories</a> of each of the characters in the
 * annotation text. For example, "A-z0" is an uppercase letter, followed by a dash, followed by a
 * lowercase letter, followed by a digit, and so would get the pattern "LuPdLlNd".
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CharacterCategoryPatternExtractor implements SimpleFeatureExtractor {

  /**
   * The type of pattern to generate in feature values.
   */
  public static enum PatternType {
    /**
     * The standard pattern, where one category abbreviation is added to the feature value for each
     * character in the text.
     */
    ONE_PER_CHAR,
    /**
     * A simplified pattern, where if the same category appears many times in a row, the category is
     * added to the feature value only once. For example "XX00" would get the pattern "LuNd" since
     * there are two uppercase letters followed by two digits.
     */
    REPEATS_MERGED
  }

  private PatternType patternType;

  private String name;

  /**
   * Create the standard feature extractor, where one category is added to the feature value for
   * each character in the text. See {@link PatternType#ONE_PER_CHAR}.
   */
  public CharacterCategoryPatternExtractor() {
    this(PatternType.ONE_PER_CHAR);
  }

  /**
   * Create a feature extractor with the specified pattern type. See {@link PatternType} for the
   * acceptable pattern types.
   * 
   * @param patternType
   *          The type of pattern to generate in feature values.
   */
  public CharacterCategoryPatternExtractor(PatternType patternType) {
    this.patternType = patternType;
    switch (this.patternType) {
      case ONE_PER_CHAR:
        this.name = "CharPattern";
        break;
      case REPEATS_MERGED:
        this.name = "CharPatternRepeatsMerged";
        break;
    }
  }

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {
    StringBuilder builder = new StringBuilder();
    String text = focusAnnotation.getCoveredText();
    String lastType = null;
    for (int i = 0; i < text.length(); i += 1) {
      String type;
      int typeInt = Character.getType(text.charAt(i));
      switch (typeInt) {
        case Character.CONTROL:
          type = "CC";
          break;
        case Character.FORMAT:
          type = "Cf";
          break;
        case Character.UNASSIGNED:
          type = "Cn";
          break;
        case Character.PRIVATE_USE:
          type = "Co";
          break;
        case Character.SURROGATE:
          type = "Cs";
          break;
        case Character.LOWERCASE_LETTER:
          type = "Ll";
          break;
        case Character.MODIFIER_LETTER:
          type = "Lm";
          break;
        case Character.OTHER_LETTER:
          type = "Lo";
          break;
        case Character.TITLECASE_LETTER:
          type = "Lt";
          break;
        case Character.UPPERCASE_LETTER:
          type = "Lu";
          break;
        case Character.COMBINING_SPACING_MARK:
          type = "Mc";
          break;
        case Character.ENCLOSING_MARK:
          type = "Me";
          break;
        case Character.NON_SPACING_MARK:
          type = "Mn";
          break;
        case Character.DECIMAL_DIGIT_NUMBER:
          type = "Nd";
          break;
        case Character.LETTER_NUMBER:
          type = "Nl";
          break;
        case Character.OTHER_NUMBER:
          type = "No";
          break;
        case Character.CONNECTOR_PUNCTUATION:
          type = "Pc";
          break;
        case Character.DASH_PUNCTUATION:
          type = "Pd";
          break;
        case Character.END_PUNCTUATION:
          type = "Pe";
          break;
        case Character.FINAL_QUOTE_PUNCTUATION:
          type = "Pf";
          break;
        case Character.INITIAL_QUOTE_PUNCTUATION:
          type = "Pi";
          break;
        case Character.OTHER_PUNCTUATION:
          type = "Po";
          break;
        case Character.START_PUNCTUATION:
          type = "Ps";
          break;
        case Character.CURRENCY_SYMBOL:
          type = "Sc";
          break;
        case Character.MODIFIER_SYMBOL:
          type = "Sk";
          break;
        case Character.MATH_SYMBOL:
          type = "Sm";
          break;
        case Character.OTHER_SYMBOL:
          type = "So";
          break;
        case Character.LINE_SEPARATOR:
          type = "Zl";
          break;
        case Character.PARAGRAPH_SEPARATOR:
          type = "Zp";
          break;
        case Character.SPACE_SEPARATOR:
          type = "Zs";
          break;
        default:
          throw new RuntimeException("Unknown character type: " + typeInt);
      }
      switch (this.patternType) {
        case ONE_PER_CHAR:
          builder.append(type);
          break;
        case REPEATS_MERGED:
          if (!type.equals(lastType)) {
            builder.append(type);
          }
          break;
      }
      lastType = type;
    }
    return Arrays.asList(new Feature(this.name, builder.toString()));
  }
}
