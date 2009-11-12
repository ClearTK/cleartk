package org.cleartk.test.util;

import org.uutuc.factory.ConfigurationParameterFactory;

public class ConfigurationParameterNameFactory {

	 public static String createConfigurationParameterName(Class<?> clazz, String fieldName) throws RuntimeException {
		 try {
			 return ConfigurationParameterFactory.getConfigurationParameterName(
					clazz.getDeclaredField(fieldName));
		 } catch(Exception e) {
			 throw new RuntimeException(e);
		 }
	 }
}
