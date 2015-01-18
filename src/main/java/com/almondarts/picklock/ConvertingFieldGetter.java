package com.almondarts.picklock;

import java.lang.reflect.Field;

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
		return convertResult(field.get(object));
	}

	private Object convertResult(Object result) throws NoSuchMethodException {
		Class<?> fieldType = field.getType();
		if (targetType.equals(fieldType)) {
			return result;
		} else {
			return ObjectAccess.unlock(result).features(targetType);
		}
	}

}
