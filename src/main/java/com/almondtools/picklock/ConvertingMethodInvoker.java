package com.almondtools.picklock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given method. Beyond {@link MethodInvoker} this class also converts the argument according to its annotation signature
 * @see Convert
 */
public class ConvertingMethodInvoker implements MethodInvocationHandler {

	private Method method;
	private Method target;

	public ConvertingMethodInvoker(Method method, Method target) {
		this.method = method;
		this.target = target;
		method.setAccessible(true);
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		try {
			Object[] convertArguments = Converter.convertArguments(target.getParameterTypes(), method.getParameterTypes(), args);
			Object result = method.invoke(object, convertArguments);
			return Converter.convertResult(target.getReturnType(), method.getReturnType(), result);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}
