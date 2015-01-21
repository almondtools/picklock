package com.almondtools.picklock;

@Deprecated
public interface ConstructorConfig {

	Object[] arguments();
	Class<?>[] signature();
}
