package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class ConvertingFieldSetterTest {

	@Test
	public void testSetField() throws Throwable {
		WithField object = new WithField();
		Object result = new ConvertingFieldSetter(WithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object , new Object[]{proxy("hello")});
		assertThat(result, nullValue());
		assertThat(object.field.content, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNone() throws Throwable {
		WithField object = new WithField();
		new ConvertingFieldSetter(WithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object , new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNull() throws Throwable {
		WithField object = new WithField();
		new ConvertingFieldSetter(WithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object , null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignature2() throws Throwable {
		WithField object = new WithField();
		new ConvertingFieldSetter(WithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object , new Object[]{proxy("hello"), "world"});
	}

	@Test(expected=ClassCastException.class)
	public void testSetFieldWithoutMatchingType() throws Throwable {
		WithField object = new WithField();
		new ConvertingFieldSetter(WithField.class.getDeclaredField("other"), String.class).invoke(object , new Object[]{Integer.valueOf(1)});
	}

	@Test
	public void testSetFieldNonConvertable() throws Throwable {
		WithField object = new WithField();
		Object result = new ConvertingFieldSetter(WithField.class.getDeclaredField("other"), String.class).invoke(object , new Object[]{"hello"});
		assertThat(result, nullValue());
		assertThat(object.other, equalTo("hello"));
	}

	@Test
	public void testSetFieldFinal() throws Throwable {
		WithFinalField object = new WithFinalField();
		Object result = new ConvertingFieldSetter(WithFinalField.class.getDeclaredField("finalfield"), ConvertingInterface.class).invoke(object , new Object[]{proxy("hello")});
		assertThat(result, nullValue());
		assertThat(object.finalfield.content, equalTo("hello"));
	}

	private static ConvertingInterface proxy(final String s) {
		return new ConvertingInterface() {

			@Override
			public String getContent() {
				return s;
			}

			@Override
			public void setContent(String s) {
			}
			
		};
	}
	

	interface ConvertingInterface {
		String getContent();
		void setContent(String s);
	}
	
	private static class WithField {
		
		public String other = "hello";
		public ConvertableField field = new ConvertableField();
	}

	private static class WithFinalField {
		
		public final ConvertableField finalfield = new ConvertableField();
	}

	private static class ConvertableField {

		public String content = "world";

		public ConvertableField() {
		}
		
	}

}
