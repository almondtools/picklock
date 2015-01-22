package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.Field;

/**
 * Wraps a field with read (getter) access. Beyond {@link FieldGetter} this class also wraps the result to a given targetType.
 */
public class ConvertingFieldGetter implements MethodInvocationHandler {

	private Field field;
	private Class<?> targetType;

	public ConvertingFieldGetter(Field field, Class<?> targetType) {
		this.field = field;
		this.targetType = targetType;
		field.setAccessible(true);
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		if (args != null && args.length != 0) {
			throw new IllegalArgumentException("getters can only be invoked with no argument, was " + args.length + " arguments");
		}
		return convertResult(targetType, field.getType(), field.get(object));
	}

}
