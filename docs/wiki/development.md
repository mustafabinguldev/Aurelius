# Development

This guide is for contributors changing Aurelius itself. The project is a Maven-based Java 26 application with Netty for HTTP, JavaFX for the optional desktop panel, SnakeYAML for configuration, Jackson for REST JSON, Jsoup for HTML processing, Lombok, JUnit 4, and Mockito.

## Repository layout

```text
.
├── pom.xml
├── README.MD
├── CONTRIBUTING.md
├── src/
│   ├── main/
│   │   ├── java/tech/bingulhan/webserver/
│   │   │   ├── Main.java
│   │   │   ├── app/             # configuration, content discovery, addons, UI
│   │   │   ├── response/        # request dispatch and response handlers
│   │   │   └── server/          # Netty bootstrap and inbound handler
│   │   └── resources/
│   └── test/java/tech/bingulhan/webserver/
│       ├── app/                 # settings validation
│       ├── response/            # media, MVC, REST, JSON behavior
│       └── server/              # server lifecycle and request handler
└── docs/wiki/                   # this source-driven wiki
```

## Build, test, and package

Run these commands from the repository root with JDK 26 selected:

```sh
mvn test
mvn verify
```

`mvn test` runs the JUnit suite. `mvn verify` runs the full Maven verification lifecycle and invokes the shade plugin to assemble the runnable artifact. The project uses the Maven compiler plugin with release 26; a lower JDK cannot compile this source version.

The current tests cover:

- server thread selection and graceful termination;
- request offloading, reference counting, and rejection behavior;
- server setting validation;
- page and custom-404 responses;
- direct media transfer, `GET`, `HEAD`, and method errors;
- REST root selection, methods, status codes, and trailing path data; and
- JSON response and error handling.

When changing a behavior in one of these areas, add or update the focused regression test in the corresponding package.

## Local smoke test

After packaging, use a temporary application directory rather than the repository root:

```sh
mkdir aurelius-demo
cd aurelius-demo
mkdir public
```

Create `app/main.html` through the normal first-run sequence, add a simple page, then run the built JAR from this directory. Verify:

1. `/` returns the page and HTTP `200`.
2. an unknown page returns HTTP `404`.
3. `/public/<supported-file>` supports `GET` and `HEAD`.
4. an installed addon endpoint returns JSON and the expected status.

For changes to `HttpRequestHandler` or `HttpNettyServer`, also exercise more than one request on the same HTTP connection. Ordering and resource ownership are intentional parts of that design.

## Contribution workflow

The repository's [contribution guide](../../CONTRIBUTING.md) asks contributors to fork and clone the project, branch from `master`, add documentation and tests for new behavior, run checks before committing, and open a pull request to `master` with a clear description and linked issues where applicable.

Keep changes small and focused. The source has clear responsibilities—application loading, Netty server bootstrap, request dispatch, and response family handlers—and an unrelated refactor makes regression behavior harder to assess.

## Current constraints and open work

- The project targets JDK 26. The optional JavaFX UI and non-Windows environments need broader verification.
- Media discovery is intentionally non-recursive and accepts only the extensions listed in [Pages and Media](pages-and-media.md).
- Addon loading skips JAR failures without surfacing the cause; addon class-loader cleanup and error reporting remain open work.
- REST registration currently prevents multiple methods from sharing one root.
- The root README records that no dependency security audit or concurrent large-file load test was performed for the current update.

## Source references

- [`pom.xml`](../../pom.xml) defines build plugins, compiler release, dependencies, and test runner.
- [`src/test/java`](../../src/test/java) contains the regression suite.
- [`CONTRIBUTING.md`](../../CONTRIBUTING.md) defines the upstream contribution workflow.
- [`README.MD`](../../README.MD) records release notes and remaining validation work.
