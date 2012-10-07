package de.inovex.liferay.mockgenerator;

import org.clapper.util.classutil.ClassInfo;

import com.sun.codemodel.JCodeModel;

public class MockObjectGenerator extends ServiceMockGenerator {

	public MockObjectGenerator(ClassInfo classInfo, JCodeModel codeModel) {
		super(classInfo, codeModel);
	}

	@Override
	protected String getClassNameToGenerate() {
		return classInfo.getClassName() + "Mock";
	}
}
