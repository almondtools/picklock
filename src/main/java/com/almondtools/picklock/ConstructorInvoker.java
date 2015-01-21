package com.almondtools.picklock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Invokes a given constructor.
 */
public class ConstructorInvoker implements StaticMethodInvocationHandler {

	private Constructor<?> constructor;

	public ConstructorInvoker(Constructor<?> constructor) {
		this.constructor = constructor;
		constructor.setAccessible(true);
	}

	public ConstructorInvoker(Class<?> clazz) throws NoSuchMethodException {
		this(defaultConstructor(clazz));
	}

	private static Constructor<?> defaultConstructor(Class<?> clazz) throws NoSuchMethodException {
		return clazz.getDeclaredConstructor(new Class[0]);
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		try {
			return constructor.newInstance(args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}
