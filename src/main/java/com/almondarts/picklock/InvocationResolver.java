package com.almondarts.picklock;

import static com.almondarts.picklock.SignatureUtil.isBooleanGetter;
import static com.almondarts.picklock.SignatureUtil.isGetter;
import static com.almondarts.picklock.SignatureUtil.isSetter;
import static com.almondarts.picklock.SignatureUtil.propertyOf;
import static com.almondarts.picklock.SignatureUtil.computeFieldNames;
import static com.almondarts.picklock.SignatureUtil.fieldSignature;
import static com.almondarts.picklock.SignatureUtil.methodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class InvocationResolver {

	private Class<?> innerClass;

	public InvocationResolver(Class<?> clazz) {
		this.innerClass = clazz;
	}

	protected MethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException {
		try {
			return createMethodInvocator(method);
		} catch (NoSuchMethodException e) {
			try {
				if (isSetter(method)) {
					return createSetterInvocator(method);
				} else if (isGetter(method) || isBooleanGetter(method)) {
					return createGetterInvocator(method);
				} else {
					throw e;
				}
			} catch (NoSuchFieldException e2) {
				throw e;
			}
		}
	}

	private MethodInvocationHandler createSetterInvocator(Method method) throws NoSuchFieldException {
		return new FieldSetter(findField(propertyOf(method), method.getParameterTypes()[0]));
	}

	private MethodInvocationHandler createGetterInvocator(Method method) throws NoSuchFieldException {
		return new FieldGetter(findField(propertyOf(method), method.getReturnType()));
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

	protected MethodInvocationHandler createMethodInvocator(Method method) throws NoSuchMethodException {
		Class<?> currentClass = this.innerClass;
		while (currentClass != Object.class) {
			try {
				if (isAutoPicklocked(method)) {
					String defaultName = containsConvertable(method.getAnnotations(), method.getReturnType());
					Method candidate = findConvertableMethod(currentClass, method.getName(), method.getReturnType(), defaultName, method.getParameterTypes(), method.getParameterAnnotations());
					if (Arrays.equals(method.getExceptionTypes(), candidate.getExceptionTypes())) {
						return new ConvertingMethodInvoker(candidate, method);
					}
				} else {
					Method candidate = findMatchingMethod(method, currentClass);
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
		if (method.getAnnotation(Convert.class) != null) {
			return true;
		}
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			Annotation[] annotations = parameterAnnotations[i];
			if (containsConvertable(annotations, parameterType) != null) {
				return true;
			}
		}
		return false;
	}

	private Method findMatchingMethod(Method method, Class<?> currentClass) throws NoSuchMethodException {
		return currentClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
	}

	private Method findConvertableMethod(Class<?> currentClass, String name, Class<?> resultType, String convertResult, Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) throws NoSuchMethodException {
		String[] conversionVector = determineNeededConversions(parameterAnnotations, parameterTypes);
		for (Method candidate : currentClass.getDeclaredMethods()) {
			if (matchesSignature(candidate, name, resultType, convertResult, parameterTypes, conversionVector)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException();
	}

	private boolean matchesSignature(Method candidate, String name, Class<?> resultType, String convertResult, Class<?>[] parameterTypes, String[] convertArguments) {
		if (!candidate.getName().equals(name)) {
			return false;
		}
		Class<?>[] candidateParameters = candidate.getParameterTypes();
		if (candidateParameters.length != parameterTypes.length) {
			return false;
		}
		for (int i = 0; i < candidateParameters.length; i++) {
			Class<?> candidateType = candidateParameters[i];
			Class<?> requiredType = parameterTypes[i];
			if (!isCompliant(requiredType, candidateType, convertArguments[i])) {
				return false;
			}
		}
		return isCompliant(resultType, candidate.getReturnType(), convertResult) ;
	}

	private boolean isCompliant(Class<?> requiredType, Class<?> candidateType, String annotatedName) {
		return candidateType.equals(requiredType) || candidateType.getSimpleName().equals(annotatedName);
	}

	private String[] determineNeededConversions(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) {
		String[] convert = new String[parameterAnnotations.length];
		for (int i = 0; i < parameterAnnotations.length; i++) {
			convert[i] = containsConvertable(parameterAnnotations[i], parameterTypes[i]);
		}
		return convert;
	}

	private String containsConvertable(Annotation[] annotations, Class<?> defaultType) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == Convert.class) {
				Convert convertable = (Convert) annotation;
				String name = convertable.value();
				if (name.isEmpty()) {
					return defaultType.getSimpleName();
				} else {
					return name;
				}
			}
		}
		return null;
	}

}
