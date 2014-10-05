package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class StaticInvocationResolverTest {

	private StaticInvocationResolver resolver;

	@Before
	public void before() {
		this.resolver = new StaticInvocationResolver(TestSubClass.class);
	}

	@Test
	public void testFindField() throws Exception {
		assertThat(resolver.findField("bo", boolean.class), notNullValue());
		assertThat(resolver.findField("st", String.class), notNullValue());
		assertThat(resolver.findField("IN", int.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldNonExisting() throws Exception {
		assertThat(resolver.findField("a", boolean.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSuperFieldWronglyTyped() throws Exception {
		assertThat(resolver.findField("st", boolean.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldWronglyTyped() throws Exception {
		assertThat(resolver.findField("bo", String.class), notNullValue());
	}

	@Test
	public void testFindSetter() throws Exception {
		assertThat(resolver.findSetter("setSt", String.class), notNullValue());
		assertThat(resolver.findSetter("setIN", int.class), notNullValue());
		assertThat(resolver.findGetter("setBo", boolean.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSetterOnNonexistent() throws Exception {
		resolver.findSetter("setX", String.class);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSetterOnWronglyTyped() throws Exception {
		resolver.findSetter("setS", int.class);
	}

	@Test
	public void testFindGetter() throws Exception {
		assertThat(resolver.findGetter("getSt", String.class), notNullValue());
		assertThat(resolver.findGetter("getIN", int.class), notNullValue());
		assertThat(resolver.findGetter("getBo", boolean.class), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindGetterOnNonexistent() throws Exception {
		resolver.findGetter("setX", String.class);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindGetterOnWronglyTyped() throws Exception {
		resolver.findGetter("setS", int.class);
	}

	@Test
	public void testFindMethod() throws Exception {
		assertThat(resolver.findMethod("methoda", String.class, new Class[0], new Class[0]), notNullValue());
		assertThat(resolver.findMethod("methodb", String.class, new Class[] { String.class }, new Class[0]), notNullValue());
		assertThat(resolver.findMethod("methodc", String.class, new Class[] { String.class }, new Class[] { Exception.class }), notNullValue());
		assertThat(resolver.findMethod("methodd", String.class, new Class[] { String.class }, new Class[] { Exception.class }), notNullValue());
		assertThat(resolver.findMethod("methode", String.class, new Class[] { String.class }, new Class[] { Exception.class }), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodNonExisting() throws Exception {
		assertThat(resolver.findMethod("methodz", String.class, new Class[0], new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglySignature() throws Exception {
		assertThat(resolver.findMethod("methodb", String.class, new Class[0], new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyTyped() throws Exception {
		assertThat(resolver.findMethod("methodb", String.class, new Class[] { int.class }, new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTyped() throws Exception {
		assertThat(resolver.findMethod("methodb", String.class, new Class[] { String.class }, new Class[] { Exception.class }), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTypedInSuperclass() throws Exception {
		assertThat(resolver.findMethod("methode", String.class, new Class[] { String.class }, new Class[] { IOException.class }), notNullValue());
	}

	@Test
	public void testFindConstructor() throws Exception {
		assertThat(resolver.findConstructor(new Class[0], new Class[0]), notNullValue());
		assertThat(resolver.findConstructor(new Class[] { String.class }, new Class[0]), notNullValue());
		assertThat(resolver.findConstructor(new Class[] { int.class }, new Class[] { Exception.class }), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindConstructorWronglySignature() throws Exception {
		assertThat(resolver.findConstructor(new Class[]{String.class, boolean.class}, new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindConstructorWronglyTyped() throws Exception {
		assertThat(resolver.findConstructor(new Class[] { boolean.class }, new Class[0]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindConstructorWronglyExceptionTyped() throws Exception {
		assertThat(resolver.findConstructor(new Class[] { int.class }, new Class[] { IOException.class }), notNullValue());
	}

	@Test
	public void testMatches() throws Exception {
		assertThat(resolver.matches(new Class[0], new Class[0]), equalTo(true));
		assertThat(resolver.matches(new Class[] { String.class }, new Class[] { String.class }), equalTo(true));
		assertThat(resolver.matches(new Class[] { String.class }, new Class[0]), equalTo(false));
		assertThat(resolver.matches(new Class[0], new Class[] { String.class }), equalTo(false));
		assertThat(resolver.matches(new Class[] { int.class }, new Class[] { String.class }), equalTo(false));
		assertThat(resolver.matches(new Class[] { String.class }, new Class[] { int.class }), equalTo(false));
		assertThat(resolver.matches(new Class[] { Object.class }, new Class[] { String.class }), equalTo(true));
		assertThat(resolver.matches(new Class[] { String.class }, new Class[] { Object.class }), equalTo(false));
	}

	private static class TestClass {
		private static String st;
		private static final int IN = 0;

		private static String methode(String b) throws Exception {
			return null;
		}
	}

	private static class TestSubClass extends TestClass {
		private static boolean bo;

		private TestSubClass() {
		}

		private TestSubClass(String s) {
		}

		private TestSubClass(int i) throws Exception {
		}

		private static void methoda() {
		}

		private static void methodb(String b) {
		}

		private static void methodc(String b) throws Exception {
		}

		private static String methodd(String b) throws Exception {
			return null;
		}
	}

}
