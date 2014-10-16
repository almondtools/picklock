Picklock
========
Picklock (meaning a duplicate key) is about accessing private members of java objects and classes utilizing a strongly typed interface.
Picklock uses Java Reflection and Java Proxys to create an unlocked Facade of an Object or a Class.   

The Objectives of Picklock follow her:

Picklocking a sealed class
==========================
How would one access a private method like
```Java
public class Foo {

	public void bar(String text) {
		...
	}
}
```

The default way is:
```Java
	public void callBar(Foo foo, String text)  throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<? extends Foo> fooClass = foo.getClass();
		Method bar = fooClass.getDeclaredMethod("bar", new Class<?>[]{String.class});
		bar.setAccessible(true);
		bar.invoke(foo,new Object[]{text});
	}
```

The picklock style would be following
```Java
	public void callBar(Foo foo, String text) throws NoSuchMethodException {
		UnlockedFoo unlockedFoo = ObjectAccess.unlock(foo).features(UnlockedFoo.class);
		unlockedFoo.bar(text);
	}
	
	interface UnlockedFoo {
		void bar(String text);
	}
```

* No need to lookup the class
* No need to lookup the method
* No need to wrap argument types and arguments
* No need to enable invocations
* No need to invocate in a non-object-oriented way

Snooping Private State
======================
...

Changing Private State
======================
...

Instantiating Singletons
========================
...

Controlled Private Access
=========================
The worst (non-prototypic) application of reflection I ever saw was a web application encoding reflective calls in the URL-Query-Params of a HTTP-Request, e.g. 

[http://foo.bar?object=x&method=foo&arg1=bar&arg2=null](#)

would call reflectively 

```Java
x.foo(bar)
```

The creator might have thought: Really cool code - the maintainer would certainly disagree. Such an interface is really flexible, but a huge security leak, because it allows **Random Access** to any private member. 

Utilizing the metaphor: This is not a picklocker which tries to sneak in an find a certain information, this is a burglar breaking the door, and breaking lots of glass and furniture.

Picklock uses the concept of **Controlled Private Access**. Any Member that should be accessed must be defined in the UnlockedFacade object. Any Member not defined in such a Facade object is not accessible.

Maintaining and Tracking Picklocked Objects
===========================================
A main Problem of Java Reflection is, that it fails at runtime, not at compile time and not at build time. This makes reflection unpredictable and risky. 

Picklock attempts will probably never fail at compile time, but some coding discipline is sufficient, to let picklock fail at build time (i.e. at test time). What is to be done:
* determine any pair of types (locked class, unlocked facade class)
* write a test containing only "ObjectAccess.check(foo.getClass()).isUnlockable(UnlockedFoo.class);"
* this test would always fail, if the internals used by Picklock do not exist any more (e.g. because the foreign implementation changed)


Now why should we use this?
===========================
Critics may mention that Picklock lets you work around bad code. But we all know that it is better to write code in a good wayrather than finding methods to work around it. **Yes and No!**

I see some scenarios of application:

1. Sometimes we have to come up with foreign code that we cannot easily change. Perhaps it was not designed for TDD or dependency injection. In this case we have to access private members, and we can do this with picklock. The alternative would be to patch the code with getters and setters, which is not very rewarding.

2. Worse: We have to come up with legacy code (meaning code, that is not maintained any more). Legacy code has one positive property. Internals are usually not changed often (because that could break the whole system). So if the private members of a legacy code object is not reachable, we can make it reachable with picklock.

3. Better: In some cases we want to publish an API, which is well designed. If the developer follows the "Law of Demeter" and "Tell don't Ask" he might have been very restrictive in using getter and setter methods. However if he is a good developer he writes tested code and testing could be a problem if the private state is completely hidden. Now picklock enables him to access the private State in unit tests but prevents other users from depending on inner state of API objects.
