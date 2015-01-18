package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

import com.almondtools.picklock.Construct;
import com.almondtools.picklock.ConstructorConfig;
import com.almondtools.picklock.ConvertingMethodInvoker;
import com.almondtools.picklock.ObjectAccess;

@SuppressWarnings("unused")
public class ConvertingMethodInvokerTest {

	@Test
	public void testInvoke() throws Throwable {
		WithMethod object = new WithMethod();
		Object invoke = new ConvertingMethodInvoker(staticMethod(), staticMethod()).invoke(object, new Object[] { 1 });
		assertThat((String) invoke, equalTo("1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvokeFailingSignature() throws Throwable {
		WithMethod object = new WithMethod();
		new ConvertingMethodInvoker(staticMethod(), staticMethod()).invoke(object, new Object[] { "1" });
	}

	@Test(expected = IOException.class)
	public void testInvokeCheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new ConvertingMethodInvoker(staticException(), staticException()).invoke(object, new Object[] { 2 });
	}

	@Test(expected = NullPointerException.class)
	public void testInvokeUncheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new ConvertingMethodInvoker(staticException(), staticException()).invoke(object, new Object[] { 1 });
	}

	@Test
	public void testInvokeWithDifferentMethods() throws Throwable {
		ForSimpleObject object = new ForSimpleObject();
		Object invoke = new ConvertingMethodInvoker(staticLongMethod(), interfaceLongMethod()).invoke(object, new Object[] { 1l, "2" });
		assertThat((Integer) invoke, equalTo(3));
	}

	@Test
	public void testConvertArgumentsNull() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticLongMethod(), interfaceLongMethod());
		assertThat(convertingMethodInvoker.convertArguments((Object[]) null), equalTo(new Object[0]));
	}

	@Test
	public void testConvertArgumentsKeepingValues() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticLongMethod(), interfaceLongMethod());
		assertThat(convertingMethodInvoker.convertArguments(1, "3"), equalTo(new Object[] { 1, "3" }));
	}

	@Test
	public void testConvertResultKeepingValues() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticLongMethod(), interfaceLongMethod());
		assertThat(convertingMethodInvoker.convertResult(Integer.valueOf(2)), equalTo((Object) Integer.valueOf(2)));
	}

	@Test
	public void testConvertArgumentsConvertingValues() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticSimpleObjectMethod(), interfaceSimpleObjectMethod());
		assertThat(convertingMethodInvoker.convertArguments(simpleObjectInterface("value")), equalTo(new Object[] { SimpleObject.build("value") }));
	}

	@Test
	public void testConvertResultConvertingValues() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticSimpleObjectMethod(), interfaceSimpleObjectMethod());
		assertThat(convertingMethodInvoker.convertResult(SimpleObject.build("value")), instanceOf(SimpleObjectInterface.class));
		assertThat(((SimpleObjectInterface) convertingMethodInvoker.convertResult(SimpleObject.build("value"))).getString(), equalTo("value"));
	}

	@Test
	public void testConvertArgumentsConvertingPicklockedValues() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticSimpleObjectMethod(), interfaceSimpleObjectMethod());
		SimpleObject val = SimpleObject.build("value");
		assertThat(convertingMethodInvoker.convertArguments(ObjectAccess.unlock(val).features(SimpleObjectInterface.class)), equalTo(new Object[] { val }));
	}

	@Test
	public void testConvertArgumentsConvertingValuesWithoutStandardConstructor() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticSimpleOtherMethod(), interfaceSimpleOtherMethod());
		assertThat(convertingMethodInvoker.convertArguments(simpleOtherInterface("value")), equalTo(new Object[] { new SimpleOtherObject("value") }));
	}

	@Test
	public void testConvertResultConvertingValuesWithoutStandardConstructor() throws Exception {
		ForSimpleObject object = new ForSimpleObject();
		ConvertingMethodInvoker convertingMethodInvoker = new ConvertingMethodInvoker(staticSimpleOtherMethod(), interfaceSimpleOtherMethod());
		assertThat(convertingMethodInvoker.convertResult(new SimpleOtherObject("value")), instanceOf(SimpleOtherInterface.class));
		assertThat(((SimpleOtherInterface) convertingMethodInvoker.convertResult(new SimpleOtherObject("value"))).getString(), equalTo("value"));
	}

	private Method staticMethod() throws NoSuchMethodException {
		return WithMethod.class.getDeclaredMethod("staticMethod", int.class);
	}

	private Method staticException() throws NoSuchMethodException {
		return WithMethod.class.getDeclaredMethod("staticException", int.class);
	}

	private Method staticLongMethod() throws NoSuchMethodException {
		return ForSimpleObject.class.getDeclaredMethod("longMethod", long.class, String.class);
	}

	private Method interfaceLongMethod() throws NoSuchMethodException {
		return InterfaceForSimpleObject.class.getDeclaredMethod("longMethod", long.class, String.class);
	}

	private Method staticSimpleObjectMethod() throws NoSuchMethodException {
		return ForSimpleObject.class.getDeclaredMethod("simpleObjectMethod", SimpleObject.class);
	}

	private Method interfaceSimpleObjectMethod() throws NoSuchMethodException {
		return InterfaceForSimpleObject.class.getDeclaredMethod("simpleObjectMethod", SimpleObjectInterface.class);
	}

	private Method staticSimpleOtherMethod() throws NoSuchMethodException {
		return ForSimpleOther.class.getDeclaredMethod("simpleObjectMethod", SimpleOtherObject.class);
	}

	private Method interfaceSimpleOtherMethod() throws NoSuchMethodException {
		return InterfaceForSimpleOther.class.getDeclaredMethod("simpleObjectMethod", SimpleOtherInterface.class);
	}

	private SimpleObjectInterface simpleObjectInterface(final String s) {
		return new SimpleObjectInterface() {

			@Override
			public String getString() {
				return s;
			}

			@Override
			public void setString(String s) {
			}
			
		};
	}

	private SimpleOtherInterface simpleOtherInterface(final String s) {
		return new SimpleOtherInterface() {

			@Override
			public String getString() {
				return s;
			}

			@Override
			public void setString(String s) {
			}
			
		};
	}

	private static class WithMethod {
		private String staticMethod(int i) {
			return String.valueOf(i);
		}

		private String staticException(int i) throws IOException {
			if (i == 1) {
				throw new NullPointerException();
			} else {
				throw new IOException();
			}
		}

	}

	private static class ForSimpleObject {

		private Integer longMethod(long arg0, String arg1) {
			return (int) arg0 + Integer.parseInt(arg1);
		}

		private SimpleObject simpleObjectMethod(SimpleObject arg0) {
			return arg0;
		}

	}

	private static class ForSimpleOther {

		private SimpleOtherObject simpleObjectMethod(SimpleOtherObject arg0) {
			return arg0;
		}

	}

	interface InterfaceForSimpleObject {
		Integer longMethod(long arg0, String arg1);

		SimpleObjectInterface simpleObjectMethod(SimpleObjectInterface arg0);
	}

	interface InterfaceForSimpleOther {

		SimpleOtherInterface simpleObjectMethod(SimpleOtherInterface arg0);
	}

	interface SimpleObjectInterface {
		String getString();
		void setString(String s);
	}

	@Construct(SimpleObjectConstructorConfig.class)
	interface SimpleOtherInterface {
		String getString();
		void setString(String s);
	}

	private static class SimpleObject {
		private String string;
		
		public static SimpleObject build(String s) {
			SimpleObject simpleObject = new SimpleObject();
			simpleObject.string = s;
			return simpleObject;
		}
		
		public String getString() {
			return string;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((SimpleObject) obj).string.equals(string);
		}
	}

	private static class SimpleOtherObject {
		private String string;
		
		public SimpleOtherObject(String s) {
			this.string = s;
		}
		
		public String getString() {
			return string;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((SimpleOtherObject) obj).string.equals(string);
		}
	}

	private static class SimpleObjectConstructorConfig implements ConstructorConfig {

		@Override
		public Object[] arguments() {
			return new Object[]{"string"};
		}

		@Override
		public Class<?>[] signature() {
			return new Class<?>[]{String.class};
		}
		
	}
}
