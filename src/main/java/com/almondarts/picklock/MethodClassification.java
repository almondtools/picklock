package com.almondarts.picklock;

import java.lang.reflect.Method;

public class MethodClassification {

	private static final String IS = "is";
	private static final String GET = "get";
	private static final String SET = "set";

	public static boolean isBooleanGetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = method.getReturnType();
		return name.length() > 2 && name.startsWith(IS) && parameterTypes.length == 0 && exceptionTypes.length == 0 && (returnType == Boolean.class || returnType == boolean.class);
	}

	public static boolean isGetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		return name.length() > 3 && name.startsWith(GET) && parameterTypes.length == 0 && exceptionTypes.length == 0;
	}

	public static boolean isSetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		return name.length() > 3 && name.startsWith(SET) && parameterTypes.length == 1 && exceptionTypes.length == 0;
	}

	public static String propertyOf(Method method) {
		String name = method.getName();
		if (isSetter(method) || isGetter(method)) {
			return name.substring(3);
		} else if (isBooleanGetter(method)) {
			return name.substring(2);
		} else {
			return name;
		}
	}

}
