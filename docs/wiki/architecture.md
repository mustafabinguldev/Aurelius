# Architecture

Aurelius separates application discovery, Netty I/O, request work, and response dispatch. The separation keeps blocking or application-level work away from the server's I/O event loops while preserving HTTP response order on each connection.

## Startup flow

```text
Main
  └─ current working directory
      └─ AureliusApplication
          ├─ AureliusApplicationData
          │   ├─ paths and first-run files
          │   ├─ pages, containers, and media
          │   └─ addon discovery and enablement
          ├─ settings.yml
          ├─ placeholders.yml
          └─ HttpNettyServer
```

`Main` passes `System.getProperty("user.dir")` to `AureliusApplication`. Application data creates the first-run file structure, discovers pages, containers, direct media files, and addons. The application then reads server settings and placeholder values before starting the Netty server.

## HTTP pipeline

Each accepted connection receives this pipeline:

```text
HttpServerCodec
  → HttpObjectAggregator (1 MiB maximum body)
  → HttpRequestHandler
      → NettyResponseHandler
          ├─ page handler
          ├─ media handler
          └─ REST handler
```

`HttpRequestHandler` rejects a failed HTTP decode or a URI that does not begin with `/` with `400 Bad Request`, then closes the connection. For valid requests, it removes the query string for route selection and builds a `RequestStructure` containing the method and path.

The response dispatcher selects a handler from the first path segment:

| First segment | Handler |
| --- | --- |
| `public` | Static media handler |
| `api` | Addon REST handler |
| anything else, including no segment | Page handler |

## Concurrency model

`HttpNettyServer` creates three executor groups:

| Group | Responsibility | Configuration |
| --- | --- | --- |
| Boss event loop | Accept incoming connections | Always one thread |
| I/O worker event loop | Netty socket I/O | `server.threadSize` |
| Request executor group | Application request handling | `server.requestThreadSize` and `server.maxPendingRequests` |

The server assigns one executor from the request group to each new connection. `HttpRequestHandler` retains the incoming Netty request before crossing the asynchronous boundary and releases it when handling ends. That per-connection executor preserves the order of responses to HTTP-pipelined requests.

The request group uses a bounded queue and rejects work when it is full. On rejection, Aurelius releases the request and closes the connection instead of returning an immediate error response: an immediate response could overtake a response already queued for the same connection.

## Response behavior

| Response family | Main behavior | Connection behavior |
| --- | --- | --- |
| Page | Expands containers and placeholders; injects optional CSS and JavaScript | Closes after response |
| Media | Sends supported direct `public/` file with a Netty file region | Closes after response or `HEAD` headers |
| REST | Invokes the addon controller and serializes the return value as JSON | Honors HTTP keep-alive |

The different connection behavior is deliberate. Do not assume page or media requests can reuse a connection; REST clients may reuse one when they send the normal keep-alive headers.

## Shutdown

`AureliusApplication.stop()` delegates to the Netty server. The server tracks accepted channels and shuts down its channel group and executor groups gracefully. Server shutdown is not implemented by terminating the JVM directly, allowing callers such as the JavaFX panel or tests to retain process control.

## Extension boundaries

- The application directory is the content boundary for pages, containers, media, configuration, and addon JARs.
- The page, media, and REST handlers form the three built-in request boundaries.
- `AddonManager` is the public extension boundary for addon routes, pages, placeholders, and reloads.
- `NettyResponseService` carries the application, full Netty request, and channel context across handlers.

## Source references

- [`Main`](../../src/main/java/tech/bingulhan/webserver/Main.java) and [`AureliusApplication`](../../src/main/java/tech/bingulhan/webserver/app/AureliusApplication.java) implement startup.
- [`HttpNettyServer`](../../src/main/java/tech/bingulhan/webserver/server/HttpNettyServer.java) defines executor groups, the HTTP pipeline, and shutdown.
- [`HttpRequestHandler`](../../src/main/java/tech/bingulhan/webserver/server/handler/HttpRequestHandler.java) moves requests to the ordered executor.
- [`NettyResponseHandler`](../../src/main/java/tech/bingulhan/webserver/response/NettyResponseHandler.java) selects the page, media, or REST boundary.
- [`NettyResponseService`](../../src/main/java/tech/bingulhan/webserver/response/NettyResponseService.java) carries request context.
