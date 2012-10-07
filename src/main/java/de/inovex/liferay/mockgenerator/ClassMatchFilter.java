package de.inovex.liferay.mockgenerator;

import java.util.Collection;

import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;

public class ClassMatchFilter implements org.clapper.util.classutil.ClassFilter{
	
	private Collection<Class<?>> clazzes;
	
	public ClassMatchFilter(Collection<Class<?>> clazzes){
		this.clazzes = clazzes;
	}

	public boolean accept(ClassInfo classInfo, ClassFinder classFinder) {
		boolean accept = false;
		for(Class<?> clazz : clazzes){
			if(classInfo.getClassName().equals(clazz.getName())){
				accept = true;
				break;
			}
		}
		return accept;
	}

}
