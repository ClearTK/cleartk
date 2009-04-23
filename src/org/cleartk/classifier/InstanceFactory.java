package org.cleartk.classifier;

public class InstanceFactory {


	/**
	 * @param <T>
	 * @param outcome the outcome of the returned instance
	 * @param featureData an even number of elements corresponding to name/value pairs used to create features
	 * @return a single instance with the provided outcome and features corresponding to the featureData provided
	 */
	public static <T> Instance<T> createInstance(T outcome, Object...featureData) {
		if(featureData.length % 2 != 0) {
			throw new IllegalArgumentException("feature data must consist of an even number of elements corresponding to name/value pairs used to create features. ");
		}
		Instance<T> instance = new Instance<T>(outcome);
		for(int i=0; i<featureData.length; ) {
			instance.add(new Feature(featureData[i].toString(), featureData[i+1]));
			i+=2;
		}
		return instance;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param outcome the outcome of the returned instance
	 * @param featureData space delimited features.  Here the features only have names (no values) corresponding to the space delimited strings.
	 * @return a single instance with the provided outcome and name-only string features found in the provided featureData
	 */
	public static <T> Instance<T> createInstance(T outcome, String featureData) {
		Instance<T> instance = new Instance<T>(outcome);
		String[] columns = featureData.split(" ");
		for(int i=0; i<columns.length; i++) {
			Feature feature = new Feature();
			feature.setName(columns[i]);
			instance.add(feature);
		}
		return instance;
	}

	
}
