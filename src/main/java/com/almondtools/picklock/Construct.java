package com.almondtools.picklock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Construct {

	Class<? extends ConstructorConfig> value();
}
