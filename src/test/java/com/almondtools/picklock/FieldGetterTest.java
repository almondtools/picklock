package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

@SuppressWarnings("unused")
public class FieldGetterTest {

	@Test
	public void testGetField() throws Throwable {
		Object result = new FieldGetter(WithField.class.getDeclaredField("field")).invoke(new WithField(), new Object[0]);
		assertThat((String) result, equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter(WithField.class.getDeclaredField("field")).invoke(new WithField(), new Object[] { 1 });
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter(WithField.class.getDeclaredField("field")).invoke(new WithField(), (Object[]) null);
		assertThat((String) result, equalTo("world"));
	}

	@Test
	public void testConvertingGetField() throws Throwable {
		Object result = new FieldGetter(ConvertibleWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new ConvertibleWithField(), new Object[0]);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter(ConvertibleWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new ConvertibleWithField(), new Object[] { 1 });
	}

	@Test
	public void testConvertingGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter(ConvertibleWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new ConvertibleWithField(), (Object[]) null);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test
	public void testConvertingGetFieldNonConvertible() throws Throwable {
		Object result = new FieldGetter(ConvertibleWithField.class.getDeclaredField("other"), String.class).invoke(new ConvertibleWithField(), new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	interface ConvertingInterface {
		String getContent();
	}

	private static class ConvertibleWithField {

		private String other = "hello";
		private ConvertibleField field = new ConvertibleField("world");
	}

	private static class ConvertibleField {

		private String content;

		public ConvertibleField(String content) {
			this.content = content;
		}

	}

	private static class WithField {

		private String field = "world";
	}

}
