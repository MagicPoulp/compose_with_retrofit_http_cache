# Author

Thierry Vilmart

April 2024

# Summary

A modern Android project using a cache of GET requests via retrofit, somewhat like browsers do.

# Assignment's questions

- give us your context at the time you did this skills test. Were you stressed/relaxed, under some constraints?

I was relaxed during a public holiday.

- present your work; explain its architecture, main components and how they interact with each other. Feel free to include diagrams as appropriate. Hand-drawn is fine.



- explain where you applied some developers' good practices: design pattern, SOLID, KISS, DRY principles, etc...

I use the clean architecture with different view models and use cases.
I already have a git repository with a sofisticated and optimized prefetching of detailed data. Here I wanted to be as simple as possible.
I in SOLID, we need many interfaces to fetch the data. Hence, a potential need for detailed user data would need a new API call.

- explain your development strategy:

I reused an old project to explore more unknown things like the caching, and reuse what I already know well (compose, retrofit, paging3).

    - if you favored some functionalities or layers

I did not use a library for caching (https://github.com/ncornette/OkCacheControl) because it causes a security risk on a sensitive part.

    - your commits strategy

doc dommits and big fundamental steps

- explain if your code is future-proof (scalable, robust to changes, etc...)

Following the clean architecture makes the project robust to change.
