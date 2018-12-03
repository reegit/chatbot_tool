package com.ibm.watson.developer_cloud.conversation.v1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropertyValues {
	String result = "";
	InputStream inputStream;
 
	public String getPropValues(String key) throws IOException {
 
		try {
			Properties prop = new Properties();
			String propFileName = "./config.properties";
 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
 
			// get the property value and print it out
			String value = prop.getProperty(key);

			result = value;
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	}
	
	
}
