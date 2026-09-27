# React Applications

Aurelius can serve a React single-page application (SPA) built with Vite, Create React App, or any bundler that produces a static `index.html` plus assets. The React app owns the page routes, while `/api/...` addon endpoints and `/public/...` media keep working on the same server and port.

## Enabling React mode

Build your React project and copy the build output into a `react/` folder in the application directory:

```sh
npm run build              # Vite writes dist/, Create React App writes build/
cp -r dist/ /path/to/application-directory/react/
```

```text
application-directory/
├── settings.yml
├── react/
│   ├── index.html
│   ├── favicon.svg
│   └── assets/
│       ├── index-4f2a9c.js
│       └── index-b81d0e.css
├── public/
└── addons/
```

React mode is enabled automatically when `react/index.html` exists at startup. The console prints `React build found. Serving the single-page application from: ...`. Restart the server after adding or removing the folder. Rebuilding into an existing `react/` folder does not need a restart, because files are read from disk on every request.

## Routing

| Request | Result |
| --- | --- |
| `/api/...` | Addon REST endpoints, unchanged |
| `/public/...` | Static media, unchanged |
| An existing file in `react/`, e.g. `/assets/index-4f2a9c.js` | The file, with a content type based on its extension |
| A folder in `react/` containing `index.html` | That `index.html` |
| A missing path with a file extension, e.g. `/assets/missing.js` | `404 Not Found` |
| Any other path, e.g. `/`, `/users/42` | `react/index.html`, so client-side routers such as React Router work on reload and deep links |

In React mode, the React build replaces the HTML page handler: pages in `app/`, containers, placeholders, and the custom `app/404` page are not rendered. Do not use `api` or `public` as the first segment of a React route, because those segments are reserved.

React responses support `GET` and `HEAD`; other methods receive `405 Method Not Allowed` with `Allow: GET, HEAD`. Requests that try to leave the `react/` folder (for example with `..`) receive `404`.

## Caching

Files under `assets/` (Vite) and `static/` (Create React App) have content hashes in their names, so they are sent with `Cache-Control: public, max-age=31536000, immutable`. Everything else, including `index.html`, is sent with `Cache-Control: no-cache` so browsers pick up new deployments.

## Calling addon endpoints

Because the React app and the REST API share an origin, call endpoints with relative URLs and no CORS setup:

```js
const response = await fetch("/api/hello");
const data = await response.json();
```

During development, run the Vite dev server and proxy API calls to a running Aurelius instance:

```js
// vite.config.js
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: { "/api": "http://localhost:8080", "/public": "http://localhost:8080" },
  },
});
```

Keep Vite's default `base: "/"`, because Aurelius serves the React build at the site root.

## Source references

- [`NettyResponseReactHandler`](../../src/main/java/tech/bingulhan/webserver/response/impl/react/NettyResponseReactHandler.java) serves build files and the SPA fallback.
- [`ReactContentType`](../../src/main/java/tech/bingulhan/webserver/response/impl/react/ReactContentType.java) maps file extensions to content types.
- [`AureliusApplicationData`](../../src/main/java/tech/bingulhan/webserver/app/AureliusApplicationData.java) detects `react/index.html` at load time.
- [`NettyResponseHandler`](../../src/main/java/tech/bingulhan/webserver/response/NettyResponseHandler.java) dispatches non-API, non-media requests to the React handler when React mode is enabled.
