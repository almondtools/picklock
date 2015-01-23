package com.almondtools.picklock;

import java.lang.reflect.Field;

/**
 * Wraps a static field with read (getter) access.
 */
public class StaticGetter implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Field field;

	public StaticGetter(Class<?> type, Field field) {
		this.type = type;
		this.field = field;
		field.setAccessible(true);
	}
	
	public Field getField() {
		return field;
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		if (args != null && args.length != 0) {
			throw new IllegalArgumentException("getters can only be invoked with no argument, was " + args.length + " arguments");
		}
		return field.get(type);
	}

}
