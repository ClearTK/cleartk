package org.cleartk.ml.weka;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import weka.core.Utils;

public class WekaNominalFeatureEncoderTest {

  @Test
  public void whenValueEqualsWithTheNotSeanConstantThenValueIsUpdated(){
    WekaNominalFeatureEncoder wekaNominalFeatureEncoder = new WekaNominalFeatureEncoder("test", true);
    wekaNominalFeatureEncoder.save(WekaNominalFeatureEncoder.NOT_SEAN_VAL);
    assertThat(wekaNominalFeatureEncoder.getSortedValues()).containsOnly(WekaNominalFeatureEncoder.NOT_SEAN_VAL
        , wekaNominalFeatureEncoder.updateIfEqualWithNotSeenConstant(WekaNominalFeatureEncoder.NOT_SEAN_VAL));
  }
  
  @Test
  public void whenValueContainsSpaceThenValueIsQuoted(){
    WekaNominalFeatureEncoder wekaNominalFeatureEncoder = new WekaNominalFeatureEncoder("test", false);
    String val = "a b ";
    wekaNominalFeatureEncoder.save(val);
    assertThat(wekaNominalFeatureEncoder.getSortedValues()).containsOnly(Utils.quote(val));
  }

}
