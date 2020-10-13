/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */

package org.cleartk.ml.weka;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Majid Laali
 */
public class WekaNominalFeatureEncoderTest {

  @Test
  public void whenValueEqualsWithTheNotSeanConstantThenValueIsUpdated(){
    WekaNominalFeatureEncoder wekaNominalFeatureEncoder = new WekaNominalFeatureEncoder("test", true);
    wekaNominalFeatureEncoder.save(WekaNominalFeatureEncoder.NOT_SEAN_VAL);
    assertThat(wekaNominalFeatureEncoder.getSortedValues()).containsOnly(WekaNominalFeatureEncoder.NOT_SEAN_VAL
        , wekaNominalFeatureEncoder.updateIfEqualWithNotSeenConstant(WekaNominalFeatureEncoder.NOT_SEAN_VAL));
  }
  
  @Test
  public void whenValueContainsSpaceThenValueIsNotQuoted(){
    WekaNominalFeatureEncoder wekaNominalFeatureEncoder = new WekaNominalFeatureEncoder("test", false);
    String val = "a b ";
    wekaNominalFeatureEncoder.save(val);
    assertThat(wekaNominalFeatureEncoder.getSortedValues()).containsOnly(val);
  }

}
