package de.inovex.liferay.mockgenerator;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
	
//	private final static Logger LOG = LoggerFactory.getLogger(ClassUtils.class);

	public static List<MethodClassTuple> getAllMethods(Class<?> clazz) {
		ArrayList<MethodClassTuple> allMethods = new ArrayList<MethodClassTuple>();
		getDeclaredMethods(clazz, allMethods);
		getInterfaceMethods(clazz.getGenericInterfaces(), allMethods);
		
		Class<?> parent = clazz.getSuperclass();
		while (parent != null) {
			getDeclaredMethods(parent, allMethods);
			parent = parent.getSuperclass();
		}
		return allMethods;
	}
	
	private static void getInterfaceMethods(Type[] interfaces, List<MethodClassTuple> methods){
		for(Type type : interfaces){
			Class<?> clazz = null;
			getDeclaredMethods(type, methods);
			if(type instanceof ParameterizedType){
				clazz = (Class<?>)((ParameterizedType) type).getRawType();
			} else {
				clazz = (Class<?>) type;
			}
			if(clazz.getInterfaces().length > 0){
				getInterfaceMethods(clazz.getGenericInterfaces(), methods);
			}
		}
	}

	public static List<MethodClassTuple> getDeclaredMethods(Class<?> clazz,
			List<MethodClassTuple> methodlist) {
		Method[] declaredMethods = clazz.getDeclaredMethods();
		if (declaredMethods != null && declaredMethods.length > 0) {
			for (Method method : declaredMethods) {
				methodlist.add(new MethodClassTuple(method, clazz));				
			}
		}
		return methodlist;
	}
	
	public static List<MethodClassTuple> getDeclaredMethods(Type type, List<MethodClassTuple> methodlist) {
		Class<?> genericT = null;
		Class<?> clazz = null;
		if(type instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType) type;
			if( parameterizedType.getActualTypeArguments()[0] instanceof Class<?>){
				genericT = (Class<?>) parameterizedType.getActualTypeArguments()[0];
			}
			
			clazz = (Class<?>) parameterizedType.getRawType();
			
		} else {
			clazz = (Class<?>) type;
		}
		Method[] declaredMethods = clazz.getDeclaredMethods();
		if (declaredMethods != null && declaredMethods.length > 0) {
			for (Method method : declaredMethods) {
				methodlist.add(new MethodClassTuple(method, clazz, genericT));
			}
		}
		
		return methodlist;
	}
	
	public static List<Class<?>> getAllUsedClasses(List<MethodClassTuple> methodClassTuples){
		UniqueClassList uniqueClassList = new UniqueClassList();
		for(MethodClassTuple methodClassTuple : methodClassTuples){
			Type returnType = methodClassTuple.getMethod().getGenericReturnType();

			if(returnType instanceof ParameterizedType){
			    ParameterizedType type = (ParameterizedType) returnType;
			    Type[] typeArguments = type.getActualTypeArguments();
			    for(Type typeArgument : typeArguments){
			    	if(typeArgument instanceof Class){
				        Class<?> typeArgClass = (Class<?>) typeArgument;
				        if(typeArgClass.isInterface()){
				        	uniqueClassList.add(typeArgClass);
				        }
			    	}
			    }
			}
			
			Type[] genericParameterTypes = methodClassTuple.getMethod().getGenericParameterTypes();

			for(Type genericParameterType : genericParameterTypes){
			    if(genericParameterType instanceof ParameterizedType){
			        ParameterizedType aType = (ParameterizedType) genericParameterType;
			        Type[] parameterArgTypes = aType.getActualTypeArguments();
			        for(Type parameterArgType : parameterArgTypes){
			        	if(parameterArgType instanceof Class){
			        		Class<?> parameterArgClass = (Class<?>) parameterArgType;
			        		if(parameterArgClass.isInterface()){
			        			uniqueClassList.add(parameterArgClass);
			        		}
			        	}
			        }
			    }
			}
		}
		return uniqueClassList.getClasses();
	}

}
