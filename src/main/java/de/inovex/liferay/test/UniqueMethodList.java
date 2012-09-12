package de.inovex.liferay.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class UniqueMethodList {

	private List<Method> methods = new ArrayList<Method>();
	
	public boolean add(Method method){
		if (isUnique(method)){
			return this.methods.add(method);
		} else {
			return false;
		}
		
	}
	
	private boolean isUnique(Method method){
		boolean unique = true;
		for (Method existingMethod : methods) {
			if(isSameDeclaredMethod(existingMethod, method)){
				unique = false;
				break;
			}
		}
		return unique;
	}
	
	@SuppressWarnings("rawtypes")
	private boolean isSameDeclaredMethod(Method method, Method other){
		if (method.getName().equals( other.getName())) {
			if (!method.getReturnType().equals(other.getReturnType()))
			    return false;
			/* Avoid unnecessary cloning */
			Class[] params1 = method.getParameterTypes();
			Class[] params2 = other.getParameterTypes();
			if (params1.length == params2.length) {
			    for (int i = 0; i < params1.length; i++) {
				if (params1[i] != params2[i])
				    return false;
			    }
			    return true;
			}
		}
		return false;
	}

	public List<Method> getMethods() {
		return methods;
	}
	
}
