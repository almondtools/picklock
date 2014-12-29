package com.almondarts.picklock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker implements MethodInvocationHandler {

	private Method method;

	public MethodInvoker(Method method) {
		this.method = method;
		method.setAccessible(true);
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		try {
			return method.invoke(object, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}
