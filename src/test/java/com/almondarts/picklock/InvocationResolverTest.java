package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;

import org.hamcrest.CoreMatchers;
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
		assertThat(resolver.findSetter("S", String.class), notNullValue());
		assertThat(resolver.findSetter("I", int.class), notNullValue());
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindSetterOnNonexistent() throws Exception {
		resolver.findSetter("X", String.class);
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindSetterOnWronglyTyped() throws Exception {
		resolver.findSetter("S", int.class);
	}

	@Test
	public void testFindGetter() throws Exception {
		assertThat(resolver.findGetter("S", String.class), notNullValue());
		assertThat(resolver.findGetter("I", int.class), notNullValue());
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindGetterOnNonexistent() throws Exception {
		resolver.findGetter("X", String.class);
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindGetterOnWronglyTyped() throws Exception {
		resolver.findGetter("S", int.class);
	}

	@Test
	public void testFindIs() throws Exception {
		assertThat(resolver.findIs("B", boolean.class), notNullValue());
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindIsOnNonexistent() throws Exception {
		resolver.findIs("X", boolean.class);
	}

	@Test(expected=NoSuchFieldException.class)
	public void testFindIsOnWronglyTyped() throws Exception {
		resolver.findIs("S", int.class);
	}

	@Test
	public void testFindMethod() throws Exception {
		Method[] methods = Methods.class.getDeclaredMethods();
		assertThat(methods.length, equalTo(5));
		assertThat(resolver.findMethod(methods[0]), notNullValue());
		assertThat(resolver.findMethod(methods[1]), notNullValue());
		assertThat(resolver.findMethod(methods[2]), notNullValue());
		assertThat(resolver.findMethod(methods[3]), notNullValue());
		assertThat(resolver.findMethod(methods[4]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodNonExisting() throws Exception {
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.findMethod(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglySignature() throws Exception {
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.findMethod(badmethods[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyTyped() throws Exception {
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.findMethod(badmethods[2]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTyped() throws Exception {
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.findMethod(badmethods[3]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTypedInSuperclass() throws Exception {
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.findMethod(badmethods[4]);
	}
	
	interface Methods {
		String methoda();
		String methodb(String s);
		String methodc(String s) throws Exception;
		String methodd(String s) throws Exception;
		String methode(String s) throws Exception;
	}

	interface BadMethods {
		String methodz();
		String methodb();
		String methodb(int i);
		String methodb(String s) throws Exception;
		String methode(String s) throws IOException;
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
