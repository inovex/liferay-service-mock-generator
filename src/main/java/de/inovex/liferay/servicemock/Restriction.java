package de.inovex.liferay.servicemock;

public class Restriction {
	
	private Object[] parameter;

	public Restriction(){
		
	}
	
	public Restriction(Object... parameter){
		this.parameter = parameter;
	}

	public Object[] getParameter() {
		return parameter;
	}
	
}
