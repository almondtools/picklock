package com.almondtools.picklock;

public interface ConstructorConfig {

	Object[] arguments();
	Class<?>[] signature();
}
