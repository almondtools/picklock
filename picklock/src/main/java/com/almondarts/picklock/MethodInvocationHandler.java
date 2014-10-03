package com.almondarts.picklock;


public interface MethodInvocationHandler {

	Object invoke(Object object, Object[] args) throws Throwable;
}
