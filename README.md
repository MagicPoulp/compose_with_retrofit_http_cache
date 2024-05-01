# Author

Thierry Vilmart

April 2024

# Summary

A modern Android project using a cache of GET requests via retrofit, somewhat like browsers do.

# Assignment's questions

- give us your context at the time you did this skills test. Were you stressed/relaxed, under some constraints?

I was relaxed during a public holiday.

- present your work; explain its architecture, main components and how they interact with each other. Feel free to include diagrams as appropriate. Hand-drawn is fine.

Classic :

A classic clean Architecture is used with use cases, and a repository.
One view model for the state with a loader
One view model for the Users List.
Data is fetched using retrofit and the JSON is parsed. Jetpack component Paging3 is used to load the page lazily.

Innovation :

The cache works like a browser for GET requests. It is better than what I did in another repo using REALM and all requests. BEcause we follow the HTTP specification on caching GET requests only,
and using retrofit to follow the Cache-Control header.
I added a mechanism to detect internet connectivity. In addition, since it is not immediate and not 100% reliable, we use a socket and try directly in case of a timeout.

- explain where you applied some developers' good practices: design pattern, SOLID, KISS, DRY principles, etc...

The result pattern, and the use case screen pattern are used to show screens of error, loading, or content.
I use the clean architecture with different view models and use cases.
I already have a git repository with a sofisticated and optimized prefetching of detailed data. Here I wanted to be as simple as possible.
I in SOLID, we need many interfaces to fetch the data. Hence, a potential need for detailed user data would need a new API call.

An important part is dedicated to detecting no internet in real time, and also rechecking internet to move beteen the cache and the non-cached data.

- explain your development strategy:

I reused an old project to explore more unknown things like the caching, and reuse what I already know well (compose, retrofit, paging3).

    - if you favored some functionalities or layers

I favored a cache using Retrofit instead of a cache using a database. Because the way the cache works for browsers is commendable.
I did not use a library for caching (https://github.com/ncornette/OkCacheControl) because it causes a security risk on a sensitive part.

    - your commits strategy

There are commits of 2 kings, doc commits and big fundamental steps.
However, I am used to follow standard prefixes, such as build, bump, ci, deps(add|change|remove), docs, enabler, feat(add|change|remove), fixbug, refactor, test.

- explain if your code is future-proof (scalable, robust to changes, etc...)

Following the clean architecture makes the project robust to change.
The setup is in place (see NavigationScreen) to navigate across multiple pages

