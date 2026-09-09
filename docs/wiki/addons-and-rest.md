# Addons and REST

An Aurelius addon is a JAR loaded from the application directory's `addons/` folder. Addons can register REST endpoints, pages, and placeholders during startup. REST endpoints are available below `/api/`.

## Package an addon

Create `addons/` yourself and place addon JARs directly inside it. Aurelius scans only direct children whose filenames end in `.jar`.

Every addon JAR must include an `addon.yml` resource with its entry class:

```yaml
main: com.example.GreetingAddon
```

The entry class must:

- extend `tech.bingulhan.webserver.app.addon.Addon`;
- have an accessible no-argument constructor; and
- implement `getAddonName()`, `onEnable()`, and `onDisable()`.

```java
public final class GreetingAddon extends Addon {
    @Override
    public String getAddonName() {
        return "Greeting";
    }

    @Override
    public void onEnable() {
        // Register routes, pages, or placeholders here.
    }

    @Override
    public void onDisable() {
        // Release addon-owned resources here.
    }
}
```

During a data reload, Aurelius calls `onDisable()` on currently loaded addons, clears them, then scans and enables the JARs again. Call `AddonManager.reload()` to trigger that reload programmatically.

> **Current limitation:** a JAR that fails to load is skipped by the loader without reporting the underlying error. Check the manifest, entry-class name, constructor, and superclass first when an addon does not appear.

## Register a REST endpoint

Build a `RestFulResponseStructure` and register it from `onEnable()`:

```java
import tech.bingulhan.webserver.app.addon.AddonManager;
import tech.bingulhan.webserver.app.restful.RestFulResponseHelper;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.impl.restful.RestFulRequestType;

public void onEnable() {
    RestFulResponseStructure route = new RestFulResponseStructure.Builder("greeting")
        .setRequestType(RestFulRequestType.POST)
        .setRestFulResponse(new RestFulResponseStructure.RestFulResponse<Greeting, GreetingRequest>() {
            @Override
            public GreetingRequest convert(String bodyJson) throws Exception {
                return (GreetingRequest) AddonManager.convertFromBodyJson(bodyJson, GreetingRequest.class);
            }

            @Override
            public Greeting response(GreetingRequest request, RestFulResponseHelper helper) {
                return new Greeting("Hello, " + request.name());
            }
        })
        .build();

    if (!AddonManager.registerRestFulService(route)) {
        throw new IllegalStateException("The greeting route is already registered");
    }
}
```

This creates `POST /api/greeting`. The response object is serialized as JSON. The `convert` method receives the raw request body as UTF-8 text and must return the type consumed by `response`.

Supported request types are `GET`, `POST`, `PUT`, and `DELETE`. All currently use the JSON response handler, so `convert` is invoked even for a `GET`; make it tolerate an empty request body when appropriate.

## Routing rules

- A route root is supplied without `/api/`, for example `greeting`.
- Aurelius selects the longest matching root at a path-segment boundary. `users` matches `/api/users/42`; it does not match `/api/usersExtra`.
- Remaining segments are available through `helper.getPathData()`. For `/api/users/42`, a `users` route receives `42` as its first path value.
- The public registration API rejects a duplicate root **even if the HTTP method differs**. One addon route root can therefore currently represent only one method. Use distinct roots when that constraint matters.

## Responses, errors, and cookies

| Situation | Result |
| --- | --- |
| No matching route root | `404 Not Found` |
| Matching route but unsupported method | `405 Method Not Allowed`, with an `Allow` header |
| `convert` throws | `400 Bad Request` |
| Controller or JSON serialization throws | `500 Internal Server Error` |
| Successful controller call | `200 OK` JSON response |

`RestFulResponseHelper` exposes `getPathData()`, `getSocketAddress()`, and received cookies through `getReceivedCookies()`. Use `helper.sendCookie(...)` to add a cookie to one response. You can also provide route-level cookies with the builder's `setCookies(...)`; they are added to successful responses.

REST responses honor HTTP keep-alive when the client requests it. The page and media handlers follow a different, close-after-response model.

## Register pages and placeholders from an addon

`AddonManager` also exposes:

- `addPageData(PageStructure)` to add a page that does not collide with an existing page root;
- `addPlaceHolderData(String, Object)` to add a placeholder; and
- `reload()` to reload application data and addons.

Page and REST registration return `false` rather than replacing an existing page root or REST root. Treat `false` as a registration failure and avoid silently continuing. `addPlaceHolderData` has a different current behavior: it checks the unwrapped key but stores `%key%`, so a same-named placeholder can be overwritten instead of being rejected. Avoid registering a placeholder that is already defined in `placeholders.yml`.

## Source references

- [`Addon`](../../src/main/java/tech/bingulhan/webserver/app/addon/Addon.java) defines the addon lifecycle.
- [`FileAddonCompiler`](../../src/main/java/tech/bingulhan/webserver/app/addon/iml/FileAddonCompiler.java) scans JARs and loads `addon.yml`.
- [`AddonManager`](../../src/main/java/tech/bingulhan/webserver/app/addon/AddonManager.java) provides registration and reload APIs.
- [`RestFulResponseStructure`](../../src/main/java/tech/bingulhan/webserver/app/restful/RestFulResponseStructure.java) defines route construction and controller callbacks.
- [`NettyRestFulHandler`](../../src/main/java/tech/bingulhan/webserver/response/impl/restful/NettyRestFulHandler.java) resolves REST routes and statuses.
- [`JsonRestFulResponseHandler`](../../src/main/java/tech/bingulhan/webserver/response/impl/restful/JsonRestFulResponseHandler.java) handles JSON, cookies, and response writing.
