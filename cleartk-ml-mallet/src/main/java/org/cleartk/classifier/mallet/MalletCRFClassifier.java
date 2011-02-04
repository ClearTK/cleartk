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
package org.cleartk.classifier.mallet;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.CleartkEncoderException;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.SequenceClassifier_ImplBase;
import org.cleartk.util.ReflectionUtil;

import cc.mallet.fst.Transducer;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Sequence;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 *         This classifier provides an interface to the <a
 *         href="http://mallet.cs.umass.edu/index.php/SimpleTagger_example"> Mallet Conditional
 *         Random Field (CRF) tagger</a>. Annotators that use a sequence learner such as this one
 *         will need to support classification of a sequence of instances.
 */
public class MalletCRFClassifier extends
    SequenceClassifier_ImplBase<List<NameNumber>, String, String> {

  protected Transducer transducer;

  public MalletCRFClassifier(
      FeaturesEncoder<List<NameNumber>> featuresEncoder,
      OutcomeEncoder<String, String> outcomeEncoder,
      Transducer transducer) {
    super(featuresEncoder, outcomeEncoder);
    this.transducer = transducer;
  }

  /**
   * This method classifies several instances at once
   * 
   * @param features
   *          a list of lists of features - each list in the list represents one instance to be
   *          classified. The list should correspond to some logical sequence of instances to be
   *          classified (e.g. tokens in a sentence or lines in a document) that corresponds to the
   *          model that has been built for this classifier.
   */
  public List<String> classify(List<List<Feature>> features) throws CleartkProcessingException {
    String[][] featureStringArray = toStrings(features);
    Pipe pipe = transducer.getInputPipe();

    Instance instance = new Instance(featureStringArray, null, null, null);
    instance = pipe.instanceFrom(instance);

    Sequence<?> data = (Sequence<?>) instance.getData();
    Sequence<?> untypedSequence = transducer.transduce(data);
    Sequence<String> sequence = ReflectionUtil.uncheckedCast(untypedSequence);

    List<String> returnValues = new ArrayList<String>();

    for (int i = 0; i < sequence.size(); i++) {
      String encodedOutcome = sequence.get(i);
      returnValues.add(outcomeEncoder.decode(encodedOutcome));
    }
    return returnValues;
  }

  /**
   * Converts the features into a 2D string array that Mallet can use. The only thing mildly tricky
   * about this method is that the length of each string array is one more than the size of each
   * feature list - i.e. <code>returnValues[0].length == features.get(0).size() + 1</code> where the
   * last element in each string array is an empty string.
   * 
   * @param features
   *          the features to be converted.
   * @return a 2D string array that Mallet can use.
   */
  private String[][] toStrings(List<List<Feature>> features) throws CleartkEncoderException {
    List<List<String>> encodedFeatures = new ArrayList<List<String>>(features.size());
    for (List<Feature> features1 : features) {
      List<NameNumber> nameNumbers = this.featuresEncoder.encodeAll(features1);
      List<String> encodedFeatures1 = new ArrayList<String>();
      for (NameNumber nameNumber : nameNumbers) {
        encodedFeatures1.add(nameNumber.name);
      }
      encodedFeatures.add(encodedFeatures1);
    }

    String[][] encodedFeaturesArray = new String[encodedFeatures.size()][];
    for (int i = 0; i < encodedFeatures.size(); i++) {
      String[] encodedFeaturesArray1 = encodedFeatures.get(i).toArray(new String[0]);
      encodedFeaturesArray[i] = encodedFeaturesArray1;
    }

    return encodedFeaturesArray;
  }
}
