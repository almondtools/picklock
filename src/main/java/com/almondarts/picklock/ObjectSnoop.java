package com.almondarts.picklock;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class ObjectSnoop extends InvocationResolver {

	public ObjectSnoop(Class<?> clazz) {
		super(clazz);
	}

	public List<Method> isUnlockable(Class<?> interfaceClazz) {
		List<Method> conflicts = new LinkedList<Method>();
		for (Method method : interfaceClazz.getDeclaredMethods()) {
			try {
				findInvocationHandler(method);
			} catch (NoSuchMethodException e) {
				conflicts.add(method);
			}
		}
		return conflicts;
	}

}
