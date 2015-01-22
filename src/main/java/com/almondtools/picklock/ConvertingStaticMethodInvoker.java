package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArguments;
import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given static method. Beyond {@link StaticMethodInvoker} this class also converts the argument according to its annotation signature
 * @see Convert
 */
public class ConvertingStaticMethodInvoker implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Method method;
	private Method target;

	public ConvertingStaticMethodInvoker(Class<?> type, Method method, Method target) {
		this.type = type;
		this.method = method;
		this.target = target;
		method.setAccessible(true);
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		try {
			Object[] convertArguments = convertArguments(target.getParameterTypes(), method.getParameterTypes(), args);
			Object result = method.invoke(type, convertArguments);
			return convertResult(target.getReturnType(), method.getReturnType(), result);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}
