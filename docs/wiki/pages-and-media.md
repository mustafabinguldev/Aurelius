# Pages and Media

Aurelius turns files below the application directory into HTML routes and a small static-media surface. It discovers those files when application data is loaded, so restart the server after changing the file layout unless you explicitly use the addon reload API.

## Page layout and routes

The home page is `app/main.html`, mapped to `/`. Each nested directory containing `page.html` becomes a route. `page.css` and `page.js` in the same directory are optional.

```text
app/
├── main.html                 # /
├── main.css                  # optional styles for /
├── main.js                   # optional script for /
├── about/
│   ├── page.html             # /about
│   ├── page.css              # optional
│   └── page.js               # optional
├── products/
│   └── detail/
│       └── page.html         # /products/detail
└── 404/
    └── page.html             # optional custom 404 page
```

Only a directory with `page.html` is registered as a page. Nested directories are traversed recursively, and their names form the route path. A request for an unknown page returns HTTP `404`. If `app/404/page.html` exists, it is rendered as the error body **with HTTP 404**, rather than as a successful page.

Page CSS and JavaScript are embedded immediately before the page's closing `</head>` tag. Keep a closing `</head>` in every page that needs these companion files.

## Placeholders

Global placeholders are read from the top-level mapping in `placeholders.yml` and are addressed in HTML with percent signs:

```yaml
title: Aurelius demo
environment: local
```

```html
<title>%title%</title>
<p>Running in %environment%.</p>
```

The key `title` becomes `%title%`. Placeholder replacement happens while a page is rendered, so the values loaded during application initialization are available to every discovered page.

## Reusable HTML containers

Put a reusable fragment in `containers/<name>.html` and use it with a paired custom tag in a page.

`containers/button.html`:

```html
<button class="primary">{text}</button>
```

`app/main.html`:

```html
<container.button text="Continue"></container.button>
```

The `text` attribute replaces `{text}` in the fragment. Any unmatched `{word}` token is removed. Container names are the `.html` filenames without the extension. The current parser accepts double-quoted attributes whose names contain word characters; use that simple form for reliable substitution.

## Static media

Files directly inside `public/` are exposed at `/public/<filename>`:

```text
public/
├── logo.png        # /public/logo.png
├── intro.mp4       # /public/intro.mp4
└── data.json       # /public/data.json
```

Supported extensions and response content types are:

| Extensions | Content type |
| --- | --- |
| `jpeg`, `jpg` | `image/jpeg` |
| `png` | `image/png` |
| `mp4` | `video/mp4` |
| `yml`, `yaml` | `application/x-yaml` |
| `json` | `application/json` |

Only files immediately inside `public/` are discovered; nested subdirectories are not registered. Media supports `GET` and `HEAD`. Aurelius sends a `Content-Length` header and transfers a `GET` body through a Netty file region instead of loading the entire file into memory. Media responses close the connection after the transfer. Other HTTP methods receive `405 Method Not Allowed` with `Allow: GET, HEAD`.

## Routing boundary

The request dispatcher reserves `/public/...` for media and `/api/...` for REST. Any other path, including `/`, goes to the page handler. Avoid using `api` in a page route: the page loader intentionally does not register page paths that contain `api`.

## Source references

- [`AureliusApplicationData`](../../src/main/java/tech/bingulhan/webserver/app/AureliusApplicationData.java) discovers pages, containers, and direct `public/` files.
- [`NettyResponseMvcHandler`](../../src/main/java/tech/bingulhan/webserver/response/impl/mvc/NettyResponseMvcHandler.java) renders pages, placeholders, containers, and 404 responses.
- [`NettyResponseMediaHandler`](../../src/main/java/tech/bingulhan/webserver/response/impl/media/NettyResponseMediaHandler.java) serves media and implements `GET`/`HEAD` behavior.
- [`ResponseMediaType`](../../src/main/java/tech/bingulhan/webserver/response/impl/media/ResponseMediaType.java) defines the supported media types.
