package com.almondarts.picklock;

import java.util.ArrayList;
import java.util.List;

public final class SignatureUtil {
	private SignatureUtil() {
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
		return type.getSimpleName() + ' ' + fieldNames;
	}

	public static String methodSignature(String methodName, Class<?>[] parameterTypes, Class<?>[] exceptionTypes) {
		return "? " + methodName + parameters(parameterTypes) + " throws " + exceptions(exceptionTypes);
	}

	private static String parameters(Class<?>[] parameterTypes) {
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		if (parameterTypes.length > 0) {
			buffer.append(parameterTypes[0].getSimpleName());
		}
		for (int i = 1; i < parameterTypes.length; i++) {
			buffer.append(',').append(parameterTypes[i].getSimpleName());
		}
		buffer.append(')');
		return buffer.toString();
	}

	private static String exceptions(Class<?>[] exceptionTypes) {
		StringBuilder buffer = new StringBuilder();
		if (exceptionTypes.length > 0) {
			buffer.append(exceptionTypes[0].getSimpleName());
		}
		for (int i = 1; i < exceptionTypes.length; i++) {
			buffer.append(',').append(exceptionTypes[i].getSimpleName());
		}
		return buffer.toString();
	}

}
