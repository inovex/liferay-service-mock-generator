package de.inovex.liferay.mockgenerator;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

import org.clapper.util.classutil.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;

public class ServiceMockGenerator {

	protected ClassInfo classInfo;

	protected JCodeModel codeModel;

	protected JDefinedClass jDefinedClass;
	
	protected JMethod jMethod;
	
	protected Class<?> implementedInterface;
	
	protected String generatedClassName;

	protected boolean methodsGenerated = false;
	
	protected UniqueClassList uniqueClassList = new UniqueClassList();

	Logger LOG = LoggerFactory.getLogger(ServiceMockGenerator.class);

	public ServiceMockGenerator(ClassInfo classInfo, JCodeModel codeModel) {
		this.classInfo = classInfo;
		this.codeModel = codeModel;
	}

	private void generateClass() {
		if (jDefinedClass == null) {
			try {
				String className = getClassNameToGenerate();
				jDefinedClass = codeModel._class(className);
				this.implementedInterface = Class.forName(classInfo.getClassName());
				jDefinedClass._implements(this.implementedInterface);
				this.generatedClassName = className;
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
		jMethod = this.jDefinedClass.method(JMod.PUBLIC, returnType, _method.getName());
		if(!returnType.equals(Void.TYPE)){
			if(returnType.isPrimitive()){
				jMethod.body()._return(PrimitiveType.valueOf(returnType, this.codeModel).getDefaultReturnValue());
			} else {
				jMethod.body()._return(JExpr._null());
			}
		}
		addParameter(methodClassTuple);
		addExceptions(_method);
	}

	private void addParameter(MethodClassTuple methodClassTuple) {
		int i = 0;
		for(Type type : methodClassTuple.getMethod().getGenericParameterTypes()){
			Class<?> c = null;
			JType jType = null;
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
			if(jType == null){
				jMethod.param(c, "param" + i);
			} else {
				jMethod.param(jType, "param" + i);
			}
			i++;
		}
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
				if (!(wildcardType.getLowerBounds().length == 0
						&& wildcardType.getUpperBounds().length == 1
						&& (((Class<?>) wildcardType.getUpperBounds()[0]) == Object.class))){
					generics.add(rawLLclazz.wildcard());
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
