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
package org.cleartk.classifier.encoder.features;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.normalizer.EuclidianNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.feature.Counts;
import org.cleartk.classifier.feature.transform.util.TfidfExtractor;
import org.cleartk.classifier.util.tfidf.IDFMap;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Encodes a Counts feature into TF-IDF values.
 * 
 * A normalizer can be given, which will be applied to the list of encoded features. If no
 * normalizer is given, it defaults to a euclidian normalizer.
 * 
 * If a name is supplied this encoder will only dispatch on features of that name.
 * 
 * @author Philipp Wetzler
 * 
 */

/**
 * @deprecated replaced by {@link TfidfExtractor}
 */
@Deprecated
public class TFIDFEncoder implements FeatureEncoder<NameNumber> {

  private static final long serialVersionUID = -5280514188425612793L;

  public TFIDFEncoder(String identifier, File idfMapFile, NameNumberNormalizer normalizer)
      throws IOException {
    this.identifier = identifier;
    this.normalizer = normalizer;

    this.idfMap = IDFMap.read(idfMapFile);
  }

  public TFIDFEncoder(String identifier, File idfFile) throws IOException {
    this(identifier, idfFile, new EuclidianNormalizer());
  }

  public TFIDFEncoder(File idfFile, NameNumberNormalizer normalizer) throws IOException {
    this(null, idfFile, normalizer);
  }

  public TFIDFEncoder(File idfFile) throws IOException {
    this(null, idfFile, new EuclidianNormalizer());
  }

  public List<NameNumber> encode(Feature feature) {
    List<NameNumber> fves = new ArrayList<NameNumber>();
    Counts counts = (Counts) feature.getValue();
    String prefix = counts.getFeatureName();

    for (Object key : counts.getValues()) {
      double tf = getTF(counts, key);
      double idf = idfMap.getIDF(key);

      String name = Feature.createName(prefix, key.toString());
      NameNumber fve = new NameNumber(name, tf * idf);
      fves.add(fve);
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

  private static double getTF(Counts counts, Object key) {
    return (double) counts.getCount(key) / (double) counts.getTotalCount();
  }

  private String identifier;

  private NameNumberNormalizer normalizer;

  private IDFMap idfMap;

}
