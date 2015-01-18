package com.almondtools.picklock;



public class LockedObject extends LockedSuper {

	private String myField;
	
	private int myMethod(String string, boolean flag) {
		return flag ? Integer.parseInt(string) : 0;
	}

	public int myPublicMethod() {
		try {
			return myMethod(myField, true);
		} catch (Exception e) {
			return myMethod(myField, false);
		}
	}
	
}
