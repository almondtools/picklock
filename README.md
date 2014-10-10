picklock
========
Picklock is about accessing private members of objects and classes with a typed interface.

Use cases
=========
* in many cases setters and getters are not helpful in public APIs. However unit testing a 
public API often requires access to the dependencies. Picklock enables you to unlock your API
in tests, but to keep it free from unwanted injection in use case scenarios
* in some cases one encounters mean classes that do not allow Injection of dependencies. Singleton
classes often do not allow to modify the provided Singleton or to instatiate a second instance of
the same type (otherwise it would not be Singleton). Picklock enables you to instantiate Singletons
and similar classes without using explicit Reflection in a virtually strongly-typed way.

...

