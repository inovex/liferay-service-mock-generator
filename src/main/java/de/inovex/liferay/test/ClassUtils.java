package de.inovex.liferay.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {

	@SuppressWarnings("rawtypes")
	public static List<Method> getAllMethods(Class clazz) {
		ArrayList<Method> allMethods = new ArrayList<Method>();
		getDeclaredMethods(clazz, allMethods);
		for(Class interfaceClass : clazz.getInterfaces()){
			getDeclaredMethods(interfaceClass, allMethods);
		}
		
		Class parent = clazz.getSuperclass();
		while (parent != null) {
			getDeclaredMethods(parent, allMethods);
			parent = parent.getSuperclass();
		}
		return allMethods;
	}

	@SuppressWarnings("rawtypes")
	public static List<Method> getDeclaredMethods(Class clazz,
			List<Method> methodlist) {
		Method[] declaredMethods = clazz.getDeclaredMethods();
		if (declaredMethods != null && declaredMethods.length > 0) {
			for (Method method : declaredMethods) {
				methodlist.add(method);
			}
		}
		return methodlist;
	}

}
