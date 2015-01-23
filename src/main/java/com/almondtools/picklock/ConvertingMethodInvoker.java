package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArguments;
import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given method. Beyond {@link MethodInvoker} this class also converts the argument according to its annotation signature
 * @see Convert
 */
public class ConvertingMethodInvoker extends MethodInvoker {

	private Method target;

	public ConvertingMethodInvoker(Method method, Method target) {
		super(method);
		this.target = target;
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		try {
			Object[] convertArguments = convertArguments(target.getParameterTypes(), getMethod().getParameterTypes(), args);
			Object result = super.invoke(object, convertArguments);
			return convertResult(target.getReturnType(), getMethod().getReturnType(), result);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

}
