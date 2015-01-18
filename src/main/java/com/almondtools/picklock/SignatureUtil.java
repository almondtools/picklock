package com.almondtools.picklock;

import static com.almondtools.picklock.BoxingUtil.getUnboxed;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SignatureUtil {

	private static final String IS = "is";
	private static final String GET = "get";
	private static final String SET = "set";
	
	private SignatureUtil() {
	}

	public static boolean isBooleanGetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = getUnboxed(method.getReturnType());
		return name.length() > 2 
			&& name.startsWith(IS) 
			&& parameterTypes.length ==0 
			&& exceptionTypes.length == 0 
			&& returnType == boolean.class;
	}

	public static boolean isGetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = getUnboxed(method.getReturnType());
		return name.length() > 3 
			&& name.startsWith(GET) 
			&& parameterTypes.length == 0 
			&& exceptionTypes.length == 0 
			&& returnType != void.class;
	}

	public static boolean isSetter(Method method) {
		String name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = getUnboxed(method.getReturnType());
		return name.length() > 3 
			&& name.startsWith(SET) 
			&& parameterTypes.length == 1 
			&& exceptionTypes.length == 0
			&& returnType == void.class;
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

	public static List<String> computeFieldNames(String fieldPattern) {
		List<String> names = new ArrayList<String>(2);
		if (fieldPattern.toUpperCase().equals(fieldPattern)) {
			names.add(fieldPattern);
			names.add(Character.toLowerCase(fieldPattern.charAt(0)) + fieldPattern.substring(1));
		} else {
			names.add(Character.toLowerCase(fieldPattern.charAt(0)) + fieldPattern.substring(1));
			names.add(fieldPattern);
		}
		return names;
	}

	public static String fieldSignature(List<String> fieldNames, Class<?> type) {
		StringBuilder buffer = new StringBuilder()
		.append(typeName(type))
		.append(' ');
		Iterator<String> iterator = fieldNames.iterator();
		if (iterator.hasNext()) {
			buffer.append(iterator.next());
		}
		while(iterator.hasNext()) {
			buffer.append('|');
			buffer.append(iterator.next());
		}
		return buffer.toString();
	}

	public static String methodSignature(String methodName, Class<?> resultType, Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
		return typeName(resultType) + ' ' + methodName + parameters(parameterTypes) + throwsClause(exceptionTypes);
	}

	private static String throwsClause(Class<?>[] exceptionTypes) {
		if (exceptionTypes.length == 0) {
			return "";
		}
		return " throws " + exceptions(exceptionTypes);
	}

	private static String parameters(Class<?>[] parameterTypes) {
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		if (parameterTypes.length > 0) {
			buffer.append(typeName(parameterTypes[0]));
		}
		for (int i = 1; i < parameterTypes.length; i++) {
			buffer.append(',').append(typeName(parameterTypes[i]));
		}
		buffer.append(')');
		return buffer.toString();
	}

	private static String exceptions(Class<?>[] exceptionTypes) {
		StringBuilder buffer = new StringBuilder();
		if (exceptionTypes.length > 0) {
			buffer.append(typeName(exceptionTypes[0]));
		}
		for (int i = 1; i < exceptionTypes.length; i++) {
			buffer.append(',').append(typeName(exceptionTypes[i]));
		}
		return buffer.toString();
	}

	private static String typeName(Class<?> clazz) {
		return clazz.getSimpleName();
	}
}
