package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;


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
		Object result = new StaticGetter(WithField.class, WithField.class.getDeclaredField("field")).invoke((Object[]) null);
		assertThat((String) result, equalTo("world"));
	}

	@Test
	public void testInvokeWithResultConversion() throws Throwable {
		StaticGetter staticMethod = new StaticGetter(WithConvertedProperty.class, WithConvertedProperty.class.getDeclaredField("converted"), ConvertedInterface.class);
		Object result = staticMethod.invoke();
		assertThat(result, instanceOf(ConvertedInterface.class));
	}
	
	interface Properties {
		@Convert("WithConvertedProperty") ConvertedInterface getConverted();
	}
	
	@SuppressWarnings("unused")
	private static class WithField {
		
		private static String field = "world";
	}

	@SuppressWarnings("unused")
	private static class WithConvertedProperty {
		
		private static WithConvertedProperty converted = new WithConvertedProperty();

	}
	
	interface ConvertedInterface {
	}
}
