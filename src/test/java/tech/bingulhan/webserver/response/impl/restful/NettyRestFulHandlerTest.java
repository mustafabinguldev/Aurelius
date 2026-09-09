package tech.bingulhan.webserver.response.impl.restful;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.restful.RestFulResponseHelper;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NettyRestFulHandlerTest {
    private final List<RestFulResponseStructure> routes = new ArrayList<>();
    private AureliusApplication application;

    @Before
    public void setUp() {
        // Avoid application startup, which binds a server and loads disk addons.
        application = mock(AureliusApplication.class);
        when(application.getRestFulResponseStructures()).thenReturn(routes);
    }

    @Test
    public void missingRouteReturns404() {
        assertResponse("/api/missing", HttpMethod.GET, 404, "404", null);
    }

    @Test
    public void routePrefixMustEndAtSegmentBoundary() {
        route("users", RestFulRequestType.GET, "users");
        assertResponse("/api/usersExtra", HttpMethod.GET, 404, "404", null);
    }

    @Test
    public void samePathCanHaveMultipleMethods() {
        route("users", RestFulRequestType.GET, "get");
        route("users", RestFulRequestType.POST, "post");
        assertResponse("/api/users", HttpMethod.POST, 200, "\"post:\"", null);
    }

    @Test
    public void unsupportedMethodReturns405AndAllow() {
        route("users", RestFulRequestType.POST, "post");
        route("users", RestFulRequestType.GET, "get");
        assertResponse("/api/users", HttpMethod.DELETE, 405, "", "GET, POST");
    }

    @Test
    public void nestedRouteReceivesOnlyRemainingSegments() {
        route("users", RestFulRequestType.GET, "parent");
        route("users/profile", RestFulRequestType.GET, "profile");
        assertResponse("/api/users/profile/42/details", HttpMethod.GET, 200,
                "\"profile:42/details\"", null);
    }

    @Test
    public void exactRouteReceivesNoPathData() {
        route("users/profile", RestFulRequestType.GET, "profile");
        assertResponse("/api/users/profile", HttpMethod.GET, 200, "\"profile:\"", null);
    }

    @Test
    public void methodMismatchDoesNotFallBackToParentRoute() {
        route("users", RestFulRequestType.POST, "parent");
        route("users/profile", RestFulRequestType.GET, "profile");
        assertResponse("/api/users/profile", HttpMethod.POST, 405, "", "GET");
    }

    private void route(String root, RestFulRequestType method, String result) {
        routes.add(new RestFulResponseStructure.Builder(root).setRequestType(method)
                .setRestFulResponse(new RestFulResponseStructure.RestFulResponse<String, Object>() {
                    public Object convert(String body) { return null; }
                    public String response(Object body, RestFulResponseHelper helper) {
                        return result + ":" + String.join("/", helper.getPathData());
                    }
                }).build());
    }

    private void assertResponse(String path, HttpMethod method, int status, String body, String allow) {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, path);
        FullHttpResponse response = null;
        try {
            RequestStructure structure = new RequestStructure();
            structure.setRoot(path);
            structure.setMethod(method.name());
            new NettyRestFulHandler().handleResponse(new NettyResponseService(application, request,
                    channel.pipeline().firstContext()), structure);
            response = channel.readOutbound();
            assertNotNull(response);
            assertEquals(status, response.status().code());
            assertEquals(body, response.content().toString(CharsetUtil.UTF_8));
            assertEquals(allow, response.headers().get(HttpHeaderNames.ALLOW));
            assertEquals(response.content().readableBytes(), HttpUtil.getContentLength(response));
            assertNull(channel.readOutbound());
        } finally {
            if (response != null) response.release();
            request.release();
            channel.finishAndReleaseAll();
        }
    }
}
