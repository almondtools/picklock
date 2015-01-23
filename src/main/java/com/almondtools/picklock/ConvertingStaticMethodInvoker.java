package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArguments;
import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.Method;

/**
 * Invokes a given static method. Beyond {@link StaticMethodInvoker} this class also converts the argument according to its annotation signature
 * 
 * @see Convert
 */
public class ConvertingStaticMethodInvoker extends StaticMethodInvoker {

	private Method target;

	public ConvertingStaticMethodInvoker(Class<?> type, Method method, Method target) {
		super(type, method);
		this.target = target;
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		Object[] convertArguments = convertArguments(target.getParameterTypes(), getMethod().getParameterTypes(), args);
		Object result = super.invoke(convertArguments);
		return convertResult(target.getReturnType(), getMethod().getReturnType(), result);
	}

}
