package de.inovex.liferay.mockgenerator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UniqueMethodList {

	private List<MethodClassTuple> methodClassTuples = new ArrayList<MethodClassTuple>();
	
	public boolean add(MethodClassTuple methodClassTuple){
		if (isUnique(methodClassTuple)){
			return this.methodClassTuples.add(methodClassTuple);
		} else {
			return false;
		}
		
	}
	
	public void addAll(Collection<MethodClassTuple> methods){
		for(MethodClassTuple methodclassTuple : methods){
			add(methodclassTuple);
		}
	}
	
	private boolean isUnique(MethodClassTuple methodClasTuple){
		boolean unique = true;
		for (MethodClassTuple existingMethodclassTuple : methodClassTuples) {
			if(isSameDeclaredMethod(existingMethodclassTuple, methodClasTuple)){
				unique = false;
				break;
			}
		}
		return unique;
	}
	
	@SuppressWarnings("rawtypes")
	private boolean isSameDeclaredMethod(MethodClassTuple methodClassTuple, MethodClassTuple otherMethodClassTuple){
		Method method = methodClassTuple.getMethod();
		Method other = otherMethodClassTuple.getMethod();
		if (method.getName().equals( other.getName())) {
			if (!method.getReturnType().equals(other.getReturnType())){
				if(!other.getReturnType().isAssignableFrom(method.getReturnType())){
					return false;
				}
			}
			/* Avoid unnecessary cloning */
			Class[] params1 = method.getParameterTypes();
			Class[] params2 = other.getParameterTypes();
			if (params1.length == params2.length) {
			    for (int i = 0; i < params1.length; i++) {
					if (!params2[i].isAssignableFrom(params1[i])) {
					    return false;
				    } else { 
				    	if(methodClassTuple.getMethodSource() == otherMethodClassTuple.getMethodSource()){
				    		if(params2[i] != params1[i]){
				    			return false;
				    		}
				    	}
				    }
				}
			    return true;
			}
		}
		return false;
	}

	public List<MethodClassTuple> getMethods() {
		return methodClassTuples;
	}
	
}
