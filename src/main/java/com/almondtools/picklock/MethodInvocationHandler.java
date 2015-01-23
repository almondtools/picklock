package com.almondtools.picklock;


public interface MethodInvocationHandler {

	MethodInvocationHandler NULL = new MethodInvocationHandler() {
		
		@Override
		public Object invoke(Object object, Object... args) throws Throwable {
			return null;
		}
	};

	Object invoke(Object object, Object... args) throws Throwable;
}
