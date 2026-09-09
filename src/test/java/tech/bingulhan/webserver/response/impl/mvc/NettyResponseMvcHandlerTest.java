package tech.bingulhan.webserver.response.impl.mvc;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.junit.Test;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.AureliusApplicationData;
import tech.bingulhan.webserver.app.mvc.PageStructure;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NettyResponseMvcHandlerTest {
    @Test
    public void missingPageReturns404() {
        check(null, 404, "404");
    }

    @Test
    public void customErrorPageKeeps404Status() {
        check("/404", 404, "custom page");
    }

    @Test
    public void existingPageReturns200() {
        check("/requested", 200, "custom page");
    }

    private void check(String registeredPath, int status, String body) {
        AureliusApplication app = mock(AureliusApplication.class);
        AureliusApplicationData data = mock(AureliusApplicationData.class);
        when(app.getData()).thenReturn(data);
        HashMap<String, PageStructure> pages = new HashMap<>();
        if (registeredPath != null) {
            PageStructure page = new PageStructure(registeredPath,
                    "<html><head></head><body>custom page</body></html>", "", "");
            pages.put(registeredPath, page);
        }
        when(data.getPages()).thenReturn(pages);
        when(data.getContainerStructures()).thenReturn(Collections.emptyList());
        when(data.getPlaceholders()).thenReturn(new HashMap<>());
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/requested");
        FullHttpResponse response = null;
        try {
            RequestStructure structure = new RequestStructure();
            structure.setRoot("/requested");
            new NettyResponseMvcHandler().handleResponse(
                    new NettyResponseService(app, request, channel.pipeline().firstContext()), structure);
            response = channel.readOutbound();
            assertNotNull(response);
            assertEquals(status, response.status().code());
            assertTrue(response.content().toString(CharsetUtil.UTF_8).contains(body));
            assertEquals(response.content().readableBytes(), HttpUtil.getContentLength(response));
        } finally {
            if (response != null) response.release();
            request.release();
            channel.finishAndReleaseAll();
        }
    }
}
