package com.almondarts.picklock;

import static com.almondarts.picklock.SignatureUtil.computeFieldNames;
import static com.almondarts.picklock.SignatureUtil.fieldSignature;
import static com.almondarts.picklock.SignatureUtil.isBooleanGetter;
import static com.almondarts.picklock.SignatureUtil.isGetter;
import static com.almondarts.picklock.SignatureUtil.isSetter;
import static com.almondarts.picklock.SignatureUtil.methodSignature;
import static com.almondarts.picklock.SignatureUtil.propertyOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

	protected MethodInvocationHandler createSetterInvocator(Method method) throws NoSuchFieldException {
		return new FieldSetter(findField(propertyOf(method), method.getParameterTypes()[0]));
	}

	protected MethodInvocationHandler createGetterInvocator(Method method) throws NoSuchFieldException {
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
				if (isConverted(method)) {
					Method candidate = findConvertableMethod(method, currentClass);
					return new ConvertingMethodInvoker(candidate, method);
				} else {
					Method candidate = findMatchingMethod(method, currentClass);
					return new MethodInvoker(candidate);
				}
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(method.getName(), method.getReturnType(), method.getParameterTypes(), method.getExceptionTypes()));
	}

	private boolean isConverted(Method method) {
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

	private Method findConvertableMethod(Method method, Class<?> currentClass) throws NoSuchMethodException {
		String[] conversionVector = determineNeededConversions(method.getParameterAnnotations(), method.getParameterTypes());
		for (Method candidate : currentClass.getDeclaredMethods()) {
			String containsConvertable = containsConvertable(method.getAnnotations(), method.getReturnType());
			if (matchesSignature(method, candidate, conversionVector, containsConvertable)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException();
	}

	private Method findMatchingMethod(Method method, Class<?> currentClass) throws NoSuchMethodException {
		Method candidate = currentClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
		if (matchesSignature(candidate, method, null, null)) {
			return candidate;
		}
		throw new NoSuchMethodException();
	}

	private boolean matchesSignature(Method method, Method candidate, String[] convertArguments, String convertResult) {
		if (!candidate.getName().equals(method.getName())) {
			return false;
		}
		return isCompliant(method.getParameterTypes(), candidate.getParameterTypes(), convertArguments)
			&& isCompliant(method.getReturnType(), candidate.getReturnType(), convertResult)
			&& isCompliant(method.getExceptionTypes(), candidate.getExceptionTypes(), null);
	}

	private boolean isCompliant(Class<?>[] requiredTypes, Class<?>[] candidateTypes, String[] annotatedNames) {
		if (candidateTypes.length != requiredTypes.length) {
			return false;
		}
		if (annotatedNames == null) {
			annotatedNames = new String[requiredTypes.length];
		}
		for (int i = 0; i < candidateTypes.length; i++) {
			Class<?> candidateType = candidateTypes[i];
			Class<?> requiredType = requiredTypes[i];
			if (!isCompliant(requiredType, candidateType, annotatedNames[i])) {
				return false;
			}
		}
		return true;
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
