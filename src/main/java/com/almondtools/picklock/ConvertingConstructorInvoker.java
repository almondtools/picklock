package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArguments;
import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Invokes a given constructor. Beyond {@link ConstructorInvoker} this class also converts the argument according to its annotation signature
 * @see Convert
 */
public class ConvertingConstructorInvoker extends ConstructorInvoker {

	private Method target;

	public ConvertingConstructorInvoker(Constructor<?> constructor, Method target) {
		super(constructor);
		this.target = target;
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		Object[] convertArguments = convertArguments(target.getParameterTypes(), getConstructor().getParameterTypes(), args);
		Object result = super.invoke(convertArguments);
		return convertResult(target.getReturnType(), getConstructor().getDeclaringClass(), result);
	}

}
