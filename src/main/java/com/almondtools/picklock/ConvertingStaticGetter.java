package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.Field;

/**
 * Wraps a static field with read (getter) access.
 */
public class ConvertingStaticGetter extends StaticGetter {

	private Class<?> targetType;

	public ConvertingStaticGetter(Class<?> type, Field field, Class<?> targetType) {
		super(type, field);
		this.targetType = targetType;
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		Object result = super.invoke(args);
		return convertResult(targetType, getField().getType(), result);
	}

}
