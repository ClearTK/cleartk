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
package org.cleartk.timeml.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.cleartk.eval.AnnotationStatistics;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.util.CleartkInternalModelFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.base.Function;
import com.google.common.base.Objects;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TemporalLinkModelInfo extends ModelInfo<TemporalLink> {

  public TemporalLinkModelInfo(CleartkInternalModelFactory modelFactory, String[] trainingArguments) {
    super(
        TemporalLink.class,
        "relationType",
        TemporalLinkSpanAndRelation.ANNOTATION_CONVERTER,
        modelFactory,
        trainingArguments);
  }

  public TemporalLinkModelInfo(CleartkInternalModelFactory modelFactory) {
    this(modelFactory, new String[0]);
  }

  @Override
  public void updateStatistics(AnnotationStatistics statistics, JCas goldView, JCas systemView) {
    // restrict evaluation to only the TLINKs that were present in the gold data
    Collection<TemporalLink> goldTlinks = JCasUtil.select(goldView, this.annotatedClass);
    Collection<TemporalLink> systemTlinks = JCasUtil.select(systemView, this.annotatedClass);
    Set<TemporalLinkSpan> goldSpans = new HashSet<TemporalLinkSpan>();
    for (TemporalLink tlink : goldTlinks) {
      goldSpans.add(new TemporalLinkSpan(tlink));
    }
    List<TemporalLink> systemTlinksWithGoldSpans = new ArrayList<TemporalLink>();
    for (TemporalLink tlink : systemTlinks) {
      if (goldSpans.contains(new TemporalLinkSpan(tlink))) {
        systemTlinksWithGoldSpans.add(tlink);
      }
    }
    statistics.add(goldTlinks, systemTlinksWithGoldSpans, this.annotationConverter);
  }

  private static class TemporalLinkSpan {

    private int sourceBegin;

    private int sourceEnd;

    private int targetBegin;

    private int targetEnd;

    public TemporalLinkSpan(TemporalLink tlink) {
      Anchor source = tlink.getSource();
      Anchor target = tlink.getTarget();
      this.sourceBegin = source.getBegin();
      this.sourceEnd = source.getEnd();
      this.targetBegin = target.getBegin();
      this.targetEnd = target.getEnd();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.sourceBegin, this.sourceEnd, this.targetBegin, this.targetEnd);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || this.getClass() != obj.getClass()) {
        return false;
      }
      TemporalLinkSpan that = (TemporalLinkSpan) obj;
      return this.sourceBegin == that.sourceBegin && this.sourceEnd == that.sourceEnd
          && this.targetBegin == that.targetBegin && this.targetEnd == that.targetEnd;
    }

  }

  private static class TemporalLinkSpanAndRelation {
    public static final Function<TemporalLink, TemporalLinkSpanAndRelation> ANNOTATION_CONVERTER = new Function<TemporalLink, TemporalLinkSpanAndRelation>() {
      @Override
      public TemporalLinkSpanAndRelation apply(TemporalLink tlink) {
        return new TemporalLinkSpanAndRelation(tlink);
      }
    };

    private TemporalLinkSpan span;

    private String relationType;

    public TemporalLinkSpanAndRelation(TemporalLink tlink) {
      this.span = new TemporalLinkSpan(tlink);
      this.relationType = tlink.getRelationType();
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.span, this.relationType);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || this.getClass() != obj.getClass()) {
        return false;
      }
      TemporalLinkSpanAndRelation that = (TemporalLinkSpanAndRelation) obj;
      return Objects.equal(this.span, that.span)
          && Objects.equal(this.relationType, that.relationType);
    }

  }
}
