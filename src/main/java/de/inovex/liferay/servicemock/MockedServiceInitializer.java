package de.inovex.liferay.servicemock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ReflectionUtils;


public class MockedServiceInitializer {
	
	private final static Logger LOG = LoggerFactory.getLogger(MockedServiceInitializer.class);
	
	public static void setMockedService(Object liferayLocalServiceUtil, Object mockedService){
		Field serviceField = ReflectionUtils.findField(liferayLocalServiceUtil.getClass(), "_service");
		ReflectionUtils.makeAccessible(serviceField);
		ReflectionUtils.setField(serviceField, liferayLocalServiceUtil, mockedService);
	}
	
	public static void initAllMockedServices() throws IOException, InstantiationException, IllegalAccessException{
		Properties serviceProperties = PropertiesLoaderUtils.loadAllProperties("service.properties");
		Enumeration<Object> keys = serviceProperties.keys();
		while (keys.hasMoreElements()) {
			Object object = keys.nextElement();
			LOG.debug(object.toString());
			try {
				Class<?> utilClass = findLiferayUtilClass(object.toString());
				setMockedService(utilClass.newInstance(), serviceProperties.get(Class.forName(serviceProperties.getProperty(object.toString())).newInstance()));
			} catch (ClassNotFoundException e) {
				LOG.error("Util class not found for: " + object);
			}
		}
	}
	
	private static Class<?> findLiferayUtilClass(String serviceInterface) throws ClassNotFoundException{
		Class<?> utilClass = Class.forName(serviceInterface + "Util");
		return utilClass;
	}
}
