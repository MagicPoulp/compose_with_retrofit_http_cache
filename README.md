# Author

Thierry Vilmart

#Date

December 2023

# Summary

A modern Android project using a Realm cache in an HTTP interceptor, using prefetching in parallel flows,
and with a unit test of the parallel flow with turbine.

Interesting in this project:
- A unit test of a flatMergeMap parallel flow using turbine
- Compose with paging3 is used to page the lazy loading of a LazyColomn.
- Realm is used in an HTTP interceptor to support an offline mode.
- We fetch data and present it in a list and we can tap to open the detail page.
- 2 API points are used, one for the list, and one for the detail.
The detail data is prefetched without delaying the display of the list
- Error handling is managed with a specific screen
- The result pattern design pattern is used.
- StateFlow is used to update the UI with observeAsState() in the compose component
(similar to LiveData but without observing on the UI thread)
- the clean architecture is used with used cases, and repositories
- Dependency injection is used with Hilt, in the app, and in the test
- Additional filtering of duplicates was added in the Pager because the API was sending duplicates.
