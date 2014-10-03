package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;


@SuppressWarnings("unused")
public class FieldGetterTest {

	@Test
	public void testGetField() throws Throwable {
		Object result = new FieldGetter(WithField.class.getDeclaredField("field")).invoke(new WithField() , new Object[0]);
		assertThat((String) result, equalTo("world"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter(WithField.class.getDeclaredField("field")).invoke(new WithField() , new Object[]{1});
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter(WithField.class.getDeclaredField("field")).invoke(new WithField() , null);
		assertThat((String) result, equalTo("world"));
	}

	private static class WithField {
		
		private String field = "world";
	}

}
