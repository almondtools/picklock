package com.almondarts.picklock;

import static com.almondarts.picklock.SignatureUtil.computeFieldNames;
import static com.almondarts.picklock.SignatureUtil.fieldSignature;
import static com.almondarts.picklock.SignatureUtil.methodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.almondarts.picklock.examples.innerclass.AutoPicklock;

public class InvocationResolver {

	private static final String IS = "is";
	private static final String GET = "get";
	private static final String SET = "set";
	
	private Class<?> innerClass;

	public InvocationResolver(Class<?> clazz) {
		this.innerClass = clazz;
	}

	protected MethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Class<?> returnType = method.getReturnType();
		try {
			return findMethod(method);
		} catch (NoSuchMethodException e) {
			try {
				if (methodName.length() > 3 && methodName.startsWith(SET) && parameterTypes.length == 1 && exceptionTypes.length == 0) {
					return findSetter(methodName, parameterTypes[0]);
				} else if (methodName.length() > 3 && methodName.startsWith(GET) && parameterTypes.length == 0 && exceptionTypes.length == 0) {
					return findGetter(methodName, returnType);
				} else if (methodName.length() > 2 && methodName.startsWith(IS) && parameterTypes.length == 0 && exceptionTypes.length == 0 && (returnType == Boolean.class || returnType == boolean.class)) {
					return findIs(methodName, returnType);
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

	protected FieldGetter findIs(String methodName, Class<?> type) throws NoSuchFieldException {
		String fieldName = methodName.substring(2);
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

	protected MethodInvoker findMethod(Method method) throws NoSuchMethodException {
		Class<?> currentClass = this.innerClass;
		while (currentClass != Object.class) {
			try {
				if (isAutoPicklocked(method)) {
					Method candidate = findPicklockedMethod(currentClass, method.getName(), method.getParameterTypes(), method.getParameterAnnotations());
					if (Arrays.equals(method.getExceptionTypes(), candidate.getExceptionTypes())) {
						return new MethodInvoker(candidate);
					}
				} else {
					Method candidate = currentClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
					if (Arrays.equals(method.getExceptionTypes(), candidate.getExceptionTypes())) {
						return new MethodInvoker(candidate);
					}
				}
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(method.getName(), method.getReturnType(), method.getParameterTypes(), method.getExceptionTypes()));
	}

	private boolean isAutoPicklocked(Method method) {
		if (method.getAnnotation(AutoPicklock.class) != null) {
			return true;
		}
		for (Annotation[] annotations : method.getParameterAnnotations()) {
			if (containsAutopicklocked(annotations)) {
				return true;
			}
		}
		return false;
	}

	private Method findPicklockedMethod(Class<?> currentClass, String name, Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) throws NoSuchMethodException {
		boolean[] convert = determineParamsToConvert(parameterAnnotations);
		for (Method candidate : currentClass.getDeclaredMethods()) {
			if (matchesSignature(candidate, name, parameterTypes, convert)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException();
	}

	private boolean matchesSignature(Method candidate, String name, Class<?>[] parameterTypes, boolean[] convert) {
		if (!candidate.getName().equals(name)) {
			return false;
		}
		Class<?>[] candidateParameters = candidate.getParameterTypes();
		for (int i = 0; i < candidateParameters.length; i++) {
			Class<?> candidateParameter = candidateParameters[i];
			Class<?> requiredParameter = parameterTypes[i];
			if (candidateParameter.equals(requiredParameter))  {
				continue;
			}
			if (convert[i] && candidateParameter.getSimpleName().equals(requiredParameter.getSimpleName())) {
				continue;
			}
		}
		return true;
	}

	private boolean[] determineParamsToConvert(Annotation[][] parameterAnnotations) {
		boolean[] convert = new boolean[parameterAnnotations.length];
		for (int i = 0; i < parameterAnnotations.length; i++) {
			convert[i] = containsAutopicklocked(parameterAnnotations[i]);
		}
		return convert;
	}

	private boolean containsAutopicklocked(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == AutoPicklock.class) {
				return true;
			}
		}
		return false;
	}

}
