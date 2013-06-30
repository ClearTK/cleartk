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
package org.cleartk.util.treebank;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class was written to be a stand alone parser for the Penn Treebank data. Basically, I need a
 * way to synch up the propbank data with extents of plain text that are labeled. This is not
 * possible to do without parsing the treebank data first. The parse method will parse a single
 * sentence from the treebank data from e.g. wsj/mrg/06/wsj_0656.mrg.
 * 
 * I initially looked at the OpenNLP treebank parser but they made a few assumptions about they
 * wanted to keep for the parser that would make it difficult to align with the propbank data. See:
 * http://sourceforge.net/projects/opennlp/forums/forum/9943/topic/1751983 for relevant discussion.
 * I looked at their parsing implementation and tried to modify it. However, I think the code below
 * bears little resemblance to theirs. But there may yet be some snippets taken directly out of that
 * code. The two regular expressions used are very similar.
 * 
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */
public class TreebankFormatParser {
  /**
   * used to identify tokens in Penn Treebank labeled constituents. It will match strings such as:
   * <ul>
   * <li>(NNP Community)
   * <li>(: --)
   * <li>(-NONE- *U*)
   * </ul>
   */
  public static final String LEAF_NODE_REGEX = "\\(([^( )]+) ([^( )]+)\\s*\\)";

  private static Pattern leafNodePattern = Pattern.compile(LEAF_NODE_REGEX);

  /**
   * Uses the leafNodePattern to identify a string as a terminal. Examples:
   * <ul>
   * <li>parseFragment = "(NNP Community)", returns a leaf node
   * <li>parseFragment = "(QP ($ $) (CD 107) (CD million) )", returns null
   * </ul>
   * 
   * @param parseFragment
   *          some fragment of a treebank parse.
   * @return if the string matches, then a node will be returned. Otherwise, null is returned.
   */
  public static TreebankNode getLeafNode(String parseFragment) {
    Matcher leafNodeMatcher = leafNodePattern.matcher(parseFragment);
    if (leafNodeMatcher.matches()) {
      String type = leafNodeMatcher.group(1);
      String value = leafNodeMatcher.group(2);
      TreebankNode node = new TreebankNode();
      node.setType(getTypeFromType(type));
      node.setTags(getTagsFromType(type));
      node.setValue(value);
      node.setLeaf(true);
      // the token and the value will almost always be the same except for
      // a few special values (e.g. "-RCB-", "-LCB-", and "-NONE-")
      String token = getToken(node.getValue(), node.getType());
      node.setText(token);
      return node;
    }
    return null;
  }

  private static String getTypeFromType(String fullType) {
    if (fullType.startsWith("-"))
      return fullType.substring(0, fullType.indexOf('-', 1) + 1);

    return fullType.split("[-=]")[0];
  }

  private static String[] getTagsFromType(String fullType) {
    if (fullType.startsWith("-")) {
      String rest = fullType.substring(fullType.indexOf('-', 1) + 1);
      if (rest.length() > 0)
        return rest.split("[-=]");
      else
        return new String[0];
    } else {
      String[] parts = fullType.split("[-=]");
      String[] tags = new String[parts.length - 1];

      for (int i = 1; i < parts.length; i++)
        tags[i - 1] = parts[i];
      return tags;
    }
  }

  /**
   * used to identify the type of a consituent in a treebank parse tree. It will match strings such
   * as:
   * <ul>
   * <li>"NNP" in "(NNP Community)"
   * <li>":" in "(: --)"
   * <li>"-NONE-" in "(-NONE- *U*)"
   * </ul>
   */
  public static final String TYPE_REGEX = "^\\(([^() ]+)";

  private static Pattern typePattern = Pattern.compile(TYPE_REGEX);

  /**
   * Returns the type of a constituent of some fragment of a treebank parse. Assumes that the first
   * character is a parenthesis. Examples:
   * <ul>
   * <li>parseFragment = "(NP-LOC (NNP Calif.) )" return = "NP-LOC"
   * <li>parseFragment = "(NP" return "NP"
   * <li>parseFragment = "(-NONE- *U*) ) (PP (IN of)" return = "-NONE-"
   * </ul>
   * 
   * @param parseFragment
   *          some fragment of a treebank parse
   * @return the type of the constituent.
   */
  public static String getType(String parseFragment) {
    Matcher typeMatcher = typePattern.matcher(parseFragment);
    if (typeMatcher.find())
      return typeMatcher.group(1);
    return null;
  }

  public static final String cleanUPRegex1 = "\\s+";

  private static final Pattern cleanUpPattern1 = Pattern.compile(cleanUPRegex1, Pattern.MULTILINE);

  public static final String cleanUPRegex2 = "\\( \\(";

  private static final Pattern cleanUpPattern2 = Pattern.compile(cleanUPRegex2, Pattern.MULTILINE);

  public static final String cleanUPRegex3 = "\\) \\)";

  private static final Pattern cleanUpPattern3 = Pattern.compile(cleanUPRegex3, Pattern.MULTILINE);

  public static final String cleanUPRegex4 = "\\s*\\(\\s*\\(";

  private static final Pattern cleanUpPattern4 = Pattern.compile(cleanUPRegex4, Pattern.MULTILINE);

  /**
   * This method was created simply as a way to clean up the parse string for a sentence in the
   * treebank syntax. The most important thing that it does is add a type called TOP to the top node
   * of the sentence. This simplifies parsing. The other string replacements just remove white space
   * and such and are probably unnecessary. This was inspired by the OpenNLP solution which takes in
   * one line at a time from a file that has been modified in this way.
   * 
   * @param parse
   *          a String in the treebank format
   * @return a String in the treebank that has been cleaned up a bit.
   */
  public static String prepareString(String parse) {
    parse = cleanUpPattern1.matcher(parse).replaceAll(" ");
    parse = cleanUpPattern2.matcher(parse).replaceAll("((");
    parse = cleanUpPattern3.matcher(parse).replaceAll("))");
    parse = cleanUpPattern4.matcher(parse).replaceFirst("(TOP (");
    return parse.trim();
  }

  /**
   * A treebank parse does not preserve whitespace information. This method provides a simple
   * mechanism for inferring the original plain text of a treebank parse. If you have access to the
   * original plain text, then you can bypass use of this method by calling the appropriate parse
   * method.
   * 
   * @see #parse(String, String, int)
   * 
   * @param treebankText
   *          One or more parses in Treebank parenthesized format.
   * @return a "best" guess of the original plain text given in the parse.
   */
  public static String inferPlainText(String treebankText) {
    StringBuilder sb = new StringBuilder();
    for (String parse : splitSentences(treebankText)) {
      Matcher matcher = leafNodePattern.matcher(parse);
      while (matcher.find()) {
        TreebankNode node = getLeafNode(matcher.group());
        if (node.getText() != null && node.getText().length() > 0) {
          int lastIndex = sb.length() - 1;
          if (lastIndex > 0 && !needsSpaceBefore(node.getText()) && sb.charAt(lastIndex) == ' ') {
            sb.deleteCharAt(lastIndex);
          }
          sb.append(node.getText());
          if (needsSpaceAfter(node.getText())) {
            sb.append(" ");
          }
        }
      }
      int lastIndex = sb.length() - 1;
      if (lastIndex >= 0 && sb.charAt(lastIndex) == ' ') {
        sb.deleteCharAt(lastIndex);
      }
      sb.append('\n');
    }
    return sb.toString().trim();
  }

  private static boolean needsSpaceBefore(String tokenText) {
    String[] noSpaceTokens = new String[] {
        ".",
        ",",
        ":",
        ";",
        "?",
        "'s",
        "'t",
        "\"",
        "!",
        ")",
        "]" };
    for (String noSpaceToken : noSpaceTokens) {
      if (tokenText.equals(noSpaceToken)) {
        return false;
      }
    }
    return true;
  }

  private static boolean needsSpaceAfter(String tokenText) {
    String[] noSpaceTokens = new String[] { "\"", "(", "[" };
    for (String noSpaceToken : noSpaceTokens) {
      if (tokenText.equals(noSpaceToken)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Create TreebankNode objects corresponding to the given TreeBank format parse, e.g.:
   * 
   * <PRE>
   * ( (X (NP (NP (NML (NN Complex ) (NN trait )) (NN analysis )) (PP (IN of ) (NP (DT the ) (NN mouse ) (NN striatum )))) (: : ) (S (NP-SBJ (JJ independent ) (NNS QTLs )) (VP (VBP modulate ) (NP (NP (NN volume )) (CC and ) (NP (NN neuron ) (NN number)))))) )
   * </PRE>
   * 
   * The text will be inferred automatically from the words in the parse.
   * 
   * @param parse
   *          A TreeBank formatted parse
   * @return The TreebankNode root of the parse tree
   * @see #inferPlainText(String)
   * @see #parse(String, String, int)
   */
  public static TopTreebankNode parse(String parse) {
    parse = prepareString(parse);
    String plainText = inferPlainText(parse).trim();
    return parse(parse, plainText, 0);
  }

  private static void checkText(TreebankNode node, String text) {
    String text1 = node.getText();
    int start = node.getTextBegin();
    int end = node.getTextEnd();
    String text2 = text.substring(start, end);
    if (!text1.equals(text2)) {
      // TreeBank adds in (. .) nodes in odd places, e.g. when a sentence
      // ends with U.S. (and no final period). As a result, we need to
      // allow periods to match whitespace and adjust the node bounds.
      String prefix1 = text1.substring(0, text1.length() - 1);
      String prefix2 = text2.substring(0, text2.length() - 1);
      if (text1.endsWith(".") && prefix1.equals(prefix2)) {
        node.setTextEnd(node.getTextEnd() - 1);
      } else {
        throw new IllegalArgumentException(
            "plain text does not align with tokens in treebank parse.  node text = '" + text1
                + "'  plain text = '" + text2 + "'");
      }
    }
  }

  /**
   * Create TreebankNode objects corresponding to the given TreeBank format parse, e.g.:
   * 
   * <PRE>
   * ( (X (NP (NP (NML (NN Complex ) (NN trait )) (NN analysis )) (PP (IN of ) (NP (DT the ) (NN mouse ) (NN striatum )))) (: : ) (S (NP-SBJ (JJ independent ) (NNS QTLs )) (VP (VBP modulate ) (NP (NP (NN volume )) (CC and ) (NP (NN neuron ) (NN number)))))) )
   * </PRE>
   * 
   * The start and end offsets of each TreebankNode will be aligned to the word offsets in the given
   * text.
   * 
   * @param parse
   *          A TreeBank formatted parse
   * @param text
   *          The text to which the parse should be aligned
   * @param textOffset
   *          The character offset at which the parse text should start to be aligned. For example,
   *          if the words of the parse start right at the beginning of the text, the appropriate
   *          textOffset is 0.
   * @return The TreebankNode root of the parse tree. The root node will be a TopTreebankNode, and
   *         all its descendants will be TreebankNodes.
   * @see TopTreebankNode
   * @see TreebankNode
   */
  public static TopTreebankNode parse(String parse, String text, int textOffset) {
    try {
      TopTreebankNode topNode = new TopTreebankNode();
      parse = prepareString(parse);
      // used to capture the plain text of the sentence.
      StringBuffer consumedText = new StringBuffer();
      if (text != null) {
        textOffset = movePastWhiteSpaceChars(text, textOffset);
        consumedText.append(text.substring(0, textOffset));
      }

      Stack<Integer> parseOffsetStack = new Stack<Integer>();
      Stack<Integer> plainTextOffsetStack = new Stack<Integer>();

      // keeps the nodes that are waiting for their parents to be completed.
      Stack<TreebankNode> parseStack = new Stack<TreebankNode>();

      for (int ci = 0; ci < parse.length(); ci++) {
        char c = parse.charAt(ci);
        if (c == '(') {
          // at the start of each constituent we will push the starting
          // index of it
          // w.r.t. the parse string.
          parseOffsetStack.push(ci);
          // also push the starting index w.r.t. the plain text of the
          // sentence.
          plainTextOffsetStack.push(consumedText.length());
        } else if (c == ')') {
          int begin = parseOffsetStack.pop();
          int end = ci;
          // the portion of the parse string that corresponds to the
          // constituent that
          // we found the left bracket for ')'.
          String subParse = parse.substring(begin, end + 1);

          int textBegin = plainTextOffsetStack.pop();

          TreebankNode node = getLeafNode(subParse);
          if (node != null) {
            node.setTopNode(topNode);
            node.setParseBegin(begin);
            node.setParseEnd(end + 1);
            String token = node.getText();

            if (token.length() > 0) {
              int realBegin = movePastWhiteSpaceChars(text, textBegin);
              consumedText.append(text.substring(textBegin, realBegin));
              consumedText.append(token);
              node.setTextBegin(realBegin);
              node.setTextEnd(realBegin + token.length());

            } else {
              node.setTextBegin(textBegin);
              node.setTextEnd(textBegin + token.length());
            }
            checkText(node, text);
            parseStack.push(node);
          } else {
            if (parse.lastIndexOf(')') == ci) // the last ')' is the top
              // node.
              node = topNode; // this is the instance that will be
            // returned.
            else
              node = new TreebankNode();
            node.setTopNode(topNode);
            node.setParseBegin(begin);
            node.setParseEnd(end + 1);
            String type = getType(subParse);
            node.setType(getTypeFromType(type));
            node.setTags(getTagsFromType(type));
            node.setLeaf(false);
            // keep adding the nodes on the stack until it is empty or
            // the next node on the stack starts before the current node
            // (i.e. has a different
            // parent than the current node that will be completed
            // later.)
            while (parseStack.size() > 0
                && parseStack.peek().getParseBegin() > node.getParseBegin()) {
              TreebankNode child = parseStack.pop();
              node.addChild(child);
              child.setParent(node);
            }
            // we typically add a token followed by a space to
            // plainText, except when the
            // token is an empty string as it is when the corresponding
            // type is -NONE-
            int realBegin = movePastWhiteSpaceChars(text, textBegin);
            node.setTextBegin(realBegin);
            node.setTextEnd(Math.max(realBegin, consumedText.length()));

            try {
              node.setText(consumedText.substring(node.getTextBegin(), node.getTextEnd()));
            } catch (StringIndexOutOfBoundsException sioobe) {
              node.setText("");
            }
            checkText(node, text);
            parseStack.push(node);
          }
        }
      }

      topNode.setTreebankParse(parse);
      topNode.initTerminalNodes();
      return topNode;
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("exception thrown when parsing the following: " + parse, e);
    }
  }

  private static final Pattern nonwhiteSpaceCharPattern = Pattern.compile("[^\\s]");

  public static int movePastWhiteSpaceChars(String text, int textOffset) {
    Matcher matcher = nonwhiteSpaceCharPattern.matcher(text);
    if (matcher.find(textOffset)) {
      return matcher.start();
    }
    return textOffset;
  }

  /**
   * Replace specially encoded tokens with their original textual representation.
   * (http://www.cis.upenn.edu/~treebank/tokenization.html)
   * 
   * @param value
   * @param type
   * @return The string in its original textual representation.
   */
  private static String getToken(String value, String type) {
    value = value.replace("-RCB-", "}");
    value = value.replace("-LCB-", "{");
    value = value.replace("-RRB-", ")");
    value = value.replace("-LRB-", "(");
    value = value.replace("-RSB-", "]");
    value = value.replace("-LSB-", "[");
    value = value.replace("``", "\"");
    value = value.replace("''", "\"");

    if (type.equals("-NONE-"))
      return "";

    if (value.contains("\\/"))
      return value.replace("\\/", "/");

    return value;
  }

  /**
   * Generally speaking, we expect one treebanked sentence per line. This method will simply return
   * the lines of a document assuming that each line has matching parentheses. However, the native
   * penn treebank data contains parsed sentences that are broken up across multiple lines. Each
   * sentence in the PTB starts with "( (S..." and so we split on this to get the sentences. If this
   * method sees "( (S...", then it will return the contents split on that pattern. If not, it will
   * return the lines of the input string.
   * 
   * Splits an .mrg file (e.g. wsj/mrg/00/wsj_0020.mrg) into sentence parses.
   * 
   * @return individual sentence parses from treebank - i.e. strings of the form "( (S..."
   */

  public static String[] splitSentences(String mrgContents) {
    // Splitting on this regular expression can cause the first value
    // in the array to be an empty string if e.g. the first line of the file
    // is blank
    String[] contents = mrgContents.split("(?=\\(\\s*\\()");
    if (contents.length > 1) {
      if (contents.length > 0 && contents[0].trim().equals("")) {
        String[] returnValues = new String[contents.length - 1];
        System.arraycopy(contents, 1, returnValues, 0, returnValues.length);
        return returnValues;
      } else {
        String[] returnValues = new String[contents.length];
        System.arraycopy(contents, 0, returnValues, 0, returnValues.length);
        return returnValues;
      }
    }

    String[] lines = mrgContents.split("\r?\n");
    for (String line : lines) {
      if (!parensMatch(line)) {
        throw new IllegalArgumentException(
            "Parentheses counts do not match for treebank sentence: " + line);
      }
    }
    return lines;
  }

  public static boolean parensMatch(String contents) {
    int leftParenCount = 0;
    int rightParenCount = 0;

    for (char c : contents.toCharArray()) {
      if (c == '(')
        leftParenCount++;
      if (c == ')')
        rightParenCount++;
    }

    return leftParenCount == rightParenCount;

  }

  /**
   * This method parses an entire documents worth of treebanked sentences.
   * 
   * @param parse
   *          a single document provided as treebank parenthesized parses
   * @param textOffset
   *          a value that corresponds to the character offset of the first character of the
   *          document. The appropriate value for this method will typically be 0.
   * @param text
   *          a single document provided as plain text. If you do not have access to the original
   *          plain text of the document, you can generate some using
   *          {@link #inferPlainText(String)}.
   */
  public static List<TopTreebankNode> parseDocument(String parse, int textOffset, String text) {
    List<TopTreebankNode> returnValues = new ArrayList<TopTreebankNode>();
    String[] sentenceParses = splitSentences(parse);

    for (String sentenceParse : sentenceParses) {
      TopTreebankNode topNode = parse(sentenceParse, text, textOffset);
      textOffset = topNode.getTextEnd();
      returnValues.add(topNode);
    }
    return returnValues;
  }

}
