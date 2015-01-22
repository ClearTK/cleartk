/*
 * Copyright (c) 2007-2013, Regents of the University of Colorado 
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
package org.cleartk.ml.tksvmlight.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

import org.cleartk.ml.svmlight.model.Kernel;
import org.cleartk.ml.svmlight.model.LinearKernel;
import org.cleartk.ml.svmlight.model.PolynomialKernel;
import org.cleartk.ml.svmlight.model.RbfKernel;
import org.cleartk.ml.svmlight.model.SigmoidKernel;
import org.cleartk.ml.svmlight.model.UnsupportedKernelError;
import org.cleartk.ml.tksvmlight.TreeFeature;
import org.cleartk.ml.tksvmlight.TreeFeatureVector;
import org.cleartk.ml.tksvmlight.kernel.PartialTreeKernel;
import org.cleartk.ml.tksvmlight.kernel.SubsetTreeKernel;
import org.cleartk.ml.tksvmlight.kernel.TreeKernel;
import org.cleartk.ml.tksvmlight.model.CompositeKernel.ComboOperator;
import org.cleartk.ml.tksvmlight.model.CompositeKernel.Normalize;
import org.cleartk.ml.util.featurevector.FeatureVector;
import org.cleartk.ml.util.featurevector.InvalidFeatureVectorValueException;
import org.cleartk.ml.util.featurevector.SparseFeatureVector;

import com.google.common.annotations.Beta;

/**
 * 
 * 
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 */
@Beta
public class TreeKernelSvmModel {
  protected String version;

  protected CompositeKernel kernel = null;

  protected double bias;

  protected TkSupportVector[] supportVectors = null;

  public double evaluate(TreeFeatureVector fv) {
    double result = -bias;

    for (TkSupportVector sv : supportVectors) {
      result += sv.getAlpha() * kernel.evaluate(fv, sv.tfv);
    }

    return result;
  }

  public static TreeKernelSvmModel fromFile(File modelFile) throws IOException {
    InputStream modelStream = new FileInputStream(modelFile);
    TreeKernelSvmModel model = fromInputStream(modelStream);
    modelStream.close();
    return model;
  }

  public static TreeKernelSvmModel fromInputStream(InputStream modelStream) throws IOException {
    return fromInputStream(modelStream, null);
  }

  public static TreeKernelSvmModel fromInputStream(InputStream modelStream, TreeKernel tk) throws IOException {
    TreeKernelSvmModel model = new TreeKernelSvmModel();
    TkSvmLightReader in = new TkSvmLightReader(modelStream);
    Kernel featKernel = null;
    TreeKernel treeKernel = tk;
    String buffer;

    // Model File Version
    buffer = in.readLine();
    model.version = buffer;

    // kernel type
    int ktype = Integer.parseInt(in.readLine());
    // kernel parameter -d (parameter degree in polynomial kernel)
    int kpar_degree = Integer.parseInt(in.readLine());
    // kernel parameter -g (parameter gamma in rbf kernel)
    double kpar_gamma = Double.parseDouble(in.readLine());
    // kernel parameter -s (parameter s in sigmoid/poly kernel)
    double kpar_s = Double.parseDouble(in.readLine());
    // kernel parameter -r (parameter c in sigmoid/poly kernel)
    double kpar_c = Double.parseDouble(in.readLine());
    // kernel parameter -u (parameter of user defined kernel)
    in.readLine();
    // kernel parameter -L (decay factor in tree kernel)
    double lambda = Double.parseDouble(in.readLine());
    // kernel parameter -T (multiplicative constant for the contribution of tree kernels when -C =
    // '+')
    double multiplier = Double.parseDouble(in.readLine());
    // kernel parameter -C (combine kernel types: {+, *, T, V} for add, mult, tree only, vector
    // only)
    String comboOperator = in.readLine();
    // kernel parameter -F (???)
    in.readLine();
    // kernel parameter -S (kernel to be used with vectors)
    int fkType = Integer.parseInt(in.readLine());
    // kernel parameter -D (0, SubTree kernel or 1, SubSet Tree kernels)
    int tkType = (int) Double.parseDouble(in.readLine());
    // kernel parameter -N (0 = no normalization, 1 = tree, 2 = vector and 3 = both)
    int norm = Integer.parseInt(in.readLine());
    // kernel parameter -V (kernel applied to 'S' (sequence of trees), or 'A' (all tree pairs))
    in.readLine();
    // kernel parameter -W (kernel applied to 'S' (sequence of trees), or 'A' (all tree pairs))
    in.readLine();

    for (int kCode : new int[] { ktype, fkType }) {
      if (featKernel != null) {
        // no need to look at the fkType if the ktype is present - not sure what that would contain
        break;
      }
      switch (kCode) {
        case 0:
          // linear kernel
          featKernel = new LinearKernel();
          break;
        case 1:
          // polynomial kernel
          featKernel = new PolynomialKernel(kpar_s, kpar_c, kpar_degree);
          break;
        case 2:
          // rbf kernel
          featKernel = new RbfKernel(kpar_gamma);
          break;
        case 3:
          // sigmoid kernel
          featKernel = new SigmoidKernel(kpar_s, kpar_c);
          break;
        case 5:
          break;
        default:
          throw new UnsupportedKernelError();
      }
    }

    ComboOperator operator = null;
    // check for code for TK
    if (ktype == 5) {
      boolean normalize = norm == 1 || norm == 3;

      if(tkType == 1){
        treeKernel = new SubsetTreeKernel(lambda, normalize);
      }else if(tkType == 3){
        treeKernel = new PartialTreeKernel(lambda, PartialTreeKernel.MU_DEFAULT, normalize);
      }else if(treeKernel == null){
        throw new UnsupportedKernelError();
      }
      if (comboOperator.equals("+")) {
        operator = ComboOperator.SUM;
      } else if (comboOperator.equals("*")) {
        operator = ComboOperator.PRODUCT;
      } else if (comboOperator.equals("T")) {
        operator = ComboOperator.TREE_ONLY;
      } else if (comboOperator.equals("V")) {
        operator = ComboOperator.VECTOR_ONLY;
      } else {
        throw new UnsupportedKernelError();
      }
    } else {
      operator = ComboOperator.VECTOR_ONLY;
    }

    Normalize normal = Normalize.BOTH;
    switch (norm) {
      case 0:
        normal = Normalize.NEITHER;
        break;
      case 1:
        normal = Normalize.TREE;
        break;
      case 2:
        normal = Normalize.VECTOR;
        break;
      case 3:
        normal = Normalize.BOTH;
        break;
      default:
        throw new UnsupportedKernelError();
    }
    model.kernel = new CompositeKernel(featKernel, treeKernel, operator, multiplier, normal);

    in.readLine(); // feature index
    in.readLine(); // num documents
    int numSVs = Integer.valueOf(in.readLine()) - 1; // number of support vectors
    model.bias = Double.valueOf(in.readLine());

    in.inSVs = true;
    // read support vectors
    model.supportVectors = new TkSupportVector[numSVs];
    for (int i = 0; i < numSVs; i++) {
      model.supportVectors[i] = readSV(in);
    }

    return model;
  }

  private static TkSupportVector readSV(TkSvmLightReader in) throws IOException {
    String line = in.readLine();
    int firstWhitespace = line.indexOf(' ');
    double alpha_y = Double.parseDouble(line.substring(0, firstWhitespace));
    String treesAndVecs = line.substring(firstWhitespace).trim();

    String treeSect = null;
    String vectSect = null;

    int etInd = treesAndVecs.indexOf("|ET|");
    if (etInd >= 0) {
      // there are at least trees here.
      treeSect = treesAndVecs.substring(0, etInd - 1).trim();
      if (treesAndVecs.substring(etInd).contains("|EV|")) {
        vectSect = treesAndVecs.substring(etInd + 4); // skip over symbol
      }
    } else {
      vectSect = treesAndVecs;
    }

    LinkedHashMap<String, TreeFeature> trees = new LinkedHashMap<>();
    if (treeSect != null) {
      String[] subtrees = treeSect.substring(5).split("\\|BT\\|"); // skip over "|BT| " at start
      for (int i = 0; i < subtrees.length; i++) {
        String defaultFeatName = "TK_" + i;
        TreeFeature tf = treeStringToFeature(subtrees[i], defaultFeatName);
        trees.put(tf.getName(), tf);
      }
    }

    FeatureVector fv = new SparseFeatureVector();

    if (vectSect != null) {
      String[] features = vectSect.trim().split(" +");
      for (int i = 0; i < features.length - 1; i++) {
        String[] parts = features[i].split(":");
        int featureIndex = Integer.valueOf(parts[0]);
        double featureValue = Double.valueOf(parts[1]);
        try {
          fv.set(featureIndex, featureValue);
        } catch (InvalidFeatureVectorValueException e) {
          throw new IOException(e);
        }
      }
    }

    TreeFeatureVector tfv = new TreeFeatureVector();
    tfv.setFeatures(fv);
    tfv.setTrees(trees);
    return new TkSupportVector(alpha_y, tfv);
  }

  public static TreeFeature treeStringToFeature(String subtree, String defaultFeatName){
    String featName = defaultFeatName;
    String featVal;
    String[] nameVal = subtree.trim().split("::");
    if(nameVal.length > 1){
      featName = nameVal[0];
      featVal = nameVal[1];
    }else{
      featVal = nameVal[0];
    }
    featVal = featVal.replaceAll("\\((\\S+)\\)", "$1");
    return new TreeFeature(featName, featVal);
  }

  public static String treeFeatureToString(TreeFeature tf){
    String featString = tf.getKernel() == null ? tf.getValue().toString() :
      tf.getName() + "::" + tf.getValue();
    return featString;
  }

  private static class TkSvmLightReader {
    BufferedReader reader;

    boolean inSVs = false;

    TkSvmLightReader(InputStream modelStream) {
      this.reader = new BufferedReader(new InputStreamReader(modelStream));
    }

    String readLine() throws IOException {
      String line = reader.readLine().trim();
      // need a special case here because there can be "comment" symbols (#) in the
      // tree as part of the sentence, don't want to burden upstream processing by
      // requiring escaping in svmlight-tk code.
      if (inSVs) {
        return line;
      } else {
        return line.split("#")[0].trim();
      }
    }
  }

}
