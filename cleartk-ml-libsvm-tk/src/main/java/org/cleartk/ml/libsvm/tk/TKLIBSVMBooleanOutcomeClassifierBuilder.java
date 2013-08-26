package org.cleartk.ml.libsvm.tk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.chboston.cnlp.kernel.CustomKernel;
import org.chboston.cnlp.kernel.KernelManager;
import org.chboston.cnlp.libsvm.svm_model;
import org.chboston.cnlp.libsvm.svm_node;
import org.chboston.cnlp.libsvm.svm_parameter;
import org.chboston.cnlp.libsvm.ex.Instance;
import org.chboston.cnlp.libsvm.ex.SVMTrainer;
import org.cleartk.classifier.svmlight.model.Kernel;
import org.cleartk.classifier.svmlight.model.LinearKernel;
import org.cleartk.classifier.svmlight.model.PolynomialKernel;
import org.cleartk.classifier.svmlight.model.RBFKernel;
import org.cleartk.classifier.tksvmlight.TreeFeatureVector;
import org.cleartk.classifier.tksvmlight.TreeKernelSVMBooleanOutcomeClassifier;
import org.cleartk.classifier.tksvmlight.TreeKernelSVMBooleanOutcomeClassifierBuilder;
import org.cleartk.classifier.tksvmlight.model.CompositeKernel.ComboOperator;
import org.cleartk.classifier.tksvmlight.model.CompositeKernel.Normalize;
import org.cleartk.classifier.tksvmlight.model.TreeKernel;
import org.cleartk.classifier.tksvmlight.model.TreeKernel.ForestSumMethod;
import org.cleartk.classifier.tksvmlight.model.TreeKernel.KernelType;
import org.cleartk.classifier.tksvmlight.model.TreeKernelSVMModel;
import org.cleartk.classifier.util.featurevector.FeatureVector;
import org.cleartk.classifier.util.featurevector.InvalidFeatureVectorValueException;
import org.cleartk.classifier.util.featurevector.SparseFeatureVector;


public class TKLIBSVMBooleanOutcomeClassifierBuilder extends
  TreeKernelSVMBooleanOutcomeClassifierBuilder<TreeKernelSVMBooleanOutcomeClassifier> {

  private TreeKernelSVMModel model;
  
  Options options = new Options();
  
  public TKLIBSVMBooleanOutcomeClassifierBuilder(){
    options.addOption("c", true, "Cost parameter");
    options.addOption("t", true, "Kernel type");
    options.addOption("W", true, "Tree sequence comparision method (S sequential or A for all vs. all)");
    options.addOption("V", true, "Vector sequence comparison method");
    options.addOption("S", true, "Secondary kernel for composite kernels");
    options.addOption("C", true, "Combination operator for composite/tree kernels");
    options.addOption("L", true, "Decay rate for tree kernels (lambda in Collins & Duffy");
    options.addOption("T", true, "Multiplicative constant for tree kernel in composite kernel");
    options.addOption("N", true, "Normalization parameter for composite kernels");
    options.addOption("d", true, "Degree of polynomial kernel");
    options.addOption("r", true, "Parameter c in poly kernel");
    options.addOption("s", true, "Parameter s in poly kernel");
    options.addOption("g", true, "Gamma in rbf kernel");
  }
  
  @Override
  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.libsvm");
  }

  @Override
  public void trainClassifier(File filePath, String... args) throws Exception {
    // read and parse options
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = parser.parse(options, args);
    
    // set parameters
    Kernel fk = null;
    
    CustomKernel<TreeFeatureVector> kernel = null;
    
    double cost = Double.parseDouble(cmd.getOptionValue("c", "1"));
    int kernelType = Integer.parseInt(cmd.getOptionValue("t"));
    int secondaryKernel = Integer.parseInt(cmd.getOptionValue("S", "1"));
    int d = Integer.parseInt(cmd.getOptionValue("d", "3"));
    int c = Integer.parseInt(cmd.getOptionValue("r", "1"));
    int s = Integer.parseInt(cmd.getOptionValue("s", "1"));
    double gamma = Double.parseDouble(cmd.getOptionValue("g", "1.0"));
    double lambda = Double.parseDouble(cmd.getOptionValue("L", "0.4"));
    String comboOperator = cmd.getOptionValue("C", "T");
    String sumMethod = cmd.getOptionValue("W", "S");
    int normalize = Integer.parseInt(cmd.getOptionValue("N", "3"));
    double tkWeight = Double.parseDouble(cmd.getOptionValue("T", "1.0"));
    
    if(kernelType == 5){
      
      switch(secondaryKernel){
      case 0: fk = new LinearKernel(); break;
      case 1: 
        fk = new PolynomialKernel(s, c, d); break;
      case 2:
        fk = new RBFKernel(gamma); break;
      }
      TreeKernel tk = new TreeKernel(lambda, sumMethod.equals("S") ? ForestSumMethod.SEQUENTIAL : ForestSumMethod.ALL_PAIRS, KernelType.SUBSET, normalize % 2 > 0);
      ComboOperator op = null;
      if(comboOperator.equals("+")) op = ComboOperator.SUM;
      else if(comboOperator.equals("*")) op = ComboOperator.PRODUCT;
      else if(comboOperator.equals("T")) op = ComboOperator.TREE_ONLY;
      else op = ComboOperator.VECTOR_ONLY;
      Normalize norm = null;
      switch(normalize){
      case 0: norm = Normalize.NEITHER; break;
      case 1: norm = Normalize.TREE; break;
      case 2: norm = Normalize.VECTOR; break;
      case 3: norm = Normalize.BOTH; break;
      }
      kernel = new CustomCompositeKernel(fk, tk, op, tkWeight, norm);
      KernelManager.setCustomKernel(kernel);
    }else{
      throw new Exception("If you are not doing a tree/composite kernel (t = 5) then you should just use the regular libsvm module");
    }
        
    // read in file into Instance array
    List<Instance<TreeFeatureVector>> instances = readInstances(filePath);
    
    int highestIndex = 0;
    for(Instance<TreeFeatureVector> inst : instances){
      FeatureVector vec = inst.getData().getFeatures();
      for(FeatureVector.Entry entry : vec){
        if(entry.index > highestIndex){
          highestIndex = entry.index;
        }
      }
    }
    
    // give the SVMTrainer the instance array and the kernel parameters
    svm_parameter param = new svm_parameter();
    param.svm_type = svm_parameter.C_SVC;
    param.C = cost;
    param.shrinking = 0;
    String modelFilename = filePath.getPath() + ".model";
    svm_model<TreeFeatureVector> model = SVMTrainer.train(instances, param);
    
    PrintWriter out = new PrintWriter(modelFilename);
    out.println("cleartk-ml-libsvm-tk wrapper and bridge for svmlight/libsvm tree kernel libraries");
    out.println("5 # kernel type");
    out.print(d);    out.println(" # kernel parameter -d");
    out.print(gamma);     out.println(" # kernel parameter -g");
    out.print(s);    out.println(" # kernel parameter -s");
    out.print(c);    out.println(" # kernel parameter -r");
    out.println("empty# kernel paramater -u");
    out.print(lambda);        out.println(" # kernel parameter -L");
    out.print(tkWeight);      out.println(" # kernel parameter -T");
    out.print(comboOperator); out.println(" # kernel parameter -C");
    out.println("1 # Kernel paramter -F -- don't know what this does");
    out.print(secondaryKernel); out.println(" # kernel parameter -S (secondary kernel)");
    out.println("1 # kernel parameter -D");
    out.print(normalize); out.println(" # kernel parameter -N");
    out.print("S"); out.println(" # kernel parameter -V");
    out.print("S"); out.println(" # kernel parameter -W");
    out.print(highestIndex); out.println(" # highest feature index");
    out.print(instances.size()); out.println(" # number of training documents");
    out.print(model.l+1); out.println(" # number of support vectors + 1");
    out.print(model.rho[0]); out.println(" # threshold b, each following line is a SV (starting with alpha*y)");
    
    // print out SVs:
    for(int i = 0; i < model.SV.length; i++){
      svm_node<TreeFeatureVector> node = model.SV[i];
      out.print(model.sv_coef[0][i]);
      TreeFeatureVector treeVec = node.data;
      LinkedHashMap<String,String> trees = treeVec.getTrees();
      if(trees.size() > 0){
        for(String tree : trees.values()){
          out.print(" |BT| ");
          out.print(tree);
        }
        out.print(" |ET| ");
      }
      
      FeatureVector featVec = treeVec.getFeatures();
      for(FeatureVector.Entry feat : featVec){
        out.print(feat.index);
        out.print(":");
        out.print(feat.value);
        out.print(" ");
      }
      out.print("|EV|");
      out.println();
    }
    out.close();    
  }

  private List<Instance<TreeFeatureVector>> readInstances(File filePath) throws IOException {
    List<Instance<TreeFeatureVector>> instances = new ArrayList<Instance<TreeFeatureVector>>();
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    String line = null;
    while((line = reader.readLine()) != null){
      instances.add(readInstance(line));
    }
    reader.close();
    return instances;
  }

  private Instance<TreeFeatureVector> readInstance(String line) throws IOException {
    int firstWhitespace = line.indexOf(' ');
    TreeFeatureVector treeVector = new TreeFeatureVector();
    String label = line.substring(0, firstWhitespace);
    String rest = line.substring(firstWhitespace).trim();
    String treeSect = null;
    String vectSect = null;
    
    // figure out what combination of trees & vectors are in each instance:
    int etInd = rest.indexOf("|ET|");
    if(etInd >= 0){
      treeSect = rest.substring(0, etInd-1).trim();
//      if(rest.substring(etInd).contains("|EV|")) {
      vectSect = rest.substring(etInd + 4); // skip over |ET|
//      }
    }else {
      vectSect = rest;
    }
    
    // read trees:
    LinkedHashMap<String,String> treeFeats = new LinkedHashMap<String,String>();
    if(treeSect != null){
      String[] trees = treeSect.substring(5).split("\\|BT\\|");
      for(int i = 0; i < trees.length; i++){
        treeFeats.put("TK_feat_" + i, trees[i].trim());
      }
    }
    
    // read feature vectors:
    FeatureVector vec = new SparseFeatureVector();
    if (vectSect != null) {
      String[] features = vectSect.trim().split(" +");
      for (int i = 0; i < features.length; i++) {
        String[] parts = features[i].split(":");
        int featureIndex = Integer.valueOf(parts[0]);
        double featureValue = Double.valueOf(parts[1]);
        try {
          vec.set(featureIndex, featureValue);
        } catch (InvalidFeatureVectorValueException e) {
          throw new IOException(e);
        }
      }
    }
    
    treeVector.setTrees(treeFeats);
    treeVector.setFeatures(vec);
        
    return new Instance<TreeFeatureVector>(Double.parseDouble(label), treeVector);
  }

  @Override
  protected TreeKernelSVMBooleanOutcomeClassifier newClassifier() {
    return new TreeKernelSVMBooleanOutcomeClassifier(this.featuresEncoder, this.outcomeEncoder, this.model);
  }
}
