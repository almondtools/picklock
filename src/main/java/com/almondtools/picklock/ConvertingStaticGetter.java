package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.Field;

/**
 * Wraps a static field with read (getter) access.
 */
public class ConvertingStaticGetter implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Field field;
	private Class<?> targetType;

	public ConvertingStaticGetter(Class<?> type, Field field, Class<?> targetType) {
		this.type = type;
		this.field = field;
		this.targetType = targetType;
		field.setAccessible(true);
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		if (args != null && args.length != 0) {
			throw new IllegalArgumentException("getters can only be invoked with no argument, was " + args.length + " arguments");
		}
		return convertResult(targetType, field.getType(), field.get(type));
	}

}
