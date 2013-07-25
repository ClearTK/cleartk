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
package org.cleartk.classifier.feature.extractor;

import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;

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
public class CharacterCategoryPatternExtractor<T extends Annotation> implements
    NamedFeatureExtractor1<T> {

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
    REPEATS_MERGED,
    /**
     * Similar to REPEATS_MERGED, but distinguishes between the same category appearing once and
     * more than once in a row. If the same category appears twice or more in a row, then we will
     * mark that category with a Kleene plus '+'. For example "X000" would get the pattern "LuNd+"
     * since there is a single uppercase letter followed by more than one digit.
     */
    REPEATS_AS_KLEENE_PLUS
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
      case REPEATS_AS_KLEENE_PLUS:
        this.name = "CharPatternRepeatsAsKleenePlus";
        break;
    }
  }

  @Override
  public String getFeatureName() {
    return this.name;
  }

  @Override
  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {
    StringBuilder builder = new StringBuilder();
    String text = focusAnnotation.getCoveredText();
    String lastType = null;
    boolean multipleRepeats = false;
    for (int i = 0; i < text.length(); i += 1) {
      char c = text.charAt(i);
      String type = classifyChar(c);
      switch (this.patternType) {
        case ONE_PER_CHAR:
          builder.append(type);
          break;
        case REPEATS_MERGED:
          if (!type.equals(lastType)) {
            builder.append(type);
          }
          break;
        case REPEATS_AS_KLEENE_PLUS:
          if (!type.equals(lastType)) {
            builder.append(type);
            multipleRepeats = false;
          } else if (!multipleRepeats) {
            builder.append('+');
            multipleRepeats = true;
          }
      }
      lastType = type;
    }
    return Collections.singletonList(new Feature(this.name, builder.toString()));
  }

  protected String classifyChar(char c) {
    int typeInt = Character.getType(c);
    switch (typeInt) {
      case Character.CONTROL:
        return "CC";
      case Character.FORMAT:
        return "Cf";
      case Character.UNASSIGNED:
        return "Cn";
      case Character.PRIVATE_USE:
        return "Co";
      case Character.SURROGATE:
        return "Cs";
      case Character.LOWERCASE_LETTER:
        return "Ll";
      case Character.MODIFIER_LETTER:
        return "Lm";
      case Character.OTHER_LETTER:
        return "Lo";
      case Character.TITLECASE_LETTER:
        return "Lt";
      case Character.UPPERCASE_LETTER:
        return "Lu";
      case Character.COMBINING_SPACING_MARK:
        return "Mc";
      case Character.ENCLOSING_MARK:
        return "Me";
      case Character.NON_SPACING_MARK:
        return "Mn";
      case Character.DECIMAL_DIGIT_NUMBER:
        return "Nd";
      case Character.LETTER_NUMBER:
        return "Nl";
      case Character.OTHER_NUMBER:
        return "No";
      case Character.CONNECTOR_PUNCTUATION:
        return "Pc";
      case Character.DASH_PUNCTUATION:
        return "Pd";
      case Character.END_PUNCTUATION:
        return "Pe";
      case Character.FINAL_QUOTE_PUNCTUATION:
        return "Pf";
      case Character.INITIAL_QUOTE_PUNCTUATION:
        return "Pi";
      case Character.OTHER_PUNCTUATION:
        return "Po";
      case Character.START_PUNCTUATION:
        return "Ps";
      case Character.CURRENCY_SYMBOL:
        return "Sc";
      case Character.MODIFIER_SYMBOL:
        return "Sk";
      case Character.MATH_SYMBOL:
        return "Sm";
      case Character.OTHER_SYMBOL:
        return "So";
      case Character.LINE_SEPARATOR:
        return "Zl";
      case Character.PARAGRAPH_SEPARATOR:
        return "Zp";
      case Character.SPACE_SEPARATOR:
        return "Zs";
      default:
        throw new RuntimeException("Unknown character type: " + typeInt);
    }
  }
}
