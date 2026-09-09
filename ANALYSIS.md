# Project Review — September 9, 2026

Review scope: Maven build, HTTP routing, HTML and media responses, application startup, YAML parsing, and addon loading. This work is not a full security audit or load test.

## Fixed Issues

| Issue | Outcome | Evidence |
| --- | --- | --- |
| Missing REST endpoints returned HTTP 200 | Returns HTTP 404 | `NettyRestFulHandlerTest#missingRouteReturns404` |
| `/api/usersExtra` matched the `users` endpoint | Matching checks path-segment boundaries | `NettyRestFulHandlerTest#routePrefixMustEndAtSegmentBoundary` |
| A GET registration could shadow a POST registration at the same path | Selects by HTTP method among registrations at the most specific path | `NettyRestFulHandlerTest#samePathCanHaveMultipleMethods` |
| Unsupported methods returned HTTP 400 | Returns HTTP 405 with an `Allow` header listing supported methods | `NettyRestFulHandlerTest#unsupportedMethodReturns405AndAllow` |
| Part of a nested endpoint was included in the parameters | Parameters contain only the segments after the matched endpoint | `NettyRestFulHandlerTest#nestedRouteReceivesOnlyRemainingSegments` |
| Default and custom HTML 404 pages returned HTTP 200 | Both error responses return HTTP 404 | `NettyResponseMvcHandlerTest` |
| `.gitignore` excluded all test directories | Tests can be included in version control; JUnit and Mockito test dependencies were added | `.gitignore`, `pom.xml`, `src/test/java` |

The REST router now uses the application provided by `NettyResponseService` instead of the global application. If the most specific path does not support the method, it does not fall back to a parent endpoint; a separate regression test covers this behavior.

## Remaining Findings and Priorities

The following findings came from source code review and were not fixed in this change.

| Priority | Finding and Impact | Source / Suggested Work |
| --- | --- | --- |
| High | The temporary `ByteBuf` created for REST responses is not released after `writeBytes(buf)`. Repeated requests risk accumulating reference-counted resources. | The four handlers under `src/main/java/tech/bingulhan/webserver/response/impl/restful/impl/`; write directly to the response buffer and add resource lifecycle tests. |
| High | Entire media files are read synchronously on the Netty request thread. Large files may increase memory usage and delay other requests. | `NettyResponseMediaHandler#handleResponse`; streaming transfers and concurrent large-file tests. |
| Medium | The `threadSize` setting is read, but the worker count is calculated from the processor count. | `AureliusApplication#readSettingsYml`, `HttpNettyServer#start`; configuration validation and lifecycle tests. |
| Medium | YAML input streams are not closed; missing or incorrectly typed settings are cast directly. | `AureliusApplication#readYaml`, `readSettingsYml`; try-with-resources and invalid configuration tests. |
| Medium | Addon loading errors are silently swallowed; classloader and YAML stream closure are not managed. | `FileAddonCompiler#doCompileAllAddons`, `registerAddon`; cleanup tied to the addon lifecycle and error reporting. |
| Medium | `shutdown()` terminates the entire JVM; the interrupt flag is not restored; executor cleanup is incomplete on startup failure. | `HttpNettyServer`; tests for embedded usage, startup failures, and shutdown. |
| Medium | Lombok and annotations use `RELEASE` versions; the same source may build against different dependencies in the future. | `pom.xml`; pin versions and scan dependencies for vulnerabilities. No current CVE scan was performed. |
| Medium | The README states Java 8+, but the POM uses JavaFX 17; Java 8 compatibility has not been verified. | `README.MD`, `pom.xml`; CI with a supported JDK matrix. JDK 17 was used for this work. |

## Verification

- The baseline build passed with JDK 17 and Maven 3.8.5; there were initially no tests.
- Before the REST fixes, 6 of 7 tests failed due to the expected behavioral differences.
- Before the HTML fixes, 2 of 3 tests failed with `expected 404 but was 200`.
- After the fixes, Maven `verify` passed: 10 tests, 0 errors, 0 failures; the JAR was packaged.
- Tests inspect actual handler responses through Netty `EmbeddedChannel`. Application startup and disk access are mocked; live sockets, the JavaFX UI, addon integration, and behavior under load were not tested.

Local execution: `mvn test` with JDK 17; `mvn verify` for packaging.
Maven was not on PATH in this environment; the existing `.m2/wrapper/dists` installation was used.
