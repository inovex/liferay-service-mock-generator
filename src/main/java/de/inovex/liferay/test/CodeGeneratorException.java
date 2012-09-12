package de.inovex.liferay.test;

public class CodeGeneratorException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public CodeGeneratorException(){
		
	}
	
	public CodeGeneratorException (Throwable cause){
		super(cause);
	}
	
	public CodeGeneratorException (String message){
		super(message);
	}
	
	public CodeGeneratorException (String message, Throwable cause){
		super(message, cause);
	}
}
