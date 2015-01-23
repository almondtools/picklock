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
		Object result = new FieldGetter(ConvertableWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new ConvertableWithField(), new Object[0]);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter(ConvertableWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new ConvertableWithField(), new Object[] { 1 });
	}

	@Test
	public void testConvertingGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter(ConvertableWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new ConvertableWithField(), (Object[]) null);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test
	public void testConvertingGetFieldNonConvertable() throws Throwable {
		Object result = new FieldGetter(ConvertableWithField.class.getDeclaredField("other"), String.class).invoke(new ConvertableWithField(), new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	interface ConvertingInterface {
		String getContent();
	}

	private static class ConvertableWithField {

		private String other = "hello";
		private ConvertableField field = new ConvertableField("world");
	}

	private static class ConvertableField {

		private String content;

		public ConvertableField(String content) {
			this.content = content;
		}

	}

	private static class WithField {

		private String field = "world";
	}

}
