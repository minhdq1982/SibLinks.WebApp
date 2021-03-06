package com.saatya.ws.resource;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.saatya.ws.util.DateUtil;



public class AppConfigManager {

	private static final Logger logger = LoggerFactory.getLogger(AppConfigManager.class);
	
	private static ResourceBundle rb = null;

	public static ResourceBundle getRb() {
		return rb;
	}

	public static void setRb(ResourceBundle rb) {
		AppConfigManager.rb = rb;
		
		
	}

	public static void init(String env) {

		String fileName = null;

		try {

			if (env != null)
				fileName = "appconfig_" + env;
			else
				fileName = "appconfig";

			rb = ResourceBundle.getBundle(fileName);
			
			/*String appConfig = System.getProperty("appConfig");
			String appName = System.getProperty("appName");
			String jvmName = System.getProperty("jvmName");
			
			if(appConfig == null)  appConfig = getConfigVal("appConfig");
			if(appName == null) appName = getConfigVal("appName");
			if(jvmName == null) jvmName = getConfigVal("jvmName");*/
            
          
			
			

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in loading the property File"
					+ e.getMessage());
		}

	}

	public static String getConfigVal(String key) {

		String value = null;
		try {
			if (rb.getString(key) != null)
				value = rb.getString(key).toString();
			else
				value = null;
		} catch (Exception e) {
		}
		return value;
	}
	
	public static String getConfigVal(String key, String defVal) {

		String value = null;
		try {
			if (rb.getString(key) != null)
				value = rb.getString(key).toString();
			else
				value = defVal;
		} catch (Exception e) {
		}
		return value;
	}

	public static long getLongValue(String key, long defVal) {
		try {
			return Long.parseLong(getConfigVal(key));
		} catch (Exception e) {
			return defVal;
		}
	}

	public static int getIntValue(String key, int defVal) {
		try {
			return Integer.parseInt(getConfigVal(key));
		} catch (Exception e) {
			return defVal;
		}
	}

	public static Date getDateValue(String key, Date defValue) {
		return DateUtil.utc2Date(getConfigVal(key), defValue);
	}

	public static void main(String args[]) {

		AppConfigManager.init("stage");
		System.out.println(AppConfigManager.getConfigVal("appName"));

	}
	
	public static Map<String, String> getAllProperties() {
	    Enumeration<String> keys = rb.getKeys();
	    Map<String, String> response = new TreeMap<String, String>();
	    while(keys.hasMoreElements()) {
	        String key = keys.nextElement();
	        response.put(key, rb.getString(key));
	    }
	    return response;
	}

}
