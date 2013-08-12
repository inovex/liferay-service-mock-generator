package de.inovex.liferay.mockgenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import com.liferay.portal.util.Portal;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;

public class LiferayServiceMockGenerator {

	Logger LOG = LoggerFactory.getLogger(LiferayServiceMockGenerator.class);

	private File target;

	private Properties servicesToMock = new Properties();

	private UniqueClassList parameterAndReturnValues = new UniqueClassList("com.liferay");

	private ClassFinder classFinder = null;

	public LiferayServiceMockGenerator(File targetFolder) {
		target = targetFolder;
	}

	public void generate() throws ZipException, IOException,
			JClassAlreadyExistsException, ClassNotFoundException {
		this.generateServiceMocks();
		this.generateMockObjects();
		this.createMavenProject();
		this.writeServiceProperties();
	}

	private void generateServiceMocks() throws ZipException, IOException,
			JClassAlreadyExistsException, ClassNotFoundException {
		Collection<ClassInfo> serviceDefinitions = findServiceDefinitions();
		for (ClassInfo liferayService : serviceDefinitions) {
			LOG.debug(liferayService.getClassName());
			this.inspectServiceForMockObjects(liferayService);
//			this.servicesToMock.setProperty(liferayService.getClassName(),
//					liferayService.getClassName());
		}
	}

	private void generateMockObjects() throws ClassNotFoundException,
			IOException, JClassAlreadyExistsException {
		List<ClassInfo> classInfos = getClassInfoFor(parameterAndReturnValues.getClasses());
		for (ClassInfo classInfo : classInfos) {
			generateMockObjects(classInfo);
		}
	}

	private void createMavenProject() throws IOException {
		InputStream stream = this.getClass().getResourceAsStream("/pom.xml");
		IOUtils.copy(stream,
				FileUtils.openOutputStream(new File(target, "pom.xml")));
	}

	private void writeServiceProperties() throws IOException {
		File propertyTarget = new File(target,
				"src/main/resources/service-mock.properties");
		propertyTarget.getParentFile().mkdirs();
		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				propertyTarget));
		this.servicesToMock.store(out,
				"Interfaces and their mock implementations");
		out.flush();
		out.close();
	}

	private void inspectServiceForMockObjects(ClassInfo classInfo) throws IOException,
			JClassAlreadyExistsException, ClassNotFoundException {
		JCodeModel codeModel = new JCodeModel();
		ServiceMockGenerator codeGenerator = new ServiceMockGenerator(
				classInfo, codeModel);
		codeGenerator.generateClassAndMethods();
		this.parameterAndReturnValues.addAll(codeGenerator
				.getClassesUsedAsParameterOrReturnValue());
		this.parameterAndReturnValues.add(Portal.class);
		this.servicesToMock.setProperty(
				codeGenerator.getImplementedInterfaceClassName(),
				codeGenerator.getGeneratedClassName());
		build(codeModel);
	}

	private void generateMockObjects(ClassInfo classInfo) throws IOException,
			JClassAlreadyExistsException, ClassNotFoundException {
		JCodeModel codeModel = new JCodeModel();
		MockObjectGenerator codeGenerator = new MockObjectGenerator(classInfo,
				codeModel);
		codeGenerator.generateClassAndMethods();
		build(codeModel);
	}

	private void build(JCodeModel codeModel) throws IOException {
		File javaSourceFolder = new File(target, "src/main/java");
		if (!javaSourceFolder.exists()) {
			javaSourceFolder.mkdirs();
		}
		codeModel.build(javaSourceFolder);
	}

	private Collection<ClassInfo> findServiceDefinitions() throws ZipException,
			IOException {
		ArrayList<ClassInfo> serviceDefinitions = new ArrayList<ClassInfo>();

		ClassFilter classFilter = new SubclassClassFilter(
				PersistedModelLocalService.class);
		getClassFinder()
				.findClasses(
						serviceDefinitions,
						new AndClassFilter(classFilter,
								new InterfaceOnlyClassFilter()));

		return serviceDefinitions;
	}

	private List<ClassInfo> getClassInfoFor(Collection<Class<?>> clazzes)
			throws FileNotFoundException, ClassNotFoundException {
		ClassMatchFilter classMatchFilter = new ClassMatchFilter(clazzes);
		AndClassFilter andClassFilter = new AndClassFilter(
				new InterfaceOnlyClassFilter(), classMatchFilter);
		ArrayList<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
		getClassFinder().findClasses(classInfoList, andClassFilter);
		return classInfoList;
	}

	public ClassFinder getClassFinder() throws FileNotFoundException {
		if (this.classFinder == null) {
			classFinder = new ClassFinder();
			for (String liferayPortalImplFile : findLiferayPortalFiles()) {

				LOG.info("Liferay portal Impl file: " + liferayPortalImplFile);
				File classpathFile = new File(liferayPortalImplFile);
				classFinder.add(classpathFile);
			}
		}
		return classFinder;
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

	private static boolean isTargetValid(String targetFolder) {
		File targetDirectory = new File(targetFolder);
		if (targetDirectory.exists()) {
			if (targetDirectory.isDirectory()) {
				if (targetDirectory.canWrite()) {
					return true;
				} else {
					System.out.println("Can not write to " + targetFolder);
					return false;
				}
			} else {
				System.out.println(targetFolder + " is not a folder");
				return false;
			}
		} else {
			System.out.println("Folder: " + targetFolder + " does not exist");
			return false;
		}
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
		if (args != null && args.length == 1) {
			String targetFolder = args[0];
			if (isTargetValid(targetFolder)) {
				LiferayServiceMockGenerator generator = new LiferayServiceMockGenerator(
						new File(targetFolder));
				generator.generate();
			}
		} else {
			System.out.println("Target parameter missing");
		}
	}
}
