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
package org.cleartk.ml.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.DataWriterFactory;
import org.cleartk.ml.Instance;

/**
 * A simple {@link DataWriter} that stores all instances in a public attribute. Intended only for
 * testing.
 * 
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public class PublicFieldDataWriter<T> implements DataWriter<T> {

  public List<Instance<T>> instances;

  public PublicFieldDataWriter() {
    this.instances = new ArrayList<Instance<T>>();
  }

  @Override
  public void finish() {
  }

  @Override
  public void write(Instance<T> instance) {
    this.instances.add(instance);
  }

  /**
   * Returns a single static instance of {@code InstanceCollector<String>}.
   */
  public static class StringFactory implements DataWriterFactory<String> {
    private static PublicFieldDataWriter<String> collector = new PublicFieldDataWriter<String>();

    @Override
    public DataWriter<String> createDataWriter() {
      return collector;
    }

    public static List<Instance<String>> collectInstances(AnalysisEngine engine, JCas jCas)
            throws AnalysisEngineProcessException {
      return PublicFieldDataWriter.collectInstances(engine, jCas, collector);
    }
  }

  /**
   * Returns a single static instance of {@code InstanceCollector<String>}.
   */
  public static class BooleanFactory implements DataWriterFactory<Boolean> {
    private static PublicFieldDataWriter<Boolean> collector = new PublicFieldDataWriter<Boolean>();

    @Override
    public DataWriter<Boolean> createDataWriter() {
      return collector;
    }

    public static List<Instance<Boolean>> collectInstances(AnalysisEngine engine, JCas jCas)
            throws AnalysisEngineProcessException {
      return PublicFieldDataWriter.collectInstances(engine, jCas, collector);
    }
  }

  static <T> List<Instance<T>> collectInstances(AnalysisEngine engine, JCas jCas,
          PublicFieldDataWriter<T> collector) throws AnalysisEngineProcessException {
    collector.instances.clear();
    engine.process(jCas);
    engine.collectionProcessComplete();
    return collector.instances;
  }
}
