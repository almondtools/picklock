package com.almondarts.picklock;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldSetter implements MethodInvocationHandler {

	private Field field;

	public FieldSetter(Field field) {
		this.field = field;
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
		if (args[0] != null && !BoxingUtil.getBoxed(field.getType()).isInstance(args[0])) {
			throw new ClassCastException("defined type of " + field.getName() + " is " + args[0].getClass().getSimpleName() + ", but assigned type was " + field.getType().getSimpleName());
		}
		field.set(object, args[0]);
		return null;
	}

}
