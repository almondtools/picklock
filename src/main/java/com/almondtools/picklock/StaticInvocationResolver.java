package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.determineNeededConversions;
import static com.almondtools.picklock.Converter.isConverted;
import static com.almondtools.picklock.SignatureUtil.findTargetTypeName;
import static com.almondtools.picklock.SignatureUtil.fieldSignature;
import static com.almondtools.picklock.SignatureUtil.isBooleanGetter;
import static com.almondtools.picklock.SignatureUtil.isConstructor;
import static com.almondtools.picklock.SignatureUtil.isGetter;
import static com.almondtools.picklock.SignatureUtil.isSetter;
import static com.almondtools.picklock.SignatureUtil.matchesSignature;
import static com.almondtools.picklock.SignatureUtil.methodSignature;
import static com.almondtools.picklock.SignatureUtil.propertyAnnotationsOf;
import static com.almondtools.picklock.SignatureUtil.propertyOf;
import static com.almondtools.picklock.SignatureUtil.propertyTypeOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class StaticInvocationResolver {

	private Class<?> type;

	public StaticInvocationResolver(Class<?> type) {
		this.type = type;
	}

	protected StaticMethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException {
		try {
			return createMethodInvocator(method);
		} catch (NoSuchMethodException e) {
			try {
				if (isConstructor(method)) {
					return createConstructorInvocator(method);
				} else if (isSetter(method)) {
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

	protected StaticMethodInvocationHandler createConstructorInvocator(Method method) throws NoSuchMethodException {
		return new ConstructorInvoker(findConstructor(method, type), findConversionTarget(method));
	}

	private Constructor<?> findConstructor(Method method, Class<?> clazz) throws NoSuchMethodException {
		if (isConverted(method)) {
			return findConvertableConstructor(method, type);
		} else {
			return findMatchingConstructor(method, type);
		}
	}

	private Constructor<?> findConvertableConstructor(Method method, Class<?> clazz) throws NoSuchMethodException {
		for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
			if (matchesSignature(method, candidate, null, null)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException(clazz.getSimpleName() + Arrays.asList(method.getParameterTypes()));
	}

	private Constructor<?> findMatchingConstructor(Method method, Class<?> clazz) throws NoSuchMethodException {
		for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
			if (matchesSignature(method, candidate, null, null)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException(clazz.getSimpleName() + Arrays.asList(method.getParameterTypes()));
	}

	protected StaticMethodInvocationHandler createGetterInvocator(Method method) throws NoSuchFieldException {
		return new StaticGetter(type, findField(method), convertedPropertyTypeOf(method));
	}

	protected StaticMethodInvocationHandler createSetterInvocator(Method method) throws NoSuchFieldException {
		return new StaticSetter(type, findField(method), convertedPropertyTypeOf(method));
	}

	private Class<?> convertedPropertyTypeOf(Method method) {
		if (!isConverted(method)) {
			return null;
		}
		return propertyTypeOf(method);
	}

	private Field findField(Method method) throws NoSuchFieldException {
		if (isConverted(method)) {
			return findField(propertyOf(method), propertyTypeOf(method), propertyAnnotationsOf(method));
		} else {
			return findField(propertyOf(method), propertyTypeOf(method), new Annotation[0]);
		}
	}

	protected Field findField(String fieldPattern, Class<?> type, Annotation[] annotations) throws NoSuchFieldException {
		String convert = findTargetTypeName(annotations, type);
		List<String> fieldNames = SignatureUtil.computeFieldNames(fieldPattern);
		Class<?> currentClass = this.type;
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

	protected StaticMethodInvocationHandler createMethodInvocator(Method method) throws NoSuchMethodException {
		Class<?> currentClass = type;
		while (currentClass != Object.class) {
			try {
				Method candidate = findMethod(method, currentClass);
				return new StaticMethodInvoker(currentClass, candidate, findConversionTarget(method));
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(method.getName(), method.getReturnType(), method.getParameterTypes(), method.getExceptionTypes()));
	}

	private Method findMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		if (isConverted(method)) {
			return findConvertableMethod(method, clazz);
		} else {
			return findMatchingMethod(method, clazz);
		}
	}

	private Method findConversionTarget(Method method) {
		if (isConverted(method)) {
			return method;
		} else {
			return null;
		}
	}

	private Method findConvertableMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		String[] conversionVector = determineNeededConversions(method.getParameterAnnotations(), method.getParameterTypes());
		for (Method candidate : clazz.getDeclaredMethods()) {
			String containsConvertable = findTargetTypeName(method.getAnnotations(), method.getReturnType());
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

}
