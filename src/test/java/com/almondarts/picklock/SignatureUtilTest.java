package com.almondarts.picklock;

import static com.almondarts.picklock.SignatureUtil.methodSignature;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;


public class SignatureUtilTest {

	@Test
	public void testMethodSignatureOnlyName() throws Exception {
		assertThat(methodSignature("method", String.class, new Class<?>[0], new Class<?>[0]), equalTo("String method()"));
	}
	
	@Test
	public void testMethodSignatureNameAndException() throws Exception {
		Class<?>[] exceptions = {IOException.class};
		assertThat(methodSignature("method", String.class, new Class<?>[0], exceptions), equalTo("String method() throws IOException"));
	}

	@Test
	public void testMethodSignatureWithParameters() throws Exception {
		Class<?>[] classes = {String.class, int.class};
		assertThat(methodSignature("method", String.class, classes, new Class<?>[0]), equalTo("String method(String,int)"));
	}

}
