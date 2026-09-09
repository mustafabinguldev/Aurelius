# Aurelius Wiki

Welcome to the Aurelius project wiki. Aurelius is a lightweight Java web server built on Netty. It serves HTML pages, reusable HTML containers, and static media from an application directory, and it can be extended with Java addons that expose REST endpoints.

This wiki has two paths:

- **Application users** can start with [Getting Started](getting-started.md), then learn how to build [Pages and Media](pages-and-media.md).
- **Addon developers** should read [Addons and REST](addons-and-rest.md), then use [Architecture](architecture.md) to understand the request pipeline and extension boundaries.
- **Contributors** can use [Development](development.md) for the project layout, build commands, tests, and known constraints.

## What Aurelius does

Aurelius starts from the current working directory, which becomes the application directory. On first launch, it prepares the basic application structure and reads its YAML configuration and placeholders. The server then routes requests into three built-in areas:

| URL area | Purpose | Main audience |
| --- | --- | --- |
| `/` | HTML pages, including optional page CSS and JavaScript | Application users |
| `/public/...` | Static media files | Application users |
| `/api/...` | REST endpoints supplied by addons | Addon developers |

At a high level, a request enters the Netty HTTP server, is processed by an ordered request worker for its connection, and is dispatched to the page, media, or REST handler. The [Architecture](architecture.md) guide explains this flow and its concurrency guarantees in detail.

## Quick facts

- The Maven artifact is `Aurelius`, currently version `1.2-SNAPSHOT`.
- Building requires JDK 26 and Maven.
- `settings.yml` configures the listening port, I/O and request worker counts, queue capacity, and the optional JavaFX control panel.
- `placeholders.yml` supplies values that can be inserted into HTML pages.
- Addons are JAR files placed in `addons/`; each declares an entry class in `addon.yml`.

For the concise installation and usage reference, see the repository [README](../../README.MD). This wiki complements it with the why, constraints, and implementation-level details needed to build applications and addons confidently.

## Source map

| Area | Primary source |
| --- | --- |
| Process entry point | [`Main`](../../src/main/java/tech/bingulhan/webserver/Main.java) |
| Application bootstrap and configuration | [`AureliusApplication`](../../src/main/java/tech/bingulhan/webserver/app/AureliusApplication.java) |
| HTTP request handling | [`HttpRequestHandler`](../../src/main/java/tech/bingulhan/webserver/server/handler/HttpRequestHandler.java) |
| Root, media, and REST dispatch | [`NettyResponseType`](../../src/main/java/tech/bingulhan/webserver/response/NettyResponseType.java) |
| Maven build configuration | [`pom.xml`](../../pom.xml) |
