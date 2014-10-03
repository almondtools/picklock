package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class InvocationResolverTest {

	private InvocationResolver resolver;

	@Before
	public void before() {
		this.resolver = new InvocationResolver(TestSubClass.class);
	}

	@Test
	public void testFindField() throws Exception {
		assertThat(resolver.findField("b", boolean.class), notNullValue());
		assertThat(resolver.findField("s", String.class), notNullValue());
		assertThat(resolver.findField("i", int.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldNonExisting() throws Exception {
		assertThat(resolver.findField("a", boolean.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSuperFieldWronglyTyped() throws Exception {
		assertThat(resolver.findField("s", boolean.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldWronglyTyped() throws Exception {
		assertThat(resolver.findField("b", String.class), notNullValue());
	}

	@Test
	public void testFindSetter() throws Exception {
		assertThat(resolver.findSetter("setS", String.class), notNullValue());
		assertThat(resolver.findSetter("setI", int.class), notNullValue());
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindSetterOnNonexistent() throws Exception {
		resolver.findSetter("setX", String.class);
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindSetterOnWronglyTyped() throws Exception {
		resolver.findSetter("setS", int.class);
	}

	@Test
	public void testFindGetter() throws Exception {
		assertThat(resolver.findGetter("getS", String.class), notNullValue());
		assertThat(resolver.findGetter("getI", int.class), notNullValue());
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindGetterOnNonexistent() throws Exception {
		resolver.findGetter("setX", String.class);
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindGetterOnWronglyTyped() throws Exception {
		resolver.findGetter("setS", int.class);
	}

	@Test
	public void testFindMethod() throws Exception {
		assertThat(resolver.findMethod("methoda", new Class[0], new Class[0]), notNullValue());
		assertThat(resolver.findMethod("methodb", new Class[]{String.class}, new Class[0]), notNullValue());
		assertThat(resolver.findMethod("methodc", new Class[]{String.class}, new Class[]{Exception.class}), notNullValue());
		assertThat(resolver.findMethod("methodd", new Class[]{String.class}, new Class[]{Exception.class}), notNullValue());
		assertThat(resolver.findMethod("methode", new Class[]{String.class}, new Class[]{Exception.class}), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodNonExisting() throws Exception {
		assertThat(resolver.findMethod("methodz", new Class[0], new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglySignature() throws Exception {
		assertThat(resolver.findMethod("methodb", new Class[0], new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyTyped() throws Exception {
		assertThat(resolver.findMethod("methodb", new Class[]{int.class}, new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTyped() throws Exception {
		assertThat(resolver.findMethod("methodb", new Class[]{String.class}, new Class[]{Exception.class}), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTypedInSuperclass() throws Exception {
		assertThat(resolver.findMethod("methode", new Class[]{String.class}, new Class[]{IOException.class}), notNullValue());
	}

	private static class TestClass {
		private String s;
		private int i;

		private String methode(String b) throws Exception {
			return null;
		}
	}

	private static class TestSubClass extends TestClass {
		private boolean b;
		
		private void methoda() {
		}

		private void methodb(String b) {
		}

		private void methodc(String b) throws Exception {
		}

		private String methodd(String b) throws Exception {
			return null;
		}
	}
}
