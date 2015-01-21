package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convert;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Wraps a field with modification (setter) access. Beyond {@link FieldSetter} this class also converts the argument from a given targetType.
 */
public class ConvertingFieldSetter implements MethodInvocationHandler {

	private Field field;
	private Class<?> targetType;

	public ConvertingFieldSetter(Field field, Class<?> targetType) {
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
			// omit this exception
		}
	}

	@Override
	public Object invoke(Object object, Object[] args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		Object value = convertArgument(args[0]);
		if (value != null && !BoxingUtil.getBoxed(field.getType()).isInstance(value)) {
			throw new ClassCastException("defined type of " + field.getName() + " is " + value.getClass().getSimpleName() + ", but assigned type was " + field.getType().getSimpleName());
		}
		field.set(object, value);
		return null;
	}

	private Object convertArgument(Object argument) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Class<?> fieldType = field.getType();
		if (targetType.equals(fieldType)) {
			return argument;
		} else {
			return convert(argument, fieldType, targetType);
		}
	}
}
