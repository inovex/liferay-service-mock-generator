package de.inovex.liferay.mockgenerator;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.clapper.util.classutil.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import de.inovex.liferay.servicemock.MockService;

public class ServiceMockGenerator {

	protected ClassInfo classInfo;

	protected JCodeModel codeModel;
	
	protected boolean generateMockClass;

	protected JDefinedClass jDefinedClass;
	
	protected JMethod jMethod;
	
	protected Class<?> implementedInterface;
	
	protected String generatedClassName;

	protected boolean methodsGenerated = false;
	
	protected UniqueClassList uniqueClassList = new UniqueClassList();
	
	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	Logger LOG = LoggerFactory.getLogger(ServiceMockGenerator.class);

	public ServiceMockGenerator(ClassInfo classInfo, JCodeModel codeModel) {
		this(classInfo, codeModel, true);
	}
	
	public ServiceMockGenerator(ClassInfo classInfo, JCodeModel codeModel, boolean generateMockClass) {
		this.classInfo = classInfo;
		this.codeModel = codeModel;
		this.generateMockClass = generateMockClass;
	}

	private void generateClass() {
		if (jDefinedClass == null) {
			try {
				String className = getClassNameToGenerate();
				jDefinedClass = codeModel._class(className);
				this.implementedInterface = Class.forName(classInfo.getClassName());
				jDefinedClass._implements(this.implementedInterface);
				this.generatedClassName = className;
				
				JClass classToExtend = this.codeModel.ref(MockService.class).narrow(this.implementedInterface);
				jDefinedClass._extends(classToExtend);
				
				JMethod initMockObjectMethod = jDefinedClass.method(JMod.PUBLIC, codeModel.VOID, "initMockObject");
				JBlock block = initMockObjectMethod.body();
				
				block.directStatement("this.setMockObject(org.mockito.Mockito.mock(" + this.implementedInterface.getName() + ".class));");
				
				
			} catch (Exception e) {
				throw new CodeGeneratorException(e);
			}
		}
	}
	
	protected String getClassNameToGenerate(){
		return classInfo.getClassName().replace("LocalService", "LocalServiceMock");
	}

	public void generateClassAndMethods() {
		if (!methodsGenerated) {
			generateClass();
			List<MethodClassTuple> methodClassTuples = ClassUtils.getAllMethods(this.implementedInterface);
			this.uniqueClassList.addAll(ClassUtils.getAllUsedClasses(methodClassTuples));
			UniqueMethodList uniqueMethodList = new UniqueMethodList();
			for (MethodClassTuple methodClassTuple : methodClassTuples) {
				boolean aded = uniqueMethodList.add(methodClassTuple);
				if(!aded){
					LOG.debug("Method already listed: " + methodClassTuple.getMethod().toGenericString());
				}
			}
			for(MethodClassTuple methodClassTuple : uniqueMethodList.getMethods()){
				LOG.debug(methodClassTuple.getMethod().toString());
				implementMethod(methodClassTuple);
			}
			methodsGenerated = true;
		}
	}

	private void implementMethod(MethodClassTuple methodClassTuple) {		
		Method _method = methodClassTuple.getMethod();
		Class<?> returnType = _method.getReturnType();
		Type genericReturnType = _method.getGenericReturnType();
		JType jType = null;
		if(genericReturnType instanceof TypeVariable){
			TypeVariable<?> typeVariable = (TypeVariable<?>) genericReturnType;
			if("T".equals(typeVariable.getName())){
				jType = this.codeModel.ref(methodClassTuple.getGenericT());
			}
		} else if(genericReturnType instanceof ParameterizedType){
			ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
			Class<?> c = (Class<?>) parameterizedType.getRawType();
			JClass rawLLclazz = this.codeModel.ref(c);
			List<JClass> generics = convert(parameterizedType.getActualTypeArguments(), rawLLclazz);
			jType = rawLLclazz.narrow(generics); 
		} else if(genericReturnType instanceof GenericArrayType){ 
			GenericArrayType genericArrayType = (GenericArrayType) genericReturnType;
			if(((Class<?>)genericArrayType.getGenericComponentType()).isPrimitive()){
				jType = JPrimitiveType.parse(this.codeModel, genericArrayType.getGenericComponentType().toString()).array();
			} else {
				jType = this.codeModel.ref((Class<?>)genericArrayType.getGenericComponentType()).array();
			}
		} else {
			if(returnType.isPrimitive()){
				jType = JPrimitiveType.parse(this.codeModel, returnType.getSimpleName());
			} else {
				jType = this.codeModel.ref(returnType);
			}
		}
		jMethod = this.jDefinedClass.method(JMod.PUBLIC, jType, _method.getName());
		
		List<JVar> params = addParameter(methodClassTuple);
		addExceptions(_method);
		
		if(!returnType.equals(Void.TYPE)){
			if(isAddServiceObjectMethod(methodClassTuple)){
				implementAddServiceObjectMethod(jMethod, methodClassTuple);
			} else if(isGetServiceObjectMethod(methodClassTuple)){ 
				implementGetServiceObjectMethod(jMethod, methodClassTuple);
			} else {
//				if(returnType.isPrimitive()){
//					jMethod.body()._return(PrimitiveType.valueOf(returnType, this.codeModel).getDefaultReturnValue());
//				} else {
//					jMethod.body()._return(JExpr._null());
//				}
				
				
				JFieldRef jFieldRef = JExpr._this().ref("mockObject");
				JInvocation invocation = jFieldRef.invoke(jMethod);
				for (JVar jVar : params) {
					invocation.arg(jVar);
				}
				jMethod.body()._return(invocation);
			}
		}
	}
	
	private String getServiceObjectName(MethodClassTuple methodClassTuple){
		if(classInfo.getClassName().endsWith("LocalService")){
			String serviceObjectName = classInfo.getClassName().replace("LocalService", "");
			int index = classInfo.getClassName().replace("LocalService", "").lastIndexOf(".");
			return serviceObjectName.substring(index+1);
			
		}
		return null;
	}
	
	private boolean isAddServiceObjectMethod(MethodClassTuple methodClassTuple){
		if(classInfo.getClassName().endsWith("LocalService")){
			boolean isAddMethod = StringUtils.equals("add" + getServiceObjectName(methodClassTuple), methodClassTuple.getMethod().getName());
			if(isAddMethod && methodClassTuple.getMethod().getGenericParameterTypes().length == 1){
				Class<?> parameterClass = methodClassTuple.getMethod().getParameterTypes()[0];
				return parameterClass.getName().startsWith("com.liferay.");
			}
		}
		return false;
	}
	
	private boolean isGetServiceObjectMethod(MethodClassTuple methodClassTuple){
		if(classInfo.getClassName().endsWith("LocalService")){
			boolean isGetMethod = StringUtils.equals("get" + getServiceObjectName(methodClassTuple), methodClassTuple.getMethod().getName());
			boolean isFetchMethod = StringUtils.equals("fetch" + getServiceObjectName(methodClassTuple), methodClassTuple.getMethod().getName());
			boolean isGetOrFetchMethod = isGetMethod || isFetchMethod;
			if(isGetOrFetchMethod && methodClassTuple.getMethod().getGenericParameterTypes().length == 1){
				Class<?> parameterClass = methodClassTuple.getMethod().getParameterTypes()[0];
				return parameterClass.getName().startsWith("com.liferay.") || parameterClass == long.class;
			}
		}
		return false;
	}
	
	private void implementAddServiceObjectMethod(JMethod jMethod, MethodClassTuple methodClassTuple){
		LOG.info("Add method for " + classInfo.getClassName() + " : " + methodClassTuple.getMethod().getName());
		this.codeModel.directClass("java.util.HashMap");
		JClass hashMapClass = this.codeModel.ref("java.util.HashMap");
		List<JClass> generics = new ArrayList<JClass>(2);
		Class<?> serviceObjectClass;
		try {
			serviceObjectClass = Class.forName(jMethod.listParams()[0].type().fullName());
			Method getPrimaryKeyMethod = ReflectionUtils.findMethod(serviceObjectClass, "getPrimaryKey");
			Class<?> primaryKeyClass = getPrimaryKeyMethod.getReturnType();
			if(primaryKeyClass.isPrimitive()){
				primaryKeyClass = PrimitiveType.valueOf(primaryKeyClass, codeModel).getObjectType();
			}
			generics.add(this.codeModel.ref(primaryKeyClass));
			generics.add(this.codeModel.ref(methodClassTuple.getMethod().getParameterTypes()[0]));
			JClass jType = hashMapClass.narrow(generics); 
			
			this.jDefinedClass.field(JMod.PRIVATE, jType, "_serviceObjects", JExpr._new(jType));
			JBlock block = jMethod.body();
			String parameterName = jMethod.listParams()[0].name();
			block.directStatement("_serviceObjects.put(" + parameterName + ".getPrimaryKey(), " + parameterName + ");");
			
			block._return(JExpr.ref(parameterName));
		} catch (ClassNotFoundException e) {
			LOG.error("Error during generation", e);
		}
		
	}
	
	private void implementGetServiceObjectMethod(JMethod jMethod, MethodClassTuple methodClassTuple){
		LOG.info("Add method for " + classInfo.getClassName() + " : " + methodClassTuple.getMethod().getName());
		JBlock block = jMethod.body();
		String parameterName = jMethod.listParams()[0].name();
		block._return(JExpr.direct("_serviceObjects.get(" + parameterName + ")"));
	}

	private List<JVar> addParameter(MethodClassTuple methodClassTuple) {
		int i = 0;
		String[] parameterNames = parameterNameDiscoverer.getParameterNames(methodClassTuple.getMethod()); 
		List<JVar> params = new ArrayList<JVar>();
		for(Type type : methodClassTuple.getMethod().getGenericParameterTypes()){
			Class<?> c = null;
			JType jType = null;
			JVar paramVar;
			if(type instanceof TypeVariable){
				TypeVariable<?> typeVariable = (TypeVariable<?>) type;
				if("T".equals(typeVariable.getName())){
					c = methodClassTuple.getGenericT();
				}
			} else if(type instanceof ParameterizedType){
				ParameterizedType parameterizedType = (ParameterizedType) type;
				c = (Class<?>) parameterizedType.getRawType();
				JClass rawLLclazz = this.codeModel.ref(c);
				List<JClass> generics = convert(parameterizedType.getActualTypeArguments(), rawLLclazz);
				jType = rawLLclazz.narrow(generics); 
			} else if(type instanceof GenericArrayType){ 
				GenericArrayType genericArrayType = (GenericArrayType) type;
				c = (Class<?>) genericArrayType.getGenericComponentType();
				if(((Class<?>)genericArrayType.getGenericComponentType()).isPrimitive()){
					jType = JPrimitiveType.parse(this.codeModel, genericArrayType.getGenericComponentType().toString()).array();
				} else {
					jType = this.codeModel.ref((Class<?>)genericArrayType.getGenericComponentType()).array();
				}
			} else {
				c = (Class<?>) type;
			}
			String parameterName = null;
			if(parameterNames != null){
				parameterName = parameterNames[i];
			} else {
				parameterName = "param" + i;
			}
			if(jType == null){
				paramVar = jMethod.param(c, parameterName);
			} else {
				paramVar = jMethod.param(jType, parameterName);
			}
			params.add(paramVar);
			i++;
		}
		return params;
	}
	
	private List<JClass> convert(Type[] parameterTypes, JClass rawLLclazz){
		List<JClass> generics = new ArrayList<JClass>();
		for(Type genericParameterType : parameterTypes){
			if(genericParameterType instanceof GenericArrayType){
				GenericArrayType genericArrayType = (GenericArrayType) genericParameterType;
				JClass genericArray = null;
				if(((Class<?>)genericArrayType.getGenericComponentType()).isPrimitive()){
					genericArray = JPrimitiveType.parse(this.codeModel, genericArrayType.getGenericComponentType().toString()).array();
				} else {
					genericArray = this.codeModel.ref((Class<?>)genericArrayType.getGenericComponentType()).array();
				}
				generics.add(genericArray);
			} else if(genericParameterType instanceof WildcardType){
				WildcardType wildcardType = (WildcardType) genericParameterType;
				
				if(wildcardType.getUpperBounds().length == 1){
					String name = ((Class<?>) wildcardType.getUpperBounds()[0]).getName();
				}
				
				if (!(wildcardType.getLowerBounds().length == 0
						&& wildcardType.getUpperBounds().length == 1
						&& (((Class<?>) wildcardType.getUpperBounds()[0]) == Object.class))){
//					generics.add(rawLLclazz.wildcard());
					generics.add(this.codeModel.ref(((Class<?>) wildcardType.getUpperBounds()[0]).getName()).wildcard());
				} else {
					generics.add(this.codeModel.ref(Object.class).wildcard());
				}
			} else if(genericParameterType instanceof ParameterizedType){
				ParameterizedType parameterizedType2 = (ParameterizedType) genericParameterType;
				JClass jClass = this.codeModel.ref((Class<?>)parameterizedType2.getRawType());
				generics.add(jClass.narrow(convert(parameterizedType2.getActualTypeArguments(), jClass)));						
			} else {
				generics.add(this.codeModel.ref((Class<?>)genericParameterType));
			}
		}
		return generics;
	}
	
	@SuppressWarnings("unchecked")
	private void addExceptions(Method _method){
		for(Class<?> c : _method.getExceptionTypes()){
			jMethod._throws((Class<? extends Throwable>)c);
		}
	}

	public String getGeneratedClassName() {
		return generatedClassName;
	}
	
	public String getImplementedInterfaceClassName() {
		return this.classInfo.getClassName();
	}
	
	public List<Class<?>> getClassesUsedAsParameterOrReturnValue(){
		return this.uniqueClassList.getClasses();
	}

}
