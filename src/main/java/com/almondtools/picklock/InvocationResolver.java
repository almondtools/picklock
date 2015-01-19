package com.almondtools.picklock;

import static com.almondtools.picklock.SignatureUtil.computeFieldNames;
import static com.almondtools.picklock.SignatureUtil.containsConvertable;
import static com.almondtools.picklock.SignatureUtil.fieldSignature;
import static com.almondtools.picklock.SignatureUtil.isBooleanGetter;
import static com.almondtools.picklock.SignatureUtil.isGetter;
import static com.almondtools.picklock.SignatureUtil.isSetter;
import static com.almondtools.picklock.SignatureUtil.matchesSignature;
import static com.almondtools.picklock.SignatureUtil.methodSignature;
import static com.almondtools.picklock.SignatureUtil.propertyOf;

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
		if (isConverted(method)) {
			return new ConvertingFieldSetter(findField(propertyOf(method), method.getParameterTypes()[0], method.getParameterAnnotations()[0]), method.getParameterTypes()[0]);
		} else {
			return new FieldSetter(findField(propertyOf(method), method.getParameterTypes()[0], new Annotation[0]));
		}
	}

	protected MethodInvocationHandler createGetterInvocator(Method method) throws NoSuchFieldException {
		if (isConverted(method)) {
			return new ConvertingFieldGetter(findField(propertyOf(method), method.getReturnType(), method.getAnnotations()), method.getReturnType());
		} else {
			return new FieldGetter(findField(propertyOf(method), method.getReturnType(), new Annotation[0]));
		}
	}

	protected Field findField(String fieldPattern, Class<?> type, Annotation[] annotations) throws NoSuchFieldException {
		String convert = containsConvertable(annotations, type);
		List<String> fieldNames = computeFieldNames(fieldPattern);
		Class<?> currentClass = this.innerClass;
		while (currentClass != Object.class) {
			for (String fieldName : fieldNames) {
				try {
					Field field = currentClass.getDeclaredField(fieldName);
					if (field.getType() == type) {
						return field;
					} else if (field.getType().getSimpleName().equals(convert)) {
						return field;
					} else {
						throw new NoSuchFieldException();
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

	private Method findMatchingMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		Method candidate = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
		if (matchesSignature(method, candidate, null, null)) {
			return candidate;
		}
		throw new NoSuchMethodException();
	}

	private String[] determineNeededConversions(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) {
		String[] convert = new String[parameterAnnotations.length];
		for (int i = 0; i < parameterAnnotations.length; i++) {
			convert[i] = containsConvertable(parameterAnnotations[i], parameterTypes[i]);
		}
		return convert;
	}

}
