package com.almondtools.picklock;

import static com.almondtools.picklock.Converter.convertArgument;
import static com.almondtools.picklock.FinalUtil.ensureNonFinal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Wraps a field with modification (setter) access.
 */
public class FieldSetter implements MethodInvocationHandler {

	private Field field;
	private Class<?> target;

	/**
	 * Sets a value on the given field.
	 * @param field the field to access
	 */
	public FieldSetter(Field field) {
		this.field = field;
		field.setAccessible(true);
		ensureNonFinal(field);
	}
	
	/**
	 * Sets a value on the given field. Beyond {@link #FieldSetter(Field)} this constructor also converts the argument
	 * @param field the field to access
	 * @param target the target signature (source arguments)
	 * @see Convert 
	 */
	public FieldSetter(Field field, Class<?> target) {
		this(field);
		this.target = target;
		ensureNonFinal(field);
	}

	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		Object arg = a(args[0]);
		if (arg != null && !BoxingUtil.getBoxed(field.getType()).isInstance(arg)) {
			throw new ClassCastException("defined type of " + field.getName() + " is " + arg.getClass().getSimpleName() + ", but assigned type was " + field.getType().getSimpleName());
		}
		field.set(object, arg);
		return null;
	}

	private Object a(Object arg) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (target == null) {
			return arg;
		}
		return convertArgument(target, field.getType(), arg);
	}

}
