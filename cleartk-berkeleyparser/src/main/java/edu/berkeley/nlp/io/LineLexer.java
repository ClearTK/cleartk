package edu.berkeley.nlp.io;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Similar to PTBLineLexer. However, the constructor is customizable.
 * 
 * @author petrov
 * 
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
