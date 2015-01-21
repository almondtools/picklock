package com.almondtools.picklock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StaticMethodInvoker implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Method method;

	public StaticMethodInvoker(Class<?> type, Method method) {
		this.type = type;
		this.method = method;
		method.setAccessible(true);
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		try {
			return method.invoke(type, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}