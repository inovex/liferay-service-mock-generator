package de.inovex.liferay.servicemock;

public abstract class MockService<T> {

	protected T mockObject;
	
	public MockService(){
		initMockObject();
	}

	public T getMockObject() {
		return mockObject;
	}

	public void setMockObject(T mockObject) {
		this.mockObject = mockObject;
	}
	
	public abstract void initMockObject();
}
