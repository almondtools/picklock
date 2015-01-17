package com.almondarts.picklock;

import static com.almondarts.picklock.MethodClassification.isBooleanGetter;
import static com.almondarts.picklock.MethodClassification.isGetter;
import static com.almondarts.picklock.MethodClassification.isSetter;
import static com.almondarts.picklock.MethodClassification.propertyOf;
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
			return findMethod(method);
		} catch (NoSuchMethodException e) {
			try {
				if (isSetter(method)) {
					return findSetter(propertyOf(method), method.getParameterTypes()[0]);
				} else if (isGetter(method)) {
					return findGetter(propertyOf(method), method.getReturnType());
				} else if (isBooleanGetter(method)) {
					return findIs(propertyOf(method), method.getReturnType());
				} else {
					throw e;
				}
			} catch (NoSuchFieldException e2) {
				throw e;
			}
		}
	}

	protected FieldSetter findSetter(String fieldName, Class<?> type) throws NoSuchFieldException {
		return new FieldSetter(findField(fieldName, type));
	}

	protected FieldGetter findGetter(String fieldName, Class<?> type) throws NoSuchFieldException {
		return new FieldGetter(findField(fieldName, type));
	}

	protected FieldGetter findIs(String fieldName, Class<?> type) throws NoSuchFieldException {
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

	protected MethodInvocationHandler findMethod(Method method) throws NoSuchMethodException {
		Class<?> currentClass = this.innerClass;
		while (currentClass != Object.class) {
			try {
				if (isAutoPicklocked(method)) {
					String defaultName = containsAutopicklocked(method.getAnnotations(), method.getReturnType());
					Method candidate = findPicklockedMethod(currentClass, method.getName(), method.getReturnType(), defaultName, method.getParameterTypes(), method.getParameterAnnotations());
					if (Arrays.equals(method.getExceptionTypes(), candidate.getExceptionTypes())) {
						return new ConvertingMethodInvoker(candidate, method);
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
		if (method.getAnnotation(Convert.class) != null) {
			return true;
		}
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			Annotation[] annotations = parameterAnnotations[i];
			if (containsAutopicklocked(annotations, parameterType) != null) {
				return true;
			}
		}
		return false;
	}

	private Method findPicklockedMethod(Class<?> currentClass, String name, Class<?> resultType, String annotatedName, Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) throws NoSuchMethodException {
		String[] convert = determineParamsToConvert(parameterAnnotations, parameterTypes);
		for (Method candidate : currentClass.getDeclaredMethods()) {
			if (matchesSignature(candidate, name, resultType, annotatedName, parameterTypes, convert)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException();
	}

	private boolean matchesSignature(Method candidate, String name, Class<?> resultType, String annotatedName, Class<?>[] parameterTypes, String[] convert) {
		if (!candidate.getName().equals(name)) {
			return false;
		}
		Class<?>[] candidateParameters = candidate.getParameterTypes();
		for (int i = 0; i < candidateParameters.length; i++) {
			Class<?> candidateType = candidateParameters[i];
			Class<?> requiredType = parameterTypes[i];
			if (isCompliant(requiredType, candidateType, convert[i])) {
				continue;
			}
			return false;
		}
		return isCompliant(resultType, candidate.getReturnType(), annotatedName) ;
	}

	private boolean isCompliant(Class<?> requiredType, Class<?> candidateType, String annotatedName) {
		return candidateType.equals(requiredType) || candidateType.getSimpleName().equals(annotatedName);
	}

	private String[] determineParamsToConvert(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) {
		String[] convert = new String[parameterAnnotations.length];
		for (int i = 0; i < parameterAnnotations.length; i++) {
			convert[i] = containsAutopicklocked(parameterAnnotations[i], parameterTypes[i]);
		}
		return convert;
	}

	private String containsAutopicklocked(Annotation[] annotations, Class<?> defaultType) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == Convert.class) {
				Convert autoPicklock = (Convert) annotation;
				String name = autoPicklock.value();
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
