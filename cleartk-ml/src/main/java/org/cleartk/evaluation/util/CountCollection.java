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

package org.cleartk.evaluation.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class CountCollection<T> implements Serializable {

  private static final long serialVersionUID = 416048470395125772L;

  Map<T, CountObject<T>> counts;

  /** Creates a new instance of CountCollection */
  public CountCollection() {
    counts = new HashMap<T, CountObject<T>>();
  }

  public CountCollection(Collection<T> collection) {
    this();
    addAll(collection);
  }

  public Set<T> getCountedObjects() {
    return Collections.unmodifiableSet(counts.keySet());
  }

  public boolean addAll(Collection<? extends T> collection) {
    for (T object : collection) {
      add(object);
    }
    return true;
  }

  public boolean add(T object) {
    if (counts.containsKey(object)) {
      counts.get(object).increment();
    } else {
      counts.put(object, new CountObject<T>(object, 1));
    }
    return true;
  }

  public boolean add(T object, int countIncrement) {
    if (counts.containsKey(object)) {
      counts.get(object).increment(countIncrement);
    } else {
      counts.put(object, new CountObject<T>(object, countIncrement));
    }
    return true;
  }

  public Collection<CountObject<T>> getCounts() {
    return Collections.unmodifiableCollection(counts.values());
  }

  public int size() {
    return counts.size();
  }

  public int getCount(T object) {
    if (counts.containsKey(object)) {
      return counts.get(object).getCount();
    } else {
      return 0;
    }
  }

  public long total() {
    long total = 0;
    for (CountObject<T> count : counts.values()) {
      total += count.getCount();
    }
    return total;
  }

  /**
   * Example, you see the following integers with the following counts:
   * <ul>
   * <li>1 - 5 times</li>
   * <li>2 - 7 times</li>
   * <li>3 - 2 times</li>
   * </ul>
   * 
   * The "weighted" average = 1*5 + 2*7 + 3*2 / (5+7+2) = 25 / 14 = 1.78571
   * 
   * @param countObjects
   * @return
   */
  public static double weightedAverage(CountCollection<? extends Number> countCollection) {
    double numerator = 0;
    double denominator = 0;

    for (CountObject<? extends Number> countObject : countCollection.counts.values()) {
      double value = countObject.getObject().doubleValue();
      int count = countObject.getCount();
      denominator += count;
      numerator += value * count;
    }
    return (double) numerator / (double) denominator;
  }

  public List<CountObject<T>> sortCounts(boolean sortAscending, boolean sortTiesAscending) {
    List<CountObject<T>> returnValues = new ArrayList<CountObject<T>>(counts.values());
    Collections.sort(returnValues, new CountComparator<T>(sortAscending, sortTiesAscending));
    return returnValues;
  }

  public List<CountObject<T>> sortObjects(boolean sortAscending) {
    List<CountObject<T>> returnValues = new ArrayList<CountObject<T>>(counts.values());

    Collections.sort(returnValues, new ObjectComparator<T>(sortAscending));
    return returnValues;
  }

  public static class CountComparator<COTYPE> implements Comparator<CountObject<COTYPE>> {
    private final boolean sortAscending;

    private final boolean sortTiesAscending;

    public CountComparator(boolean sortAscending, boolean sortTiesAscending) {
      this.sortAscending = sortAscending;
      this.sortTiesAscending = sortTiesAscending;
    }

    public int compare(CountObject<COTYPE> co1, CountObject<COTYPE> co2) {
      int comparison = compareCounts(co1, co2);
      if (comparison == 0) {
        if (sortTiesAscending)
          return compareObjects(co1, co2);
        else
          return compareObjects(co2, co1);
      }
      if (sortAscending)
        return comparison;
      else {
        return -comparison;
      }
    }
  }

  public static class ObjectComparator<COTYPE> implements Comparator<CountObject<COTYPE>> {
    private final boolean sortAscending;

    public ObjectComparator(boolean sortAscending) {
      this.sortAscending = sortAscending;
    }

    public int compare(CountObject<COTYPE> co1, CountObject<COTYPE> co2) {
      int comparison = compareObjects(co1, co2);
      if (sortAscending)
        return comparison;
      else {
        return -comparison;
      }
    }
  }

  private static int compareCounts(CountObject<?> co1, CountObject<?> co2) {
    if (co1.getCount() < co2.getCount())
      return -1;
    else if (co1.getCount() > co2.getCount())
      return 1;
    else
      return 0;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static int compareObjects(CountObject<?> co1, CountObject<?> co2) {
    Object o1 = co1.getObject();
    Object o2 = co2.getObject();
    if (o1 instanceof Comparable) {
      return ((Comparable) o1).compareTo(o2);
    }
    return 0;
  }

}
