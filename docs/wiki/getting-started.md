# Getting Started

This guide takes a new Aurelius installation from source checkout to a running local site. Aurelius is configured by the directory from which you start its JAR: that **current working directory** is the application directory, not the directory that contains the JAR.

## Prerequisites

- JDK 26
- Maven

The project declares Java release 26 in [`pom.xml`](../../pom.xml). Its Maven artifact version is currently `1.2-SNAPSHOT`.

## Build and run

From the repository root:

```sh
mvn verify
java -Dfile.encoding=UTF-8 -jar target/Aurelius-1.2-SNAPSHOT.jar
```

`mvn verify` runs the test lifecycle and produces the shaded application JAR. Use `mvn test` when you only want the test phase.

To serve a site from another directory, change into that directory before running the JAR:

```sh
cd path/to/my-site
java -Dfile.encoding=UTF-8 -jar path/to/Aurelius-1.2-SNAPSHOT.jar
```

Open `http://localhost:8080` after startup, unless you selected another port.

## First-run application directory

On first launch, Aurelius creates the minimum structure below. It creates `settings.yml`, `placeholders.yml`, `app/`, `app/main.html`, and `containers/`. Create `public/` and `addons/` yourself if your application needs them.

```text
my-site/
├── settings.yml
├── placeholders.yml
├── app/
│   └── main.html
├── containers/
├── public/              # create when needed
└── addons/              # create when needed
```

`main.html` is created empty, so a successful first launch does not automatically display a page. Add valid HTML, then restart Aurelius.

## Configure the server

Settings live under the `server` key in `settings.yml`:

```yaml
server:
  port: 8080
  threadSize: 0
  requestThreadSize: 0
  maxPendingRequests: 32
  ui: false
```

| Setting | Accepted values | Runtime behavior |
| --- | --- | --- |
| `port` | Integer from `0` to `65535` | Default `8080`; `0` lets the OS choose an available port. |
| `threadSize` | Integer from `0` to `1024` | Number of Netty I/O workers. `0` selects up to four workers from the processor count. |
| `requestThreadSize` | Integer from `0` to `1024` | Number of request workers. `0` selects between two and eight workers from the processor count. |
| `maxPendingRequests` | Integer from `16` to `65536` | Maximum queued tasks for each request worker. Default `32`. A rejected submission closes that connection. |
| `ui` | YAML boolean | Enables the optional JavaFX control panel. An omitted value means `false`; first-run generated settings use `true`. |

Invalid types or out-of-range integer values stop startup. Restart the process after changing settings; configuration is read during application initialization.

### Headless server

To prevent the JavaFX panel from opening, create `settings.yml` before the first launch, or change its generated value to:

```yaml
server:
  ui: false
```

## Next steps

- Build a site with [Pages and Media](pages-and-media.md).
- Add reusable server-side HTML fragments with containers and global values with placeholders.
- Expose application behavior through [Addons and REST](addons-and-rest.md).

## Source references

- [`Main`](../../src/main/java/tech/bingulhan/webserver/Main.java) selects the current working directory.
- [`AureliusApplicationPathData`](../../src/main/java/tech/bingulhan/webserver/app/AureliusApplicationPathData.java) creates the first-run files and directories.
- [`AureliusApplication`](../../src/main/java/tech/bingulhan/webserver/app/AureliusApplication.java) validates and applies settings.
- [`pom.xml`](../../pom.xml) defines the Java release, artifact version, tests, and shaded build.
