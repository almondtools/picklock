package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArgument;

import java.lang.reflect.Field;

/**
 * Wraps a static field with modification (setter) access.
 * 
 * unfortunately some java compiler do inline literal constants. This setter may change the constant, but does not change inlined literals, resulting in strange effects.
 * better avoid setting static final variables or make sure, that they cannot be inlined (e.g. by making its value a trivial functional expression)
 */
public class ConvertingStaticSetter extends StaticSetter {

	private Class<?> targetType;

	public ConvertingStaticSetter(Class<?> type, Field field, Class<?> targetType) {
		super(type, field);
		this.targetType = targetType;
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		args[0] = convertArgument(targetType, getField().getType(), args[0]);
		return super.invoke(args);
	}

}
