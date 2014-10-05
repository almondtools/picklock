package com.almondarts.picklock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ObjectAccess extends InvocationResolver implements InvocationHandler {

	private Map<Method, MethodInvocationHandler> methods;
	private Object object;

	public ObjectAccess(Object object) {
		super(object.getClass());
		this.methods = new HashMap<Method, MethodInvocationHandler>();
		this.object = object;
	}

	public static ObjectAccess unlock(Object object) {
		return new ObjectAccess(object);
	}

	public static ObjectSnoop check(Class<?> clazz) {
		return new ObjectSnoop(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T features(Class<T> interfaceClass) throws NoSuchMethodException {
		for (Method method : interfaceClass.getDeclaredMethods()) {
			methods.put(method, findInvocationHandler(method));
		}
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocationHandler handler = methods.get(method);
		return handler.invoke(object, args);
	}

}
