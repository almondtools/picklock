package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.determineNeededConversions;
import static com.almondtools.picklock.Converter.isConverted;
import static com.almondtools.picklock.SignatureUtil.containsConvertable;
import static com.almondtools.picklock.SignatureUtil.fieldSignature;
import static com.almondtools.picklock.SignatureUtil.isBooleanGetter;
import static com.almondtools.picklock.SignatureUtil.isConstructor;
import static com.almondtools.picklock.SignatureUtil.isGetter;
import static com.almondtools.picklock.SignatureUtil.isSetter;
import static com.almondtools.picklock.SignatureUtil.matchesSignature;
import static com.almondtools.picklock.SignatureUtil.methodSignature;
import static com.almondtools.picklock.SignatureUtil.propertyOf;

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

	protected StaticMethodInvocationHandler createMethodInvocator(Method method) throws NoSuchMethodException {
		Class<?> currentClass = type;
		while (currentClass != Object.class) {
			try {
				if (isConverted(method)) {
					Method candidate = findConvertableMethod(method, currentClass);
					return new ConvertingStaticMethodInvoker(currentClass, candidate, method);
				} else {
					Method canditate = findMatchingMethod(method, currentClass);
					return new StaticMethodInvoker(currentClass, canditate);
				}
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(method.getName(), method.getReturnType(), method.getParameterTypes(), method.getExceptionTypes()));
	}

	protected StaticMethodInvocationHandler createConstructorInvocator(Method method) throws NoSuchMethodException {
		if (isConverted(method)) {
			Constructor<?> constructor = findConvertableConstructor(method, type);
			return new ConvertingConstructorInvoker(constructor, method);
		} else {
			Constructor<?> constructor = findMatchingConstructor(method, type);
			return new ConstructorInvoker(constructor);
		}
	}

	protected StaticMethodInvocationHandler createGetterInvocator(Method method) throws NoSuchFieldException {
		if (isConverted(method)) {
			return new ConvertingStaticGetter(type, findField(propertyOf(method), method.getReturnType(), method.getAnnotations()), method.getReturnType());
		} else {
			return new StaticGetter(type, findField(propertyOf(method), method.getReturnType(), new Annotation[0]));
		}
	}

	protected StaticMethodInvocationHandler createSetterInvocator(Method method) throws NoSuchFieldException {
		if (isConverted(method)) {
			return new ConvertingStaticSetter(type, findField(propertyOf(method), method.getParameterTypes()[0], method.getParameterAnnotations()[0]), method.getParameterTypes()[0]);
		} else {
			return new StaticSetter(type, findField(propertyOf(method), method.getParameterTypes()[0], new Annotation[0]));
		}
	}

	protected Field findField(String fieldPattern, Class<?> type, Annotation[] annotations) throws NoSuchFieldException {
		String convert = containsConvertable(annotations, type);
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

	private Method findConvertableMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		String[] conversionVector = determineNeededConversions(method.getParameterAnnotations(), method.getParameterTypes());
		for (Method candidate : clazz.getDeclaredMethods()) {
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

}
