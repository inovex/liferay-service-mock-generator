package de.inovex.liferay.servicemock;

import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;


public class MockedServiceInitializer {
	
	public static void setMockedService(Object liferayLocalServiceUtil, Object mockedService){
		Field serviceField = ReflectionUtils.findField(liferayLocalServiceUtil.getClass(), "_service");
		ReflectionUtils.makeAccessible(serviceField);
		ReflectionUtils.setField(serviceField, liferayLocalServiceUtil, mockedService);
	}
}
