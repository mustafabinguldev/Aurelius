package tech.bingulhan.webserver.response.impl.restful;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.junit.Test;
import tech.bingulhan.webserver.app.restful.RestFulResponseHelper;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.app.restful.cookie.CookieStructure;
import java.util.Collections;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;
import static org.junit.Assert.*;

public class RestFulJsonResponseTest {
    @Test
    public void serializationFailureReturns500ForEveryMethod() {
        for (RestFulRequestType method : RestFulRequestType.values()) {
            check(method, false, new Object(), 500);
        }
    }

    @Test
    public void conversionFailureReturns400ForEveryMethod() {
        for (RestFulRequestType method : RestFulRequestType.values()) {
            check(method, true, "unused", 400);
        }
    }

    @Test
    public void allMethodsSerializeUnicodeAndKeepCallbackHeaders() {
        for (RestFulRequestType method : RestFulRequestType.values()) {
            check(method, false, "Merhaba çğıöşü", 200);
        }
    }

    private void check(RestFulRequestType method, boolean conversionFails, Object value, int status) {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method.name()), "/api/test");
        FullHttpResponse response = null;
        RestFulResponseStructure route = new RestFulResponseStructure.Builder("test").setRequestType(method)
                .setRestFulResponse(new RestFulResponseStructure.RestFulResponse<Object, Object>() {
                    public Object convert(String body) {
                        if (conversionFails) throw new IllegalArgumentException("bad input");
                        return null;
                    }
                    public Object response(Object body, RestFulResponseHelper helper) {
                        CookieStructure cookie = new CookieStructure();
                        cookie.setCookieName("test");
                        cookie.setCookieValue("value");
                        cookie.setFeatures(Collections.emptyList());
                        helper.sendCookie(cookie);
                        return value;
                    }
                }).build();
        try {
            method.getHandler().handle(new NettyResponseService(null, request, channel.pipeline().firstContext()),
                    new RequestStructure(), route);
            response = channel.readOutbound();
            assertNotNull(method.name(), response);
            assertEquals(method.name(), status, response.status().code());
            assertEquals(response.content().readableBytes(), HttpUtil.getContentLength(response));
            if (status == 200) {
                assertEquals("\"Merhaba çğıöşü\"", response.content().toString(CharsetUtil.UTF_8));
                assertEquals("test=value;", response.headers().get(HttpHeaderNames.SET_COOKIE));
            }
            assertNull(channel.readOutbound());
        } finally {
            if (response != null) response.release();
            request.release();
            channel.finishAndReleaseAll();
        }
    }
}
