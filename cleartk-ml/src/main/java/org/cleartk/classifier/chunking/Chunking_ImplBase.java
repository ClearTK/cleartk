/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.classifier.chunking;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Base class for classes that assemble individual classifier outcomes on smaller annotations
 * ("sub-chunks") to form larger annotations ("chunks").
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public abstract class Chunking_ImplBase<SUB_CHUNK_TYPE extends Annotation, CHUNK_TYPE extends Annotation>
    implements Chunking<String, SUB_CHUNK_TYPE, CHUNK_TYPE> {

  protected Class<? extends CHUNK_TYPE> chunkClass;

  protected Class<? extends SUB_CHUNK_TYPE> subChunkClass;

  protected String featureFullName;

  public Chunking_ImplBase(
      Class<? extends SUB_CHUNK_TYPE> subChunkClass,
      Class<? extends CHUNK_TYPE> chunkClass,
      String featureName) {
    this.subChunkClass = subChunkClass;
    this.chunkClass = chunkClass;
    this.featureFullName = featureName == null ? null : chunkClass.getCanonicalName() + ":"
        + featureName;
  }

  protected Feature getFeature(JCas jCas) {
    String name = this.featureFullName;
    return name == null ? null : jCas.getTypeSystem().getFeatureByFullName(name);
  }

  protected String getOutcomeSuffix(CHUNK_TYPE chunk, Feature feature) {
    return feature == null ? "" : "-" + chunk.getFeatureValueAsString(feature);
  }

  /**
   * Produce a map from sub-chunk annotations to their outcome prefixes (e.g. 'I', 'O').
   * 
   * If any sub-chunk annotations are not included in the map, they will be given prefix 'O'.
   * 
   * @param subChunks
   *          The sub-annotations that make up the chunks.
   * @param chunks
   *          The chunk annotations.
   * @return A mapping from chunk sub-chunk annotations to outcome prefixes.
   */
  protected abstract Map<SUB_CHUNK_TYPE, String> getSubChunkToOutcomeMap(
      JCas jCas,
      List<SUB_CHUNK_TYPE> subChunks,
      List<CHUNK_TYPE> chunks);

  @Override
  public List<String> toOutcomes(JCas jCas, List<SUB_CHUNK_TYPE> subChunks, List<CHUNK_TYPE> chunks)
      throws AnalysisEngineProcessException {

    // get the mapping from sub-chunks to their outcomes
    Map<SUB_CHUNK_TYPE, String> subChunkToOutcome;
    subChunkToOutcome = this.getSubChunkToOutcomeMap(jCas, subChunks, chunks);

    // create one outcome for each sub-chunk by combining the prefix and feature value
    List<String> outcomes = new ArrayList<String>();
    for (SUB_CHUNK_TYPE subChunk : subChunks) {
      String outcome = subChunkToOutcome.get(subChunk);
      if (outcome == null) {
        outcome = "O";
      }
      outcomes.add(outcome);
    }
    return outcomes;
  }

  /**
   * Determines whether the current outcome represents the end of a chunk.
   * 
   * Both the current outcome and the following outcome are provided for making this decision.
   * 
   * @param currPrefix
   *          The prefix of the current outcome
   * @param currLabel
   *          The label of the current outcome
   * @param nextPrefix
   *          The prefix of the following outcome
   * @param nextLabel
   *          The label of the following outcome
   * @return True if the current outcome represents the end of a chunk
   */
  protected abstract boolean isEndOfChunk(
      char currPrefix,
      String currLabel,
      char nextPrefix,
      String nextLabel);

  @Override
  public List<CHUNK_TYPE> toChunks(JCas jCas, List<SUB_CHUNK_TYPE> subChunks, List<String> outcomes)
      throws AnalysisEngineProcessException {

    // validate parameters
    int nSubChunks = subChunks.size();
    int nOutcomes = outcomes.size();
    if (nSubChunks != nOutcomes) {
      String message = "expected the same number of sub-chunks (%d) as outcome s(%d)";
      throw new IllegalArgumentException(String.format(message, nSubChunks, nOutcomes));
    }

    // get the Feature object if we need to assign an attribute
    Feature feature;
    if (this.featureFullName == null) {
      feature = null;
    } else {
      feature = jCas.getTypeSystem().getFeatureByFullName(this.featureFullName);
    }

    // parse outcomes, and add a final Outside outcome for ease of parsing
    List<ChunkOutcome> chunkOutcomes = new ArrayList<ChunkOutcome>();
    for (String outcome : outcomes) {
      chunkOutcomes.add(new ChunkOutcome(outcome));
    }
    chunkOutcomes.add(new ChunkOutcome("O"));

    // create chunk annotations as appropriate for the outcomes
    List<CHUNK_TYPE> chunks = new ArrayList<CHUNK_TYPE>();
    for (int i = 0; i < outcomes.size(); ++i) {
      ChunkOutcome outcome = chunkOutcomes.get(i);

      // if we're at the beginning of a chunk, gather outcomes until we hit the end of the chunk
      // (a chunk ends when we hit 'O' or when the label change, e.g. I-PER I-ORG)
      if (outcome.prefix != 'O') {

        // advance to the end of this chunk
        int begin = i;
        int end = i;
        while (true) {
          ChunkOutcome curr = chunkOutcomes.get(end);
          ChunkOutcome next = chunkOutcomes.get(end + 1);
          if (this.isEndOfChunk(curr.prefix, curr.label, next.prefix, next.label)) {
            break;
          }
          ++end;
        }

        // skip over all the outcomes we just consumed
        i = end;

        // convert the outcome indexes into CAS offsets
        begin = subChunks.get(begin).getBegin();
        end = subChunks.get(end).getEnd();

        // construct the chunk annotation
        Constructor<? extends CHUNK_TYPE> constructor;
        try {
          constructor = this.chunkClass.getConstructor(JCas.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
          throw new AnalysisEngineProcessException(e);
        }
        CHUNK_TYPE chunk;
        try {
          chunk = constructor.newInstance(jCas, begin, end);
        } catch (InstantiationException e) {
          throw new AnalysisEngineProcessException(e);
        } catch (IllegalAccessException e) {
          throw new AnalysisEngineProcessException(e);
        } catch (InvocationTargetException e) {
          throw new AnalysisEngineProcessException(e);
        }

        // set the annotation feature if necessary
        if (this.featureFullName != null) {
          chunk.setFeatureValueFromString(feature, outcome.label);
        }

        // add the chunk to the CAS and to the result list
        chunk.addToIndexes();
        chunks.add(chunk);
      }
    }
    return chunks;
  }

  private static class ChunkOutcome {
    public char prefix;

    public String label;

    public ChunkOutcome(String outcome) {
      this.prefix = outcome.charAt(0);
      this.label = outcome.length() < 2 ? "" : outcome.substring(2);
      if ("BIO".indexOf(this.prefix) == -1) {
        throw new IllegalArgumentException("Unrecognized BIO outcome: " + outcome);
      }
    }
  }
}
