package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertResult;

import java.lang.reflect.Field;

/**
 * Wraps a field with read (getter) access. Beyond {@link FieldGetter} this class also wraps the result to a given targetType.
 */
public class ConvertingFieldGetter extends FieldGetter {

	private Class<?> targetType;

	public ConvertingFieldGetter(Field field, Class<?> targetType) {
		super(field);
		this.targetType = targetType;
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		Object result = super.invoke(object, args);
		return convertResult(targetType, getField().getType(), result);
	}

}
