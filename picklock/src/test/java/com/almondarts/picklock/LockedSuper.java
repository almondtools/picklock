package com.almondarts.picklock;

@SuppressWarnings("unused")
public class LockedSuper {
	
	private double superField;
	
	private double superMethod() {
		return superField * 2 + 5;
	}

}
