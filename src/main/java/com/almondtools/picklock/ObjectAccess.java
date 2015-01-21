package com.almondtools.picklock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * ObjectAccess is a Decorator for any object that should get a new public interface. Usage:
 * 
 * <strong>InterfaceOfTheDecorator unlocked = ObjectAccess.unlock(object).features(InterfaceOfTheDecorator.class);</strong>
 * 
 * after that the variable unlocked contains an object of type InterfaceOfTheDecorator, where each method is mapped according to the picklock conventions:
 * 
 * - void setProperty([sometype] t) => <strong>void setProperty([sometype] t)</strong> if exists or <strong>property = t;</strong> if there is a <strong>property static [sometype] property;</strong>
 * - [sometype] getProperty() => <strong>[sometype] getProperty()</strong> if exists or <strong>return property;</strong> if there is a <strong>property static [sometype] property;</strong>
 * - [booleantype] isProperty() => <strong>[booleantype] isProperty()</strong> if exists or <strong>return property;</strong> if there is a <strong>property static [booleantype] property;</strong>
 * - [return type] methodname([signature]) => <strong>static [return type] methodname([signature])</strong>
 * 
 * @author Stefan Mandel
 */
public class ObjectAccess extends InvocationResolver implements InvocationHandler {

	private Map<Method, MethodInvocationHandler> methods;
	private Object object;

	public ObjectAccess(Object object) {
		super(object.getClass());
		this.methods = new HashMap<Method, MethodInvocationHandler>();
		this.object = object;
	}
	
	public Object getObject() {
		return object;
	}

	/**
	 * wraps the given object. The result of this method will be decoratable with the new interface
	 * 
	 * @param type
	 *            the class to unlock/decorate
	 * @return the wrapped object
	 */
	public static ObjectAccess unlock(Object object) {
		return new ObjectAccess(object);
	}

	/**
	 * wraps the given class. The result of this method is a {@link ObjectSnoop} object which enables the user to check if a wrapped object (of the given class)
	 * could be target of a mapping from a specific interface. Note that a class (not an object) is wrapped, but the result will check the instance interface of this class
	 * (all non-static methods without constructors) not the static interface. static interfaces could be checked with {@link ClassAccess.check}. 
	 * 
	 * @param type
	 *            the target class to check
	 * @return the wrapped class
	 */
	public static ObjectSnoop check(Class<?> clazz) {
		return new ObjectSnoop(clazz);
	}

	/**
	 * maps the given interface to the wrapped object
	 * 
	 * @param interfaceClass
	 *            the given interface class (defining the type of the result)
	 * @return an object of the type of interfaceClass (mapped to the members of the wrapped object)
	 * @throws NoSuchMethodException
	 *             if a method of the interface class could not be mapped according to the upper rules
	 */
	@SuppressWarnings("unchecked")
	public <T> T features(Class<T> interfaceClass) throws NoSuchMethodException {
		for (Method method : interfaceClass.getDeclaredMethods()) {
			methods.put(method, findInvocationHandler(method));
		}
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocationHandler handler = methods.get(method);
		return handler.invoke(object, args);
	}

}
