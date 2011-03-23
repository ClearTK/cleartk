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

package org.cleartk.syntax.dependency;

import java.io.PrintStream;
import java.util.Arrays;

import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.util.UIMAUtil;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * @author Philip Ogren
 */

public class DependencyNodeUtil {

  public static void print(PrintStream out, DependencyNode topNode) {
    print(out, topNode, 0);
  }

  private static void print(PrintStream out, DependencyNode node, int tabs) {
    String indent = getIndent(tabs);
    if(tabs == 0) {
      out.println(indent+node.getCoveredText());
      out.println(getNumberString(node.getCoveredText().length()));
      out.println(getTics(node.getCoveredText().length()));
      out.println();
    } else {
      StringBuilder depType = new StringBuilder();
      for (DependencyRelation depRel : UIMAUtil.toList(
          node.getHeadRelations(),
          DependencyRelation.class)) {
        depType.append(depRel.getRelation());
      }
      out.printf(
          "%s%s\t%s\t[%d,%d]\n",
          indent,
          node.getCoveredText(),
          depType,
          node.getBegin(),
          node.getEnd());
    }
    for (DependencyRelation depRel : UIMAUtil.toList(
        node.getChildRelations(),
        DependencyRelation.class)) {
      print(out, depRel.getChild(), tabs + 1);
    }
  }
  
  private static String getNumberString(int length) {
    String numbers = "0123456789";
    StringBuilder sb = new StringBuilder();
    for(int i=0; i < length / 10; i++) {
      sb.append(numbers);
    }
    sb.append(numbers.substring(0, length % 10));
    return sb.toString();
  }

  private static String getTics(int length) {
    StringBuilder sb = new StringBuilder();
    for(int i=0; i < length / 10; i++) {
      sb.append(""+i+"         ");
    }
    return sb.toString();
  }

  private static String getIndent(int tabs) {
    char[] indentChars = new char[tabs];
    Arrays.fill(indentChars, '\t');
    return new String(indentChars);
  }
  
  
}
