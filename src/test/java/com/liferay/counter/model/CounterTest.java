package com.liferay.counter.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portlet.expando.service.ExpandoValueLocalService;
import com.sun.codemodel.JCodeModel;

import de.inovex.liferay.mockgenerator.ClassMatchFilter;
import de.inovex.liferay.mockgenerator.ClassUtils;
import de.inovex.liferay.mockgenerator.LiferayServiceMockGenerator;
import de.inovex.liferay.mockgenerator.MethodClassTuple;
import de.inovex.liferay.mockgenerator.ServiceMockGenerator;
import de.inovex.liferay.mockgenerator.UniqueMethodList;

public class CounterTest {
	
	Logger LOG = LoggerFactory.getLogger(CounterTest.class);

	@Test
	public void testCounterMock(){
		List<MethodClassTuple> methods = ClassUtils.getAllMethods(Counter.class);
		assertNotNull(methods);
		assertTrue("No methods found", methods.size() > 0);
		String methodName = "getModelAttributes";
		assertTrue(methodName + " not found", containsMethod(methodName, methods));
		methodName = "resetOriginalValues";
		assertTrue(methodName + " not found", containsMethod(methodName, methods));
		methodName = "setModelAttributes";
		assertTrue(methodName + " not found", containsMethod(methodName, methods));
		methodName = "getModelClass";
		assertTrue(methodName + " not found", containsMethod(methodName, methods));
		methodName = "getModelClassName";
		assertTrue(methodName + " not found", containsMethod(methodName, methods));
	}
	
	private boolean containsMethod(String methodName, List<MethodClassTuple> methods){
		for(MethodClassTuple methodClassTuple : methods){
			if(methodName.equals(methodClassTuple.getMethod().getName())){
				return true;
			}
		}
		return false;
	}
	
	@Test
	public void testUniqueMethodList(){
		List<MethodClassTuple> methods = ClassUtils.getAllMethods(Counter.class);
		UniqueMethodList uniqueMethodList = new UniqueMethodList();
		for (MethodClassTuple methodClassTuple : methods) {
			boolean aded = uniqueMethodList.add(methodClassTuple);
			if(!aded){
				LOG.info("Method already listed: " + methodClassTuple.getMethod().toGenericString());
			}
		}
		String methodName = "toEscapedModel";
		int methodCount = getMethodCount(methodName, uniqueMethodList.getMethods());
		assertTrue("Dublicate method " + methodName, methodCount == 1);
	}
	
	private int getMethodCount(String methodName, List<MethodClassTuple> methodClassTuples){
		int count = 0;
		for (MethodClassTuple methodClassTuple : methodClassTuples) {
			if(methodName.equals(methodClassTuple.getMethod().getName())){
				count++;
			}
		}
		return count;
	}
	
	@Test
	public void classTest() throws FileNotFoundException{
		
		Class<?> clazz = ExpandoValueLocalService.class;
		List<MethodClassTuple> methods = ClassUtils.getAllMethods(clazz);
		// TODO test ExpandoValueLocalServiceMock
		Type[] interfaces = clazz.getGenericInterfaces();
		assertTrue(methods != null && methods.size() > 0);
		UniqueMethodList uniqueMethodList = new UniqueMethodList();
		uniqueMethodList.addAll(methods);
		methods = uniqueMethodList.getMethods();
		
		JCodeModel codeModel = new JCodeModel();
		Collection<Class<?>> singleClass = new ArrayList<Class<?>>();
		singleClass.add(clazz);
		
		ClassMatchFilter classMatchFilter = new ClassMatchFilter(singleClass);
		AndClassFilter andClassFilter = new AndClassFilter(
				new InterfaceOnlyClassFilter(), classMatchFilter);
		ArrayList<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
		LiferayServiceMockGenerator generator = new LiferayServiceMockGenerator(new File("/home/andy/test/generated"));
		generator.getClassFinder().findClasses(classInfoList, andClassFilter);
		
		ServiceMockGenerator codeGenerator = new ServiceMockGenerator(classInfoList.get(0), codeModel);
		codeGenerator.generateClassAndMethods();
	}
}
