package com.almondarts.picklock.examples.innerclass;

@SuppressWarnings("unused")
public class ExampleObject {

	private String outerState;
	
	public ExampleObject()  {
	}
	
	private InnerStatic createInnerStatic()  {
		return new InnerStatic(outerState);
	}

	private boolean useInnerStatic(InnerStatic arg)  {
		return arg.state != null;
	}

	private Inner createInner()  {
		return new Inner();
	}

	private static class InnerStatic {
		private String state;

		public InnerStatic(String state) {
			this.state = state;
		}
		
	}
	
	private class Inner {
		
		private String state;

		public Inner() {
			this.state = outerState;
		}
		
	}
}
