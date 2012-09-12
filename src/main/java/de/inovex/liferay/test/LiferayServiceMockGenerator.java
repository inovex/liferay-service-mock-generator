package de.inovex.liferay.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.service.PersistedModelLocalService;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;

public class LiferayServiceMockGenerator {

	Logger LOG = LoggerFactory.getLogger(LiferayServiceMockGenerator.class);

	public void generateServiceMocks() throws ZipException, IOException,
			JClassAlreadyExistsException, ClassNotFoundException {
		Collection<ClassInfo> serviceDefinitions = findServiceDefinitions();
		for (ClassInfo liferayService : serviceDefinitions) {
			LOG.debug(liferayService.getClassName());
			generateServiceMockImplemetation(liferayService);
		}
	}

	private void generateServiceMockImplemetation(ClassInfo classInfo)
			throws IOException, JClassAlreadyExistsException,
			ClassNotFoundException {
		JCodeModel codeModel = new JCodeModel();
		ServiceMockCodeGenerator serviceMockCodeGenerator = new ServiceMockCodeGenerator(classInfo, codeModel);
		serviceMockCodeGenerator.generateMethods();
		codeModel.build(new File("/home/andy/test/generated"));
	}

	private Collection<ClassInfo> findServiceDefinitions() throws ZipException,
			IOException {
		ArrayList<ClassInfo> serviceDefinitions = new ArrayList<ClassInfo>();
		ClassFinder finder = new ClassFinder();
		for (String liferayPortalImplFile : findLiferayPortalFiles()) {

			LOG.info("Liferay portal Impl file: " + liferayPortalImplFile);
			File classpathFile = new File(liferayPortalImplFile);
			finder.add(classpathFile);
		}

		ClassFilter classFilter = new SubclassClassFilter(
				PersistedModelLocalService.class);

		finder.findClasses(serviceDefinitions, new AndClassFilter(classFilter,
				new InterfaceOnlyClassFilter()));
		
		return serviceDefinitions;
	}
	
	private Collection<ClassInfo> removeClassesWithSuperclasses(Collection<ClassInfo> allClasses){
		Iterator<ClassInfo> it = allClasses.iterator();
		while (it.hasNext()) {
			ClassInfo classInfo = it.next();
			if(!StringUtils.isEmpty(classInfo.getSuperClassName())){
				it.remove();
			}
		}
		return allClasses;
	}

	private Collection<String> findLiferayPortalFiles()
			throws FileNotFoundException {
		StringTokenizer tokenizer = new StringTokenizer(
				System.getProperty("java.class.path"), File.pathSeparator,
				false);
		ArrayList<String> portalFiles = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			String fileName = tokenizer.nextToken();
			if (isPortalFile(fileName)) {
				portalFiles.add(fileName);
			}
		}
		if (!portalFiles.isEmpty()) {
			return portalFiles;
		} else {
			throw new FileNotFoundException("Liferay Portal Impl not found");
		}
	}

	private boolean isPortalFile(String libName) {
		return isPortalImpl(libName) || isPortalService(libName);
	}

	private boolean isPortalService(String libName) {
		return StringUtils.containsIgnoreCase(libName, "portal")
				&& StringUtils.containsIgnoreCase(libName, "service");
	}

	private boolean isPortalImpl(String libName) {
		return StringUtils.containsIgnoreCase(libName, "portal")
				&& StringUtils.containsIgnoreCase(libName, "impl");
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ZipException
	 * @throws JClassAlreadyExistsException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ZipException, IOException,
			JClassAlreadyExistsException, ClassNotFoundException {
		LiferayServiceMockGenerator generator = new LiferayServiceMockGenerator();
		generator.generateServiceMocks();
	}

}
