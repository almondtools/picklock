package com.almondarts.picklock;

import static com.almondarts.picklock.MethodClassification.isBooleanGetter;
import static com.almondarts.picklock.MethodClassification.isGetter;
import static com.almondarts.picklock.MethodClassification.isSetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConvertingMethodInvoker implements MethodInvocationHandler {

	private Method method;
	private Method target;

	public ConvertingMethodInvoker(Method method, Method target) {
		this.method = method;
		this.target = target;
		method.setAccessible(true);
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		try {
			Object[] convertArguments = convertArguments(args);
			Object result = method.invoke(object, convertArguments);
			return convertResult(result);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private Object[] convertArguments(Object[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, SecurityException {
		if (args == null) {
			args = new Object[0];
		}
		Object[] converted = new Object[args.length];
		Class<?>[] targetArgumentTypes = target.getParameterTypes();
		Class<?>[] methodArgumentTypes = method.getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			if (targetArgumentTypes[i].equals(methodArgumentTypes[i])) {
				converted[i] = args[i];
			} else {
				converted[i] = convert(args[i], methodArgumentTypes[i], targetArgumentTypes[i]);
			}
		}
		return converted;
	}

	private Object convert(Object object, Class<?> clazz, Class<?> accessibleClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, SecurityException {
		if (object instanceof Proxy) {
			InvocationHandler invocationHandler = Proxy.getInvocationHandler((Proxy) object);
			if (invocationHandler instanceof ObjectAccess) {
				return ((ObjectAccess) invocationHandler).getObject();
			}
		}
		Constructor<?> constructor = clazz.getConstructor();
		constructor.setAccessible(true);
		Object converted = constructor.newInstance();
		Object accessible = ObjectAccess.unlock(converted).features(accessibleClass);
		for (Method[] getSetPair : findProperties(accessibleClass)) {
			Method get = getSetPair[0];
			get.setAccessible(true);
			Method set = getSetPair[1];
			set.setAccessible(true);
			Object value = get.invoke(object);
			set.invoke(accessible, value);
		}
		return converted;
	}

	private List<Method[]> findProperties(Class<?> accessibleClass) {
		Converter converter = new Converter();
		for (Method method : accessibleClass.getDeclaredMethods()) {
			if (isSetter(method)) {
				converter.addSetter(method);
			} else if (isGetter(method) || isBooleanGetter(method)) {
				converter.addGetter(method);
			}
		}
		return converter.getReadWritablePropertyPairs();
	}

	private Object convertResult(Object result) throws NoSuchMethodException {
		Class<?> targetType = target.getReturnType();
		Class<?> methodType = method.getReturnType();
		if (targetType.equals(methodType)) {
			return result;
		} else {
			return ObjectAccess.unlock(result).features(targetType);
		}
	}

	private static class Converter {

		private static final int GETTER = 0;
		private static final int SETTER = 1;

		private Map<String, Method[]> properties;

		public Converter() {
			properties = new LinkedHashMap<String, Method[]>();
		}

		public void addGetter(Method method) {
			Method[] methods = fetchMethods(method);
			methods[GETTER] = method;
		}

		public void addSetter(Method method) {
			Method[] methods = fetchMethods(method);
			methods[SETTER] = method;
		}

		private Method[] fetchMethods(Method method) {
			String name = MethodClassification.propertyOf(method);
			Method[] methods = properties.get(name);
			if (methods == null) {
				methods = new Method[2];
				properties.put(name, methods);
			}
			return methods;
		}

		public List<Method[]> getReadWritablePropertyPairs() {
			List<Method[]> propertyPairs = new ArrayList<Method[]>();
			for (Method[] pair : properties.values()) {
				if (pair[0] != null && pair[1] != null) {
					propertyPairs.add(pair);
				}
			}
			return propertyPairs;
		}

	}

}
