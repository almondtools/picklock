package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class FieldSetterTest {

	@Test
	public void testSetField() throws Throwable {
		WithField object = new WithField();
		Object result = new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object , new Object[]{"hello"});
		assertThat(result, nullValue());
		assertThat(object.field, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNone() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object , new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNull() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object , null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignature2() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object , new Object[]{"hello", "world"});
	}

	@Test(expected=ClassCastException.class)
	public void testSetFieldWithoutMatchingType() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object , new Object[]{Integer.valueOf(1)});
	}

	private static class WithField {
		
		private String field;
	}

}
