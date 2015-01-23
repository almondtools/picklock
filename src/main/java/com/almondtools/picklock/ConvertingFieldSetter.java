package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArgument;

import java.lang.reflect.Field;

/**
 * Wraps a field with modification (setter) access. Beyond {@link FieldSetter} this class also converts the argument from a given targetType.
 */
public class ConvertingFieldSetter extends FieldSetter {

	private Class<?> targetType;

	public ConvertingFieldSetter(Field field, Class<?> targetType) {
		super(field);
		this.targetType = targetType;
	}


	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		args[0] = convertArgument(targetType, getField().getType(), args[0]);
		return super.invoke(object, args);
	}
}
