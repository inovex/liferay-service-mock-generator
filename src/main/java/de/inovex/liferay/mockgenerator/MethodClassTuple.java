package de.inovex.liferay.mockgenerator;

import java.lang.reflect.Method;

public class MethodClassTuple {

	private Method method;

	private Class<?> methodSource;
	
	private Class<?> genericT;

	public MethodClassTuple(Method method, Class<?> methodSource) {
		this(method, methodSource, null);
	}
	
	public MethodClassTuple(Method method, Class<?> methodSource, Class<?> genericT) {
		this.method = method;
		this.methodSource = methodSource;
		this.genericT = genericT;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Class<?> getMethodSource() {
		return methodSource;
	}

	public void setMethodSource(Class<?> methodSource) {
		this.methodSource = methodSource;
	}

	public Class<?> getGenericT() {
		return genericT;
	}

	public void setGenericT(Class<?> genericT) {
		this.genericT = genericT;
	}
	
}
