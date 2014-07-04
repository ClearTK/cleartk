package org.cleartk.ml.tksvmlight.kernel;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.util.treebank.TreebankNode;

public class TreeKernelUtils {
  public static final List<TreebankNode> getNodeList(TreebankNode tree) {
    ArrayList<TreebankNode> list = new ArrayList<TreebankNode>();
    list.add(tree);
    for (int i = 0; i < list.size(); ++i) {
      list.addAll(list.get(i).getChildren());
    }
    return list;
  }
}
