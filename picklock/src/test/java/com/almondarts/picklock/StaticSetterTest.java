package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StaticSetterTest {
	
	@Test
	public void testSetField() throws Throwable {
		Object result = new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(WithField.field, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNone() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNull() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignature2() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[] { "hello", "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testSetFieldWithoutMatchingType() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testSetStaticFinalField() throws Throwable {
		Object result = new StaticSetter(WithField.class, WithStaticFinalField.class.getDeclaredField("FIELD")).invoke(new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(WithStaticFinalField.FIELD, equalTo("hello"));
	}

	private static class WithField {

		private static String field;
	}

	private static class WithStaticFinalField {

		static final String FIELD = "ABC".toString();
	}
}
