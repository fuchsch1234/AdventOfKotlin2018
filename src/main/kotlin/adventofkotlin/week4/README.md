# Advent of Kotlin Week 4

My implementation for the challenge of [week 4](https://blog.kotlin-academy.com/advent-of-kotlin-week-4-mocking-cde699ec9963).

## Implementation

Javas Proxy class is used to create mock implementation of interfaces.

To add mock function bodies to a mock object the functions `setReturnValue` and `setBody` can be used. 

Both functions take a lambda as their first parameter that should invoke a function from the mocked interface and a lambda or a simple return value as the mock function body.
These functions register the class of the first lambda (which is unique) with all mock objects. Then the lambda is invoked and calls one of the mocks functions.
The mock class then checks the classnames of all functions in its current call stack for the registered classname.
If it is found the mock body is associated with the currently called function.
