package com.almondarts.picklock;

import static com.almondarts.picklock.SignatureUtil.computeFieldNames;
import static com.almondarts.picklock.SignatureUtil.fieldSignature;
import static com.almondarts.picklock.SignatureUtil.methodSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class InvocationResolver {

	private static final String GET = "get";
	private static final String SET = "set";
	
	private Class<?> innerClass;

	public InvocationResolver(Class<?> clazz) {
		this.innerClass = clazz;
	}

	protected MethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException {
		String methodName = method.getName();
		Class<?> resultType = method.getReturnType();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = method.getReturnType();
		try {
			return findMethod(methodName, resultType, parameterTypes, exceptionTypes);
		} catch (NoSuchMethodException e) {
			try {
				if (methodName.length() > 3 && methodName.startsWith(SET) && parameterTypes.length == 1 && exceptionTypes.length == 0) {
					return findSetter(methodName, parameterTypes[0]);
				} else if (methodName.length() > 3 && methodName.startsWith(GET) && parameterTypes.length == 0 && exceptionTypes.length == 0) {
					return findGetter(methodName, returnType);
				} else {
					throw e;
				}
			} catch (NoSuchFieldException e2) {
				throw e;
			}
		}
	}

	protected FieldSetter findSetter(String methodName, Class<?> type) throws NoSuchFieldException {
		String fieldName = methodName.substring(3);
		return new FieldSetter(findField(fieldName, type));
	}

	protected FieldGetter findGetter(String methodName, Class<?> type) throws NoSuchFieldException {
		String fieldName = methodName.substring(3);
		return new FieldGetter(findField(fieldName, type));
	}

	protected Field findField(String fieldPattern, Class<?> type) throws NoSuchFieldException {
		List<String> fieldNames = computeFieldNames(fieldPattern);
		Class<?> currentClass = this.innerClass;
		while (currentClass != Object.class) {
			for (String fieldName : fieldNames) {
				try {
					Field field = currentClass.getDeclaredField(fieldName);
					if (field.getType() != type) {
						throw new NoSuchFieldException();
					} else {
						return field;
					}
				} catch (NoSuchFieldException e) {
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchFieldException(fieldSignature(fieldNames, type));
	}

	protected MethodInvoker findMethod(String methodName, Class<?> resultType, Class<?>[] parameterTypes, Class<?>[] exceptionTypes) throws NoSuchMethodException {
		Class<?> currentClass = this.innerClass;
		while (currentClass != Object.class) {
			try {
				Method method = currentClass.getDeclaredMethod(methodName, parameterTypes);
				if (Arrays.equals(exceptionTypes, method.getExceptionTypes())) {
					return new MethodInvoker(method);
				}
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(methodName, resultType, parameterTypes, exceptionTypes));
	}

}
