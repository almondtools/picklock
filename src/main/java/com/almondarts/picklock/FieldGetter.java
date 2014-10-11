package com.almondarts.picklock;

import java.lang.reflect.Field;

public class FieldGetter implements MethodInvocationHandler {

	private Field field;

	public FieldGetter(Field field) {
		this.field = field;
		field.setAccessible(true);
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		if (args != null && args.length != 0) {
			throw new IllegalArgumentException("getters can only be invoked with no argument, was " + args.length + " arguments");
		}
		return field.get(object);
	}

}
