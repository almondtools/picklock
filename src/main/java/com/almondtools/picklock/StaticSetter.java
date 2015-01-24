package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArgument;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Wraps a static field with modification (setter) access.
 * 
 * unfortunately some java compiler do inline literal constants. This setter may change the constant, but does not change inlined literals, resulting in strange effects.
 * better avoid setting static final variables or make sure, that they cannot be inlined (e.g. by making its value a trivial functional expression)
 */
public class StaticSetter implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Field field;
	private Class<?> target;

	/**
	 * Sets a value on the given field.
	 * @param type the static type of the field to access
	 * @param field the field to access
	 */
	public StaticSetter(Class<?> type, Field field) {
		this.type = type;
		this.field = field;
		field.setAccessible(true);
		if (isFinal(field)) {
			makeNonFinal(field);
		}
	}
	
	/**
	 * Sets a value on the given field. Beyond {@link #StaticSetter(Class, Field)} this constructor also converts the argument
	 * @param type the static type of the field to access
	 * @param field the field to access
	 * @param target the target signature (source arguments)
	 * @see Convert 
	 */
	public StaticSetter(Class<?> type, Field field, Class<?> target) {
		this(type, field);
		this.target = target;
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
	public Object invoke(Object... args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		Object arg = a(args[0]);
		if (arg != null && !BoxingUtil.getBoxed(field.getType()).isInstance(arg)) {
			throw new ClassCastException("defined type of " + field.getName() + " is " + arg.getClass().getSimpleName() + ", but assigned type was " + field.getType().getSimpleName());
		}
		field.set(type, arg);
		return null;
	}

	private Object a(Object arg) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (target == null) {
			return arg;
		}
		return convertArgument(target, field.getType(), arg);
	}

}
