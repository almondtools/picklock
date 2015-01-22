package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArguments;
import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given constructor. Beyond {@link ConstructorInvoker} this class also converts the argument according to its annotation signature
 * @see Convert
 */
public class ConvertingConstructorInvoker implements StaticMethodInvocationHandler {

	private Constructor<?> constructor;
	private Method target;

	public ConvertingConstructorInvoker(Constructor<?> constructor, Method target) {
		this.constructor = constructor;
		this.target = target;
		constructor.setAccessible(true);
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		try {
			Object[] convertArguments = convertArguments(target.getParameterTypes(), constructor.getParameterTypes(), args);
			Object result = constructor.newInstance(convertArguments);
			return convertResult(target.getReturnType(), constructor.getDeclaringClass(), result);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}
