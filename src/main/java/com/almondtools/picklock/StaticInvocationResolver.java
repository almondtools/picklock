package com.almondtools.picklock;

import static com.almondtools.picklock.SignatureUtil.fieldSignature;
import static com.almondtools.picklock.SignatureUtil.methodSignature;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class StaticInvocationResolver {

	private static final String GET = "get";
	private static final String SET = "set";
	private static final String CONSTRUCTOR = "create";

	private Class<?> type;

	public StaticInvocationResolver(Class<?> type) {
		this.type = type;
	}

	protected StaticMethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException {
		String methodName = method.getName();
		Class<?> resultType = method.getReturnType();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = method.getReturnType();
		try {
			return findMethod(methodName, resultType, parameterTypes, exceptionTypes);
		} catch (NoSuchMethodException e) {
			try {
				if (methodName.equals(CONSTRUCTOR)) {
					return findConstructor(parameterTypes, exceptionTypes);
				} else if (methodName.length() > 3 && methodName.startsWith(SET) && parameterTypes.length == 1 && exceptionTypes.length == 0) {
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

	protected ConstructorInvoker findConstructor(Class<?>[] parameterTypes, Class<?>[] exceptionTypes) throws NoSuchMethodException {
		for (Constructor<?> constructor : type.getDeclaredConstructors()) {
			if (matches(constructor.getParameterTypes(), parameterTypes)) {
				if (Arrays.equals(exceptionTypes, constructor.getExceptionTypes())) {
					return new ConstructorInvoker(constructor);
				}
			}
		}
		throw new NoSuchMethodException(type.getSimpleName() + Arrays.asList(parameterTypes));
	}

	protected boolean matches(Class<?>[] expected, Class<?>[] found) {
		if (expected.length != found.length) {
			return false;
		}
		for (int i = 0; i < expected.length; i++) {
			if (!expected[i].isAssignableFrom(found[i])) {
				return false;
			}
		}
		return true;
	}

	protected StaticSetter findSetter(String methodName, Class<?> fieldtype) throws NoSuchFieldException {
		String fieldName = methodName.substring(3);
		return new StaticSetter(type, findField(fieldName, fieldtype));
	}

	protected StaticGetter findGetter(String methodName, Class<?> fieldtype) throws NoSuchFieldException {
		String fieldName = methodName.substring(3);
		return new StaticGetter(type, findField(fieldName, fieldtype));
	}

	protected Field findField(String fieldPattern, Class<?> fieldType) throws NoSuchFieldException {
		List<String> fieldNames = SignatureUtil.computeFieldNames(fieldPattern);
		Class<?> nextType = type;
		while (nextType != Object.class) {
			for (String fieldName : fieldNames) {
				try {
					Field field = nextType.getDeclaredField(fieldName);
					if (field.getType() != fieldType) {
						throw new NoSuchFieldException();
					} else {
						return field;
					}
				} catch (NoSuchFieldException e) {
				}
			}
			nextType = nextType.getSuperclass();
		}
		throw new NoSuchFieldException(fieldSignature(fieldNames, type));
	}

	protected StaticMethodInvoker findMethod(String methodName, Class<?> resultType, Class<?>[] parameterTypes, Class<?>[] exceptionTypes) throws NoSuchMethodException {
		Class<?> nextType = type;
		while (nextType != Object.class) {
			try {
				Method method = nextType.getDeclaredMethod(methodName, parameterTypes);
				if (Arrays.equals(exceptionTypes, method.getExceptionTypes())) {
					return new StaticMethodInvoker(nextType, method);
				}
			} catch (NoSuchMethodException e) {
			}
			nextType = nextType.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(methodName, resultType, parameterTypes, exceptionTypes));
	}

}
