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
package org.cleartk.ml.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Instance;
import org.cleartk.ml.SequenceDataWriter;
import org.cleartk.ml.SequenceDataWriterFactory;

/**
 * A simple {@link SequenceDataWriter} that stores all instances in a public attribute. Intended
 * only for testing.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public class PublicFieldSequenceDataWriter<T> implements SequenceDataWriter<T> {

  public List<Instance<T>> instances;

  public PublicFieldSequenceDataWriter() {
    this.instances = new ArrayList<Instance<T>>();
  }

  public void finish() {
  }

  public void write(List<Instance<T>> instance) {
    this.instances.addAll(instance);
  }

  /**
   * Returns a single static instance of {@code InstanceCollector<String>}.
   */
  public static class StringFactory implements SequenceDataWriterFactory<String> {
    private static PublicFieldSequenceDataWriter<String> collector = new PublicFieldSequenceDataWriter<String>();

    public SequenceDataWriter<String> createDataWriter() {
      return collector;
    }

    public static List<Instance<String>> collectInstances(AnalysisEngine engine, JCas jCas)
        throws AnalysisEngineProcessException {
      return PublicFieldSequenceDataWriter.collectInstances(engine, jCas, collector);
    }
  }

  /**
   * Returns a single static instance of {@code InstanceCollector<String>}.
   */
  public static class BooleanFactory implements SequenceDataWriterFactory<Boolean> {
    private static PublicFieldSequenceDataWriter<Boolean> collector = new PublicFieldSequenceDataWriter<Boolean>();

    public SequenceDataWriter<Boolean> createDataWriter() {
      return collector;
    }

    public static List<Instance<Boolean>> collectInstances(AnalysisEngine engine, JCas jCas)
        throws AnalysisEngineProcessException {
      return PublicFieldSequenceDataWriter.collectInstances(engine, jCas, collector);
    }
  }

  static <T> List<Instance<T>> collectInstances(
      AnalysisEngine engine,
      JCas jCas,
      PublicFieldSequenceDataWriter<T> collector) throws AnalysisEngineProcessException {
    collector.instances.clear();
    engine.process(jCas);
    engine.collectionProcessComplete();
    return collector.instances;
  }
}
