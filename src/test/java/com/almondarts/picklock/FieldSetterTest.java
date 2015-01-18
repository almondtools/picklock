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

	@Test
	public void testSetFieldFinal() throws Throwable {
		WithFinalField object = new WithFinalField();
		Object result = new FieldSetter(WithFinalField.class.getDeclaredField("runtime")).invoke(object , new Object[]{"hello"});
		assertThat(result, nullValue());
		assertThat(object.runtime, equalTo("hello"));
	}

	@Test
	public void testSetFieldCompileTimeFinal() throws Throwable {
		WithFinalField object = new WithFinalField();
		Object voidresult = new FieldSetter(WithFinalField.class.getDeclaredField("compiletime")).invoke(object , new Object[]{"hello"});
		assertThat(voidresult, nullValue());
		assertThat(object.compiletime, equalTo("")); // paradox in source code, effect of inlining (see byte code of this line)
		Object result = new FieldGetter(WithFinalField.class.getDeclaredField("compiletime")).invoke(object , new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	private static class WithField {
		
		private String field;
	}

	private static class WithFinalField {
		
		private final String runtime = "".toString();
		private final String compiletime = ""; // yet this line is inlined - in this case changes to it will not effect users of this variable
	}

}
