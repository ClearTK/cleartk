/** 
 * 
 * Copyright (c) 2007-2012, Regents of the University of Colorado 
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
package org.cleartk.summarization;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.transform.InstanceStream;
import org.cleartk.util.Options_ImplBase;
import org.kohsuke.args4j.Option;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;

/**
 * 
 * This model is based on the SumBasic model described in:
 * <p>
 * Nenkova, Vanderwende and McKeown, <i>A compositional context sensitive multi-document summarizer:
 * exploring the factors that influence summarization</i>, SIGIR 20006 <br>
 * <p>
 * and
 * <p>
 * Nenkova and Vanderwende <i>The Impact of Frequency on Summarization</i> Microsoft Research
 * TechReport, MSR-TR-2005-101, 2005 <br>
 * 
 * @author Lee Becker
 * 
 */
public class SumBasicModel extends SummarizationModel_ImplBase {

  /**
	 * 
	 */
  private static final long serialVersionUID = -354873594945022087L;

  public static final String MODEL_NAME = "model.sumbasic";

  public static class Options extends Options_ImplBase {

    @Option(
        name = "--max-num-sentences",
        usage = "Specifies the maximum number of sentences to extract in the summary")
    public int maxNumSentences = 10;

    @Option(name = "--seen-words-prob", usage = "Specify the probability for seen words.")
    public double seenWordsProbability = 0.0001;

    @Option(
        name = "--composition-function",
        usage = "Specifies how word probabilities are combined (AVERAGE|SUM|PRODUCT, default=AVERAGE)")
    public CompositionFunctionType cfType = CompositionFunctionType.AVERAGE;
  }

  // Consider moving this elsewhere if needed by other classes
  public static class TermFrequencyMap {
    private Multiset<String> termFrequencies;

    public TermFrequencyMap() {
      this.termFrequencies = LinkedHashMultiset.create();
    }

    public void add(String term, int count) {
      this.termFrequencies.add(term, count);
    }

    public double getProbability(String term) {
      return (double) this.termFrequencies.count(term) / (double) this.termFrequencies.size();
    }

    public void save(URI outputURI) throws IOException {
      File out = new File(outputURI);
      BufferedWriter writer = null;
      writer = new BufferedWriter(new FileWriter(out));
      for (Multiset.Entry<String> entry : this.termFrequencies.entrySet()) {
        writer.append(String.format("%s\t%d\n", entry.getElement(), entry.getCount()));
      }
      writer.close();
    }

    public void load(URI inputURI) throws IOException {
      File in = new File(inputURI);
      BufferedReader reader = null;
      reader = new BufferedReader(new FileReader(in));

      // The lines are the term counts divided by a tab
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] termFreqPair = line.split("\\t");
        this.termFrequencies.add(termFreqPair[0], Integer.parseInt(termFreqPair[1]));
      }
      reader.close();
    }
  }

  /**
   * 
   * SumBasic can use three different Composition Functions to combine the probabilities for words
   * in a sentence.
   * <ul>
   * <li>PRODUCT - The product of the individual word probabilities within the sentence</li>
   * <li>SUM - The sum of the individual word probabilities within the sentence</li>
   * <li>AVERAGE - The sum of the individual word probabilities within the sentence</li>
   * </ul>
   * 
   * @author Lee Becker
   * 
   */
  public static enum CompositionFunctionType {
    AVERAGE, PRODUCT, SUM;
  }

  private static abstract class CompositionFunction implements Function<List<Feature>, Double> {
    protected Double seenWordProbability;

    protected TermFrequencyMap tfMap;

    protected Set<String> seenWords;

    public CompositionFunction(
        Double seenWordProbability,
        TermFrequencyMap tfMap,
        Set<String> seenWords) {
      this.seenWordProbability = seenWordProbability;
      this.tfMap = tfMap;
      this.seenWords = seenWords;
    }
  }

  private static class AverageCF extends CompositionFunction {
    private SumCF sumcf;

    public AverageCF(Double seenWordProbability, TermFrequencyMap tfMap, Set<String> seenWords) {
      super(seenWordProbability, tfMap, seenWords);
      this.sumcf = new SumCF(seenWordProbability, tfMap, seenWords);
    }

    @Override
    public Double apply(List<Feature> wordFeatures) {
      int numWords = wordFeatures.size();
      return (numWords == 0) ? 0.0 : this.sumcf.apply(wordFeatures) / numWords;
    }
  }

  private static class ProductCF extends CompositionFunction {

    public ProductCF(Double seenWordProbability, TermFrequencyMap tfMap, Set<String> seenWords) {
      super(seenWordProbability, tfMap, seenWords);
    }

    @Override
    public Double apply(List<Feature> wordFeatures) {
      double sentenceWeight = 1.0;
      for (Feature feature : wordFeatures) {
        if (this.seenWords.contains(feature.getName())) {
          sentenceWeight *= this.seenWordProbability;
        } else {
          sentenceWeight *= this.tfMap.getProbability(feature.getName());
        }
      }
      return sentenceWeight;
    }
  }

  private static class SumCF extends CompositionFunction {

    public SumCF(Double seenWordProbability, TermFrequencyMap tfMap, Set<String> seenWords) {
      super(seenWordProbability, tfMap, seenWords);
    }

    @Override
    public Double apply(List<Feature> wordFeatures) {
      Double sentenceWeight = 0.0;
      for (Feature feature : wordFeatures) {
        if (this.seenWords.contains(feature.getName())) {
          sentenceWeight += this.seenWordProbability;
        } else {
          sentenceWeight += this.tfMap.getProbability(feature.getName());
        }
      }
      return sentenceWeight;
    }
  }

  public static void trainAndWriteModel(File modelDirectory, String... args) throws Exception {
    // This is where the bulk of the sum basic algorithm resides

    Options options = new Options();
    options.parseOptions(args);
    int maxNumSentences = options.maxNumSentences;
    CompositionFunctionType cfType = options.cfType;
    double seenWordsProbability = options.seenWordsProbability;

    // Load the serialized instance data
    Iterable<Instance<Boolean>> instances = InstanceStream.loadFromDirectory(modelDirectory);

    // save off feature data and use it to compute term frequencies
    List<List<Feature>> sentences = new ArrayList<List<Feature>>();
    TermFrequencyMap tfMap = new TermFrequencyMap();
    for (Instance<Boolean> instance : instances) {
      sentences.add(instance.getFeatures());
      for (Feature feature : instance.getFeatures()) {
        tfMap.add(feature.getName(), (Integer) feature.getValue());
      }
    }

    // the set of words in the summary (will change over time)
    final Set<String> seenWords = new HashSet<String>();

    // create scoring function
    final CompositionFunction getScore;
    switch (cfType) {
      case PRODUCT:
        getScore = new ProductCF(seenWordsProbability, tfMap, seenWords);
        break;
      case SUM:
        getScore = new SumCF(seenWordsProbability, tfMap, seenWords);
        break;
      case AVERAGE:
      default:
        getScore = new AverageCF(seenWordsProbability, tfMap, seenWords);
        break;
    }

    // Use frequency data to select N sentences
    Map<List<Feature>, Double> selectedSentences = new HashMap<List<Feature>, Double>();
    for (int i = 0; i < maxNumSentences; i++) {

      // find the sentence with the maximum score
      List<Feature> selectedSentence = Ordering.natural().onResultOf(getScore).max(sentences);
      selectedSentences.put(selectedSentence, getScore.apply(selectedSentence));

      // Update the set of seen words with the words in the selected sentence
      // These are used to discount words that have already made their way into the summary
      for (Feature feature : selectedSentence) {
        seenWords.add(feature.getName());
      }
    }

    // Save off selected sentences as the model file
    try {
      SumBasicModel model = new SumBasicModel(selectedSentences);
      File outputFile = new File(modelDirectory, model.getModelName());
      ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
          new FileOutputStream(outputFile)));
      oos.writeObject(model);
      oos.close();
    } catch (FileNotFoundException e) {
      throw new Exception(e);
    } catch (IOException e) {
      throw new Exception(e);
    }
  }

  public SumBasicModel(Map<List<Feature>, Double> selectedSentencesScores) {
    super(selectedSentencesScores);
  }

  public SumBasicModel(InputStream inputStream) throws IOException {
    super(inputStream);
  }

  @Override
  public String getModelName() {
    return SumBasicModel.MODEL_NAME;
  }

}
