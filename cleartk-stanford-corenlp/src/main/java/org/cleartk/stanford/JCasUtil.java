/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-stanford-corenlp project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.stanford;

import java.util.Iterator;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class JCasUtil extends org.uimafit.util.JCasUtil {

  public static <T extends FeatureStructure> Iterator<T> iterator(
      final JCas jCas,
      final Class<T> type) {
    final Type casType = JCasUtil.getType(jCas, type);
    return new Iterator<T>() {
      private FSIterator<?> iter = jCas.getIndexRepository().getAllIndexedFS(casType);

      @Override
      public boolean hasNext() {
        return this.iter.hasNext();
      }

      @Override
      public T next() {
        return type.cast(this.iter.next());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <T extends FeatureStructure> Iterable<T> iterate(
      final JCas jCas,
      final Class<T> type) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return JCasUtil.iterator(jCas, type);
      }
    };
  }

  public static <T extends FeatureStructure> Iterator<T> iterator(
      final FSArray fsArray,
      final Class<T> type) {
    return new Iterator<T>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return this.index < fsArray.size();
      }

      @Override
      public T next() {
        T next = type.cast(fsArray.get(this.index));
        this.index += 1;
        return next;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <T extends FeatureStructure> Iterable<T> iterate(
      final FSArray fsArray,
      final Class<T> type) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return JCasUtil.iterator(fsArray, type);
      }
    };
  }
}
