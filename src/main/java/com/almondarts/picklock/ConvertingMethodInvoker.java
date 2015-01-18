package com.almondarts.picklock;

import static com.almondarts.picklock.Converter.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
			Object[] convertArguments = convertArguments(args);
			Object result = method.invoke(object, convertArguments);
			return convertResult(result);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	protected Object[] convertArguments(Object... args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, SecurityException {
		if (args == null) {
			args = new Object[0];
		}
		Object[] converted = new Object[args.length];
		Class<?>[] targetArgumentTypes = target.getParameterTypes();
		Class<?>[] methodArgumentTypes = method.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			if (targetArgumentTypes[i].equals(methodArgumentTypes[i])) {
				converted[i] = args[i];
			} else {
				converted[i] = convert(args[i], methodArgumentTypes[i], targetArgumentTypes[i]);
			}
		}
		return converted;
	}

	protected Object convertResult(Object result) throws NoSuchMethodException {
		Class<?> targetType = target.getReturnType();
		Class<?> methodType = method.getReturnType();
		if (targetType.equals(methodType)) {
			return result;
		} else {
			return ObjectAccess.unlock(result).features(targetType);
		}
	}

}
