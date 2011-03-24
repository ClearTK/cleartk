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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntTuple;
import edu.stanford.nlp.util.Pair;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class CorefGraph {

  private HashMultimap<IntTuple, IntTuple> sourceTargetsMap;

  private Map<IntTuple, Set<IntTuple>> mentionEntityMap;

  private Set<Set<IntTuple>> entitySet;

  public CorefGraph(List<Pair<IntTuple, IntTuple>> sourceTargetPairs) {

    // convert list into map (mention1 -> mention2)
    this.sourceTargetsMap = HashMultimap.create();
    for (Pair<IntTuple, IntTuple> sourceTargetPair : sourceTargetPairs) {
      if (sourceTargetPair.first.length() != 2) {
        throw new RuntimeException("Expected 2 element tuple, found " + sourceTargetPair.first);
      }
      if (sourceTargetPair.second.length() != 2) {
        throw new RuntimeException("Expected 2 element tuple, found " + sourceTargetPair.second);
      }
      this.sourceTargetsMap.put(sourceTargetPair.first, sourceTargetPair.second);
    }

    // initialize mention-entity map with all mentions observed
    this.mentionEntityMap = new HashMap<IntTuple, Set<IntTuple>>();
    for (Pair<IntTuple, IntTuple> sourceTargetPair : sourceTargetPairs) {
      this.mentionEntityMap.put(sourceTargetPair.first, null);
      this.mentionEntityMap.put(sourceTargetPair.second, null);
    }

    // fill in the mention-entity map
    this.entitySet = new HashSet<Set<IntTuple>>();
    for (IntTuple mention : this.mentionEntityMap.keySet()) {
      this.getOrCreateEntity(mention);
    }
  }

  public Set<CoreMap> getMentions(CoreMap documentMap) {
    // convert all keys in the mention -> entity map into CoreMaps
    Set<CoreMap> mentions = new HashSet<CoreMap>();
    for (IntTuple sentToken : this.mentionEntityMap.keySet()) {
      mentions.add(this.getMention(documentMap, sentToken));
    }
    return mentions;
  }

  public Map<CoreMap, Set<CoreMap>> getMentionEntityMap(CoreMap documentMap) {
    // for each entity, map its mentions to the entity object
    Map<CoreMap, Set<CoreMap>> result = new HashMap<CoreMap, Set<CoreMap>>();
    for (Set<CoreMap> entity : this.getEntities(documentMap)) {
      for (CoreMap mention : entity) {
        result.put(mention, entity);
      }
    }
    return result;
  }

  public Set<Set<CoreMap>> getEntities(CoreMap documentMap) {
    // convert sets of mention indexes into sets of mention objects (token objects)
    Set<Set<CoreMap>> entities = new HashSet<Set<CoreMap>>();
    for (Set<IntTuple> intEntity : this.entitySet) {

      // convert mention indexes into mention objects (token objects)
      Set<CoreMap> entity = new HashSet<CoreMap>();
      for (IntTuple sentToken : intEntity) {
        entity.add(this.getMention(documentMap, sentToken));
      }
      entities.add(entity);
    }
    return entities;
  }

  public String toString() {
    return String.format("%s(%s)", this.getClass().getSimpleName(), this.sourceTargetsMap);
  }

  private Set<IntTuple> getOrCreateEntity(IntTuple mention) {
    // first see if there is already an entity for this mention in the map
    Set<IntTuple> entity = this.mentionEntityMap.get(mention);
    if (entity == null) {

      // if there are no out-links from this mention, create a new entity for it
      Set<IntTuple> targetMentions = this.sourceTargetsMap.get(mention);
      if (targetMentions.isEmpty()) {
        entity = new HashSet<IntTuple>();
        this.entitySet.add(entity);
      }

      // if there are out-links for this mention, get the entity from one of them
      else {
        for (IntTuple targetMention : targetMentions) {
          entity = this.getOrCreateEntity(targetMention);
          if (entity != null) {
            break;
          }
        }
      }
    }

    // this should never happen if the code above is correct
    if (entity == null) {
      throw new RuntimeException("no entity found for mention " + mention);
    }

    // add the mention to the entity, update the map and return the entity
    entity.add(mention);
    this.mentionEntityMap.put(mention, entity);
    return entity;
  }

  private CoreMap getMention(CoreMap documentMap, IntTuple sentToken) {
    // convert from indices to a token (guaranteed to be of length 2 by constructor above)
    int[] sentTokenIndices = sentToken.elems();
    CoreMap sentMap = documentMap.get(SentencesAnnotation.class).get(sentTokenIndices[0] - 1);
    return sentMap.get(TokensAnnotation.class).get(sentTokenIndices[1] - 1);
  }
}
