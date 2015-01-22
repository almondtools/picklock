package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;


@SuppressWarnings("unused")
public class StaticGetterTest {

	@Test
	public void testGetField() throws Throwable {
		Object result = new StaticGetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[0]);
		assertThat((String) result, equalTo("world"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new StaticGetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[]{1});
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new StaticGetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(null);
		assertThat((String) result, equalTo("world"));
	}

	private static class WithField {
		
		private static String field = "world";
	}

}
