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
package org.cleartk.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.SequentialDataWriterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * A simple instance consumer that stores all instances in a public
 * attribute. Intended only for testing.
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public class InstanceCollector<T> implements DataWriter<T>, SequentialDataWriter<T> {

	public List<Instance<T>> instances;

	public InstanceCollector() {
		this.instances = new ArrayList<Instance<T>>();
	}

	public void finish() throws CleartkException {
	}

	public Class<? extends ClassifierBuilder<T>> getDefaultClassifierBuilderClass() {
		return null;
	}

	public void write(Instance<T> instance) throws CleartkException {
		this.instances.add(instance);
	}

	public void writeSequence(List<Instance<T>> instance) throws CleartkException {
		this.instances.addAll(instance);
	}

	/**
	 * Returns a single static instance of InstanceCollector<String>.
	 */
	public static class StringFactory implements DataWriterFactory<String>, SequentialDataWriterFactory<String>{
		private static InstanceCollector<String> collector = new InstanceCollector<String>();
		public DataWriter<String> createDataWriter(File outputDirectory) throws IOException {
			return collector;
		}
		public SequentialDataWriter<String> createSequentialDataWriter(File outputDirectory) throws IOException {
			return collector;
		}
		public static List<Instance<String>> collectInstances(AnalysisEngine engine, JCas jCas)
		throws AnalysisEngineProcessException {
			return InstanceCollector.collectInstances(engine, jCas, collector);
		}
	}


	/**
	 * Returns a single static instance of InstanceCollector<String>.
	 */
	public static class BooleanFactory implements DataWriterFactory<Boolean> {
		private static InstanceCollector<Boolean> collector = new InstanceCollector<Boolean>();
		public DataWriter<Boolean> createDataWriter(File outputDirectory) throws IOException {
			return collector;
		}
		public static List<Instance<Boolean>> collectInstances(AnalysisEngine engine, JCas jCas)
		throws AnalysisEngineProcessException {
			return InstanceCollector.collectInstances(engine, jCas, collector);
		}
	}
	
	private static <T> List<Instance<T>> collectInstances(
			AnalysisEngine engine, JCas jCas, InstanceCollector<T> collector)
			throws AnalysisEngineProcessException {
		collector.instances.clear();
		engine.process(jCas);
		engine.collectionProcessComplete();
		return collector.instances;
	}
}
