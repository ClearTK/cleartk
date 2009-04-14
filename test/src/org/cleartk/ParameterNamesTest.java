package org.cleartk;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cleartk.util.io.Files;
import org.junit.Assert;
import org.junit.Test;

public class ParameterNamesTest {

	
	@Test
	public void testParameters() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		List<String> badParameters = new ArrayList<String>();
		Iterable<File> files = Files.getFiles("src", new String[] { ".java" });
		
		for (File file : files) {
			String className = file.getPath();
			className = className.substring(4);
			className = className.substring(0, className.length()-5);
			className = className.replace(File.separatorChar, '.');
			
			Class<?> cls = Class.forName(className);
			Field[] fields = cls.getDeclaredFields();
			for(Field field : fields) {
				String fieldName = field.getName();
				if(fieldName.indexOf("PARAM") != -1) {
					String expectedValue = className+"."+field.getName();
					String actualValue = (String) field.get(cls); 
					if(!expectedValue.equals(actualValue)) {
						badParameters.add("'"+actualValue+"' should be '"+expectedValue+"'");
					}
				}
			}
		}
		
		if (badParameters.size() > 0) {
			String message = String.format("%d descriptor parameters with bad names. ", badParameters.size());
			System.err.println(message);
			Collections.sort(badParameters);
			for (String badParameter : badParameters) {
				System.err.println(badParameter);
			}
			Assert.fail(message);
		}

	}
}
