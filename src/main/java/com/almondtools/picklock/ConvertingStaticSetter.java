package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArgument;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Wraps a static field with modification (setter) access.
 * 
 * unfortunately some java compiler do inline literal constants. This setter may change the constant, but does not change inlined literals, resulting in strange effects.
 * better avoid setting static final variables or make sure, that they cannot be inlined (e.g. by making its value a trivial functional expression)
 */
public class ConvertingStaticSetter implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Field field;
	private Class<?> targetType;

	public ConvertingStaticSetter(Class<?> type, Field field, Class<?> targetType) {
		this.type = type;
		this.field = field;
		this.targetType = targetType;
		field.setAccessible(true);
		if (isFinal(field)) {
			makeNonFinal(field);
		}
	}

	private boolean isFinal(Field field) {
		return (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
	}

	private void makeNonFinal(Field field) {
		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		} catch (Exception e) {
			//omit this exception
		}
	}

	@Override
	public Object invoke(Object[] args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		Object arg = convertArgument(targetType, field.getType(), args[0]);
		if (args[0] != null && !BoxingUtil.getBoxed(field.getType()).isInstance(args[0])) {
			throw new ClassCastException("defined type of " + field.getName() + " is " + args[0].getClass().getSimpleName() + ", but assigned type was " + field.getType().getSimpleName());
		}
		field.set(type, arg);
		return null;
	}

}
