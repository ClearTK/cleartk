package org.cleartk.syntax.dependency;

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.util.UIMAUtil;
import org.junit.Test;

public class DependencyNodeUtil {

  public static void print(PrintStream out, DependencyNode topNode) {
    print(out, topNode, 0);
  }

  private static void print(PrintStream out, DependencyNode node, int tabs) {
    String indent = getIndent(tabs);
    out.println(indent+node.getCoveredText()+"\t"+node.getDependencyType()+"\t["+node.getBegin()+","+node.getEnd()+"]");
    if(tabs == 0) {
      out.println(getNumberString(node.getCoveredText().length()));
    }
    List<DependencyNode> children = UIMAUtil.toList(node.getChildren(), DependencyNode.class);
    for(DependencyNode child : children) {
      print(out, child, tabs + 1);
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

  private static String getIndent(int tabs) {
    char[] indentChars = new char[tabs];
    Arrays.fill(indentChars, '\t');
    return new String(indentChars);
  }
  
  @Test
  public void testGetNumberString() throws Exception {
    assertEquals("", getNumberString(0));
    assertEquals("0", getNumberString(1));
    assertEquals("01", getNumberString(2));
    assertEquals("012", getNumberString(3));
    assertEquals("0123", getNumberString(4));
    assertEquals("01234", getNumberString(5));
    assertEquals("012345", getNumberString(6));
    assertEquals("0123456", getNumberString(7));
    assertEquals("01234567", getNumberString(8));
    assertEquals("012345678", getNumberString(9));
    assertEquals("0123456789", getNumberString(10));
    assertEquals("01234567890", getNumberString(11));
    assertEquals("012345678901", getNumberString(12));
    assertEquals("0123456789012345678901234567890123456789", getNumberString(40));
    assertEquals("01234567890123456789012345678901234567890123", getNumberString(44));
    
  }
}
