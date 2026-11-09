# Changelog

All notable changes to this project are documented in this file.

## [4.0.1] - 2026-11-09

A bug-fix release for collections and `find()`, plus dependency and release
housekeeping.

### Fixes

- A child collection is no longer treated as a key. PSJOA derives a property's
  Key, Required, Prompt, alternate-search-key and list-box flags from field
  metadata that a collection doesn't have, so on collections those flags are
  noise that changes from one read to the next. When a collection came back as a
  key, `set()` could never match the rows already in a scroll: it deleted them
  and inserted new ones, which left an extra row after `create()` and made the
  save fail. These flags are now ignored on collections.
- `find()` (and `search()` in Node.js) works. It used to cast the result of Find
  to `Boolean` and always threw `ClassCastException`; it now reads the
  `CICollection` PSJOA returns, and a find with no matches yields no rows.
- `toJSON()` after a `get()` or `create()` returns that record, even when the
  same CI was used for a `find()` earlier. Reading the find rows without calling
  `find()` first now throws a `PssdkException`.

### Other changes

- Publications are signed, as Maven Central requires.
- The Node.js wrapper requires `piscina` `^5.3.2`, which fixes the
  prototype-pollution issue GHSA-67c8-pqhq-4rmx.
- The release process is documented in `RELEASING.md`.

For a full list of changes, see the [compare view][4.0.1].

## [4.0.0] - 2026-09-11

This is a breaking release mainly because of the separation of `cancel()` and
`close()`. There is also a major performance improvement in how property
metadata is cached, as caching reduces the number of calls to the PeopleSoft
application server. We saw some jobs calling CIs with hundreds of fields
running **40x faster** after this change. We worked on making this usable beyond
GraalVM technologies, focusing on Lucee and Kotlin. The `execute()` method is
also added to allow invoking non-standard CI operations.

### Major Changes

- **Breaking.** `cancel()` and `close()` are now distinct operations. `cancel()`
  used to close the session for you; it now only invokes the standard CANCEL
  method and leaves the session open. Either replace a `cancel()` call with
  `close()`, or add `close()` after it — otherwise the session leaks. The
  try-with-resources form handles it:

  ```java
  try (CI ci = appServer.ciFactory("MY_CI_NAME")) {
      ci.cancel();
  }
  ```
- Lucee compatibility: an incoming `List<Map<String, Object>>` is accepted for
  collections, and properties are looped over so inter-property dependencies are
  met. Kotlin compatibility came for free.
- `CI.execute(operation, data)` invokes a non-standard CI operation and returns
  whatever the PeopleCode returns — a boolean, number, string, or `null`.
- `CI` implements `Closeable`, so it works with try-with-resources. `close()` is
  also exposed on the Node.js `ThreadableCI`.
- `CI.toProxyHashMap()` and `CiRow.toProxyHashMap()`, needed by GraalPy: Python
  reads a `ProxyHashMap` where JavaScript reads a `ProxyObject`.
- `PropertyInfoCatalog` reads and caches a CI's four property-info collections
  (properties, find keys, get keys, create keys) up front, so populating a CI no
  longer re-reads metadata per property.
- A collection carrying the alternate-search-key bit is no longer dropped from a
  GET. 

For a full list of changes, see the [compare view][4.0.0].

[4.0.1]: https://github.com/azusapacificuniversity/pssdk/compare/v4.0.0...v4.0.1
[4.0.0]: https://github.com/azusapacificuniversity/pssdk/compare/v3.0.0...v4.0.0
