package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;

@SuppressWarnings("unused")
public class InvocationResolverTest {

	@Test
	public void testFindField() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("b", boolean.class, new Annotation[0]), notNullValue());
		assertThat(resolver.findField("s", String.class, new Annotation[0]), notNullValue());
		assertThat(resolver.findField("i", int.class, new Annotation[0]), notNullValue());
	}

	@Test
	public void testCreateGetterInvocator() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] getters = Getters.class.getDeclaredMethods();
		assertThat(resolver.createGetterInvocator(getters[0]), notNullValue());
		assertThat(resolver.createGetterInvocator(getters[1]), notNullValue());
		assertThat(resolver.createGetterInvocator(getters[2]), notNullValue());
	}

	@Test
	public void testCreateSetterInvocator() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] setters = Setters.class.getDeclaredMethods();
		assertThat(resolver.createSetterInvocator(setters[0]), notNullValue());
		assertThat(resolver.createSetterInvocator(setters[1]), notNullValue());
		assertThat(resolver.createSetterInvocator(setters[2]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldNonExisting() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("a", boolean.class, new Annotation[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSuperFieldWronglyTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("s", boolean.class, new Annotation[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldWronglyTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("b", String.class, new Annotation[0]), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorWithoutConversion() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] methods = Methods.class.getDeclaredMethods();
		assertThat(methods.length, equalTo(5));
		assertThat(resolver.createMethodInvocator(methods[0]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[1]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[2]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[3]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[4]), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorWithConversion() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertableTestClass.class);
		Method[] methods = ConvertableMethods.class.getDeclaredMethods();
		assertThat(methods.length, equalTo(2));
		assertThat(resolver.createMethodInvocator(methods[0]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[1]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodNonExisting() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyReturnType() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglySignature() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[2]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[3]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[4]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodWronglyExceptionTypedInSuperclass() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[5]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodNotConvertableArguments() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertableTestClass.class);
		Method[] badmethods = NonConvertableMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodNotConvertableResult() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertableTestClass.class);
		Method[] badmethods = NonConvertableMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodFailedConvertableArguments() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertableTestClass.class);
		Method[] badmethods = FailedConvertableMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testFindMethodFailedConvertableResult() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertableTestClass.class);
		Method[] badmethods = FailedConvertableMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	interface Getters {
		String getS();
		int getI();
		boolean getB();
	}

	interface Setters {
		void setS(String s);
		void setI(int i);
		void setB(boolean b);
	}
	
	interface Methods {
		void methoda();

		void methodb(String s);

		void methodc(String s) throws Exception;

		String methodd(String s) throws Exception;

		String methode(String s) throws Exception;
	}

	interface BadMethods {
		String methodz();

		String methoda();

		void methodb();

		void methodb(int i);

		void methodb(String s) throws Exception;

		String methode(String s) throws IOException;
	}

	interface ConvertableMethods {

		void methoda(@Convert("ConvertableObject") ConvertableInterface o);

		@Convert("ConvertableObject")
		ConvertableInterface methodb();
	}

	interface NonConvertableMethods {

		void methoda(ConvertableInterface o);

		ConvertableInterface methodb();
	}

	interface FailedConvertableMethods {

		void methoda(@Convert ConvertableInterface o);

		@Convert
		ConvertableInterface methodb();
	}

	interface ConvertableInterface {
		String getContent();
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

		private String method0() {
			return null;
		}

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

	private static class ConvertableTestClass {

		private void methoda(ConvertableObject o) {
		}

		private ConvertableObject methodb() {
			return null;
		}
	}

	private static class ConvertableObject {
		private String content = "content";
	}

}
