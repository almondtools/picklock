package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;


@SuppressWarnings("unused")
public class ConvertingFieldGetterTest {

	@Test
	public void testGetField() throws Throwable {
		Object result = new ConvertingFieldGetter(WithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new WithField() , new Object[0]);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new ConvertingFieldGetter(WithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new WithField() , new Object[]{1});
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new ConvertingFieldGetter(WithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(new WithField() , null);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test
	public void testGetFieldNonConvertable() throws Throwable {
		Object result = new ConvertingFieldGetter(WithField.class.getDeclaredField("other"), String.class).invoke(new WithField() , new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	interface ConvertingInterface {
		String getContent();
	}
	
	private static class WithField {
		
		private String other = "hello";
		private ConvertableField field = new ConvertableField("world");
	}

	private static class ConvertableField {

		private String content;

		public ConvertableField(String content) {
			this.content = content;
		}
		
	}
}
