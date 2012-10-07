package de.inovex.liferay.mockgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class UniqueClassList {
	
	public UniqueClassList(){
		
	}
	
	public UniqueClassList(String packageNameRestriction){
		this.packageName = packageNameRestriction;
	}

	private List<Class<?>> classes = new ArrayList<Class<?>>();
	
	private String packageName;
	
	public boolean add(Class<?> clazz){
		if (isUnique(clazz) && acceptPackage(clazz)){
			return this.classes.add(clazz);
		} else {
			return false;
		}		
	}
	
	public void addAll(Collection<Class<?>> classes){
		for (Class<?> clazz : classes) {
			this.add(clazz);
		}
	}
	
	private boolean isUnique(Class<?> clazz){
		boolean unique = true;
		for (Class<?> listedClass : classes) {
			if(listedClass.getName().equals(clazz.getName())){
				unique = false;
				break;
			}
		}
		return unique;
	}
	
	private boolean acceptPackage(Class<?> clazz){
		boolean accept = true;
		if(StringUtils.isNotEmpty(packageName)){
			accept = clazz.getName().startsWith(packageName);
		}
		return accept;
	}

	public List<Class<?>> getClasses() {
		return classes;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
}
