# shop-rest

## Task 1 – First resource and server

Jersey creates a new `ProductResource` for every request, yet the class has no mutable fields (only a
`final` reference to `ProductRepository`) because the repository is one shared instance bound through
`AbstractBinder` and used by all request threads at the same time: any state stored in the resource would
be useless after the request, and all shared state must live in the thread-safe repository
(`ConcurrentHashMap` + `AtomicLong`).