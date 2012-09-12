package de.inovex.liferay.test;

import java.lang.reflect.Method;
import java.util.List;

import org.clapper.util.classutil.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

public class ServiceMockCodeGenerator {

	private ClassInfo classInfo;

	private JCodeModel codeModel;

	private JDefinedClass jDefinedClass;
	
	private JMethod jMethod;
	
	private Class<?> clazz;

	private boolean methodsGenerated = false;

	Logger LOG = LoggerFactory.getLogger(ServiceMockCodeGenerator.class);

	public ServiceMockCodeGenerator(ClassInfo classInfo, JCodeModel codeModel) {
		this.classInfo = classInfo;
		this.codeModel = codeModel;
	}

	public Class<?> getClassToImplement() {
		if (clazz == null) {
			try {
				jDefinedClass = codeModel._class(classInfo.getClassName()
						.replace("LocalService", "LocalServiceMock"));
				this.clazz = Class.forName(classInfo.getClassName());
				jDefinedClass._implements(this.clazz);
			} catch (Exception e) {
				throw new CodeGeneratorException(e);
			}
		}
		return clazz;
	}

	public void generateMethods() {
		if (!methodsGenerated) {
			this.clazz = getClassToImplement();
			List<Method> methods = ClassUtils.getAllMethods(this.clazz);
			UniqueMethodList uniqueMethodList = new UniqueMethodList();
			for (Method method : methods) {
				boolean aded = uniqueMethodList.add(method);
				if(!aded){
					LOG.info("Method already listed: " + method.toGenericString());
				}
			}
			for(Method method : uniqueMethodList.getMethods()){
				LOG.debug(method.toString());
				implementMethod(method);
			}
			methodsGenerated = true;
		}
	}

	private void implementMethod(Method _method) {		
		Class<?> returnType = _method.getReturnType();
		jMethod = this.jDefinedClass.method(JMod.PUBLIC, returnType, _method.getName());
		if(!returnType.equals(Void.TYPE)){
			if(returnType.isPrimitive()){
				jMethod.body()._return(PrimitiveType.valueOf(returnType, this.codeModel).getDefaultReturnValue());
			} else {
				jMethod.body()._return(JExpr._null());
			}
		}
		addParameter(_method);
		addExceptions(_method);
	}

	private void addParameter(Method _method) {
		int i = 0;
		for(Class<?> c : _method.getParameterTypes()){
			jMethod.param(c, "param" + i);
			i++;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addExceptions(Method _method){
		for(Class<?> c : _method.getExceptionTypes()){
			jMethod._throws((Class<? extends Throwable>)c);
		}
	}

}
