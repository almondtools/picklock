package com.almondarts.picklock;

import static com.almondarts.picklock.SignatureUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

public class SignatureUtilTest {

	@Test
	public void testMethodSignatureOnlyName() throws Exception {
		assertThat(methodSignature("method", String.class, new Class<?>[0], new Class<?>[0]), equalTo("String method()"));
	}

	@Test
	public void testMethodSignatureNameAndException() throws Exception {
		Class<?>[] exceptions = { IOException.class };
		assertThat(methodSignature("method", String.class, new Class<?>[0], exceptions), equalTo("String method() throws IOException"));
	}

	@Test
	public void testMethodSignatureWithParameters() throws Exception {
		Class<?>[] classes = { String.class, int.class };
		assertThat(methodSignature("method", String.class, classes, new Class<?>[0]), equalTo("String method(String,int)"));
	}

	@Test
	public void testIsBooleanGetter() throws Exception {
		assertThat(isBooleanGetter(method("isBooleanGetter")), is(true));
		assertThat(isBooleanGetter(method("isBooleanGetterBoxed")), is(true));
		assertThat(isBooleanGetter(method("is")), is(false));
		assertThat(isBooleanGetter(method("notBooleanGetter")), is(false));
		assertThat(isBooleanGetter(method("isNotBooleanGetter")), is(false));
		assertThat(isBooleanGetter(method("isNotBooleanGetter", String.class)), is(false));
		assertThat(isBooleanGetter(method("isNotBooleanGetterExc")), is(false));
	}

	@Test
	public void testIsGetter() throws Exception {
		assertThat(isGetter(method("getGetter")), is(true));
		assertThat(isGetter(method("getNotGetter")), is(false));
		assertThat(isGetter(method("getNotGetter", String.class)), is(false));
		assertThat(isGetter(method("getNotGetterExc")), is(false));
		assertThat(isGetter(method("get")), is(false));
		assertThat(isGetter(method("notGetter")), is(false));
	}

	@Test
	public void testIsSetter() throws Exception {
		assertThat(isSetter(method("setSetter", String.class)), is(true));
		assertThat(isSetter(method("setNotSetter")), is(false));
		assertThat(isSetter(method("set", String.class)), is(false));
		assertThat(isSetter(method("notSetter", String.class)), is(false));
		assertThat(isSetter(method("setNotSetter", String.class)), is(false));
		assertThat(isSetter(method("setNotSetterExc", String.class)), is(false));
	}

	@Test
	public void testPropertyOf() throws Exception {
		assertThat(propertyOf(method("setSetter", String.class)), equalTo("Setter"));
		assertThat(propertyOf(method("getGetter")), equalTo("Getter"));
		assertThat(propertyOf(method("isBooleanGetter")), equalTo("BooleanGetter"));
	}

	@Test
	public void testPropertyOfNonProperty() throws Exception {
		assertThat(propertyOf(method("isNotBooleanGetter")), equalTo("isNotBooleanGetter"));
	}

	public Method method(String name, Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException {
		return Methods.class.getDeclaredMethod(name, parameterTypes);
	}

	interface Methods {
		boolean isBooleanGetter();

		Boolean isBooleanGetterBoxed();

		boolean isNotBooleanGetter(String s);

		boolean isNotBooleanGetterExc() throws Exception;

		boolean notBooleanGetter();

		boolean is();

		int isNotBooleanGetter();

		String getGetter();

		void getNotGetter();

		String getNotGetter(String s);

		String getNotGetterExc() throws Exception;

		String get();

		String notGetter();

		void setSetter(String s);

		void setNotSetter();

		String setNotSetter(String s);

		void setNotSetterExc(String s) throws Exception;

		void set(String s);

		void notSetter(String s);

	}

}
