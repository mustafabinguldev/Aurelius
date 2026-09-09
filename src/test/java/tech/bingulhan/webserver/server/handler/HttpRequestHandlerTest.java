package tech.bingulhan.webserver.server.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.DefaultEventExecutor;
import org.junit.Test;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.restful.RestFulResponseHelper;
import tech.bingulhan.webserver.app.restful.RestFulResponseStructure;
import tech.bingulhan.webserver.response.impl.restful.RestFulRequestType;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpRequestHandlerTest {
    @Test(timeout = 10000)
    public void retainsRequestUntilOffloadedWorkCompletes() throws Exception {
        DefaultEventExecutor executor = new DefaultEventExecutor();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Thread> callbackThread = new AtomicReference<>();
        AureliusApplication app = mock(AureliusApplication.class);
        when(app.getRestFulResponseStructures()).thenReturn(Collections.singletonList(
                new RestFulResponseStructure.Builder("test").setRequestType(RestFulRequestType.GET)
                        .setRestFulResponse(new RestFulResponseStructure.RestFulResponse<String, Object>() {
                            public Object convert(String body) { return null; }
                            public String response(Object body, RestFulResponseHelper helper) {
                                callbackThread.set(Thread.currentThread());
                                entered.countDown();
                                try { release.await(5, TimeUnit.SECONDS); }
                                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                                return "ok";
                            }
                        }).build()));
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestHandler(app, executor));
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/api/test?q=1");
        FullHttpResponse response = null;
        try {
            channel.writeInbound(request);
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            assertNotSame(Thread.currentThread(), callbackThread.get());
            assertEquals(1, request.refCnt());
            release.countDown();
            executor.submit(() -> { }).sync();
            channel.runPendingTasks();
            response = channel.readOutbound();
            assertNotNull(response);
            assertEquals(200, response.status().code());
            assertEquals(0, request.refCnt());
        } finally {
            release.countDown();
            executor.shutdownGracefully(0, 1, TimeUnit.SECONDS).syncUninterruptibly();
            if (response != null) response.release();
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void rejectedRequestIsReleasedAndConnectionClosed() {
        DefaultEventExecutor executor = new DefaultEventExecutor();
        executor.shutdownGracefully(0, 1, TimeUnit.SECONDS).syncUninterruptibly();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestHandler(mock(AureliusApplication.class), executor));
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/api/test");
        try {
            channel.writeInbound(request);
            assertEquals(0, request.refCnt());
            assertFalse(channel.isActive());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
