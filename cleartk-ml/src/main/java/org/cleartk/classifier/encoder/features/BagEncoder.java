/** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.encoder.features;

import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.normalizer.NoOpNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.feature.Counts;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Encodes a Counts feature into a bag-of-words type feature.
 * 
 * This works on arbitrary types of objects, not just words. If an object's count is larger than
 * zero, the corresponding value (named using the .toString method of the object) is set to 1. If
 * the count is zero, the value is not included in the output.
 * 
 * @author Philipp Wetzler
 */
public class BagEncoder implements FeatureEncoder<NameNumber> {

  private static final long serialVersionUID = -5280514188425612793L;

  public BagEncoder(String identifier, NameNumberNormalizer normalizer) {
    this.identifier = identifier;
    this.normalizer = normalizer;
  }

  public BagEncoder(String identifier) {
    this(identifier, new NoOpNormalizer());
  }

  public BagEncoder(NameNumberNormalizer normalizer) {
    this(null, normalizer);
  }

  public BagEncoder() {
    this(null, new NoOpNormalizer());
  }

  public List<NameNumber> encode(Feature feature) {
    List<NameNumber> fves = new ArrayList<NameNumber>();
    Counts frequencies = (Counts) feature.getValue();

    String prefix = frequencies.getFeatureName();
    for (Object key : frequencies.getValues()) {
      if (frequencies.getCount(key) > 0) {
        String name = Feature.createName(prefix, key.toString());
        NameNumber fve = new NameNumber(name, 1);
        fves.add(fve);
      }
    }

    normalizer.normalize(fves);

    return fves;
  }

  public boolean encodes(Feature feature) {
    if (!(feature.getValue() instanceof Counts))
      return false;

    Counts counts = (Counts) feature.getValue();

    if (identifier == null)
      return true;

    if (identifier.equals(counts.getIdentifier()))
      return true;

    return false;
  }

  private String identifier;

  private NameNumberNormalizer normalizer;

}
