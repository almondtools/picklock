package com.almondarts.picklock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ClassAccess extends StaticInvocationResolver implements InvocationHandler {

	private Map<Method, StaticMethodInvocationHandler> methods;

	public ClassAccess(Class<?> type) {
		super(type);
		this.methods = new HashMap<Method, StaticMethodInvocationHandler>();
	}

	public static ClassAccess unlock(Class<?> type) {
		return new ClassAccess(type);
	}

	public static ClassSnoop check(Class<?> clazz) {
		return new ClassSnoop(clazz);
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
		StaticMethodInvocationHandler handler = methods.get(method);
		return handler.invoke(args);
	}

}
