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
package org.cleartk.token.tokenizer;

import java.util.regex.Pattern;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 *         This code was derived directly (i.e. translated) from a python script that Steven Bethard
 *         gave me called tokenizer.py. This copyright of this script is owned by the Regents of the
 *         University of Colorado and therefore there is no restriction on our use and distribution
 *         of this derivitive work here. The python script was derived from a sed script available
 *         here:
 * 
 *         http://www.cis.upenn.edu/~treebank/tokenization.html
 * 
 *         Steve's script fixes several misc. errors.
 * 
 * @author Philip Ogren
 * 
 */
public class PennTreebankTokenizer extends Tokenizer_ImplBase {
  // different brace type regexes
  public static String openBracesRegex = "\\[\\(\\{\\<";

  public static String closedBracesRegex = "\\]\\)\\}\\>";

  public static String bracesRegex = "([" + openBracesRegex + closedBracesRegex + "])";

  public static Pattern bracesPattern = Pattern.compile(bracesRegex);

  // ellipsis regex
  public static String ellipsisRegex = "(" + Pattern.quote("...") + ")";

  public static Pattern ellipsisPattern = Pattern.compile(ellipsisRegex);

  // comma regex - any comma that is not between two digits
  public static String commaRegex = "((?<!\\d),|,(?!\\d))";

  public static Pattern commaPattern = Pattern.compile(commaRegex);

  // dollar sign regex
  // any dollar sign, potentially preceded by capitals (e.g. US$)
  public static String dollarSignRegex = "([A-Z]*\\$)";

  public static Pattern dollarSignPattern = Pattern.compile(dollarSignRegex);

  // ampersand matcher regex
  // any ampersamd not surrounded by two uppercase letters
  public static String ampersandRegex = "((?<![A-Z])&|&(?![A-Z]))";

  public static Pattern ampersandPattern = Pattern.compile(ampersandRegex);

  // dash regex
  // any set of 2 or more dashes, or any dash followed by whitespace
  public static String dashRegex = "(--+|-(?=\\s))";

  public static Pattern dashPattern = Pattern.compile(dashRegex);

  // colon regex
  // if colon's between digits, then take the digits (e.g. 4:30)
  public static String colonRegex = "(\\d+:\\d+|:)";

  public static Pattern colonPattern = Pattern.compile(colonRegex);

  // other punctuation regex
  // punctuation not followed by a dash (e.g. not 62%-owned)
  public static String nonFinalPunctRegex = "(``|[|;@#`%])(?!-)";

  public static Pattern nonFinalPunctPattern = Pattern.compile(nonFinalPunctRegex);

  // period regex
  // any period ending a sequence of digits, or
  // any set of exactly two periods, or
  // any period not preceded by two other periods and followed only
  // by punctuation to the end of the line
  public static String periodRegex = "((?<=\\d)\\.(?=[^\\n\\S])|" + "(?<=[^.]\\.)\\.(?![.])|"
      + "(?<!\\.\\.)\\.[" + closedBracesRegex + "\"'`/_#*\\s]*$)";

  public static Pattern periodPattern = Pattern.compile(periodRegex, Pattern.MULTILINE);

  // any punctuation that always indicates the end of a sentence
  public static String nonPeriodPunctRegex = "([?!])";

  public static Pattern nonPeriodPunctPattern = Pattern.compile(nonPeriodPunctRegex);

  // single quote regex
  // a single quote preceding digits and an optional s(e.g. '80s), or
  // any single quote not beside another single quote and that has
  // whitespace on one side or the other
  public static String singleQuoteRegex = "('\\d+s?|(?<=\\s)'(?!')|(?<!')'(?=\\s))";

  public static Pattern singleQuotePattern = Pattern.compile(singleQuoteRegex);

  public static String tripleQuoteRegex = "'''";

  public static Pattern tripleQuotePattern = Pattern.compile(tripleQuoteRegex);

  public static String doubleQuoteRegex = "''";

  public static Pattern doubleQuotePattern = Pattern.compile(doubleQuoteRegex);

  public static String quoteRegex = Pattern.quote("\"");

  public static Pattern quotePattern = Pattern.compile(quoteRegex);

  // abbreviation regexes
  public static String oneWordAbbreviationRegex = "('ll|'re|'ve|n't|'[smd])\\b";

  public static Pattern oneWordAbbreviationPattern = Pattern.compile(
      oneWordAbbreviationRegex,
      Pattern.CASE_INSENSITIVE);

  public static String[] twoWordAbbreviationRegexes = new String[] {
      "\\b(can)(not)\\b",
      "\\b(d')(ye)\\b",
      "\\b(gim)(me)\\b",
      "\\b(gon)(na)\\b",
      "\\b(got)(ta)\\b",
      "\\b(lem)(me)\\b",
      "\\b(more)('n)\\b",
      "\\b(wan)(na)\\b" };

  public static Pattern[] twoWordAbbreviationPatterns = new Pattern[twoWordAbbreviationRegexes.length];
  static {
    for (int i = 0; i < twoWordAbbreviationRegexes.length; i++) {
      twoWordAbbreviationPatterns[i] = Pattern.compile(
          twoWordAbbreviationRegexes[i],
          Pattern.CASE_INSENSITIVE);
    }
  }

  public static String[] threeWordAbbreviationRegexes = new String[] {
      "\\b(wha)(dd)(ya)\\b",
      "\\b(wha)(t)(cha)\\b" };

  public static Pattern[] threeWordAbbreviationPatterns = new Pattern[threeWordAbbreviationRegexes.length];
  static {
    for (int i = 0; i < threeWordAbbreviationRegexes.length; i++) {
      threeWordAbbreviationPatterns[i] = Pattern.compile(
          threeWordAbbreviationRegexes[i],
          Pattern.CASE_INSENSITIVE);
    }
  }

  public static String tAbbreviationRegex = "('t)(is|was)\\b";

  public static Pattern tAbbreviationPattern = Pattern.compile(tAbbreviationRegex);

  // space regexes
  public static String beginOrEndRegex = "^|$";

  public static Pattern beginOrEndPattern = Pattern.compile(beginOrEndRegex, Pattern.MULTILINE);

  public static String extraSpaceRegex = "^(\\s+)|(\\s+)$|(?<=[ \\t])[ \\t]+";

  public static Pattern extraSpacePattern = Pattern.compile(extraSpaceRegex, Pattern.MULTILINE);

  public static String multipleWhitespaceRegex = "(\\s+)";

  public static Pattern multipleWhitespacePattern = Pattern.compile(
      multipleWhitespaceRegex,
      Pattern.MULTILINE);

  protected Pattern[] patterns;

  public PennTreebankTokenizer() {
    patterns = new Pattern[] {
        ellipsisPattern,
        commaPattern,
        dollarSignPattern,
        ampersandPattern,
        dashPattern,
        colonPattern,
        nonFinalPunctPattern,
        periodPattern,
        nonPeriodPunctPattern,
        bracesPattern };
  }

  /**
   * Tokenizes the input text and returns a string array corresponding to the tokens.
   * 
   * @param text
   * @return the tokens
   */
  public String[] getTokenTexts(String text) {
    for (Pattern pattern : patterns) {
      text = pattern.matcher(text).replaceAll(" $1 ");
    }

    text = beginOrEndPattern.matcher(text).replaceAll(" ");

    text = tripleQuotePattern.matcher(text).replaceAll(" ' '' ");
    text = doubleQuotePattern.matcher(text).replaceAll(" '' ");
    text = singleQuotePattern.matcher(text).replaceAll(" $1 ");
    text = quotePattern.matcher(text).replaceAll(" \" ");

    text = oneWordAbbreviationPattern.matcher(text).replaceAll(" $1");
    for (Pattern pattern : twoWordAbbreviationPatterns)
      text = pattern.matcher(text).replaceAll(" $1 $2");
    text = tAbbreviationPattern.matcher(text).replaceAll(" $1 $2");
    for (Pattern pattern : threeWordAbbreviationPatterns)
      text = pattern.matcher(text).replaceAll(" $1 $2 $3");

    text = extraSpacePattern.matcher(text).replaceAll("");

    // this was added because one of beginOrEndPattern or extraSpacePattern do not seem to be
    // working correctly.
    text = multipleWhitespacePattern.matcher(text).replaceAll(" ");

    String[] tokens = text.toString().split(" ");
    if (tokens.length == 1 && tokens[0].equals("")) {
      tokens = new String[0];
    }
    return tokens;
  }

}
