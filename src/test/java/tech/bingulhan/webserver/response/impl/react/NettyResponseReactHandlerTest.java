package tech.bingulhan.webserver.response.impl.react;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.FileRegion;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.AureliusApplicationData;
import tech.bingulhan.webserver.app.AureliusApplicationPathData;
import tech.bingulhan.webserver.response.NettyResponseHandler;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NettyResponseReactHandlerTest {
    private static final String INDEX = "<!doctype html><div id=\"root\"></div>";
    private static final String SCRIPT = "console.log('react');";

    @Rule public TemporaryFolder temp = new TemporaryFolder();

    private File reactFolder;
    private AureliusApplication app;

    @Before
    public void setUp() throws Exception {
        reactFolder = temp.newFolder("react");
        Files.writeString(reactFolder.toPath().resolve("index.html"), INDEX);
        Files.createDirectories(reactFolder.toPath().resolve("assets"));
        Files.writeString(reactFolder.toPath().resolve("assets/index-abc123.js"), SCRIPT);
        Files.writeString(reactFolder.toPath().resolve("vite.svg"), "<svg/>");
        Files.writeString(temp.getRoot().toPath().resolve("secret.txt"), "secret");

        app = mock(AureliusApplication.class);
        AureliusApplicationData data = mock(AureliusApplicationData.class);
        AureliusApplicationPathData paths = mock(AureliusApplicationPathData.class);
        when(app.getData()).thenReturn(data);
        when(data.getPathData()).thenReturn(paths);
        when(data.isReactEnabled()).thenReturn(true);
        when(data.getPages()).thenReturn(new HashMap<>());
        when(paths.getReactFolder()).thenReturn(reactFolder);
    }

    @Test
    public void rootServesIndexHtml() throws Exception {
        Response response = get("/");
        assertEquals(200, response.status);
        assertEquals("text/html; charset=UTF-8", response.headers.get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals("no-cache", response.headers.get(HttpHeaderNames.CACHE_CONTROL));
        assertEquals(INDEX, response.body);
    }

    @Test
    public void clientSideRouteFallsBackToIndexHtml() throws Exception {
        Response response = get("/dashboard/settings?tab=profile");
        assertEquals(200, response.status);
        assertEquals(INDEX, response.body);
    }

    @Test
    public void hashedAssetIsServedWithLongCache() throws Exception {
        Response response = get("/assets/index-abc123.js");
        assertEquals(200, response.status);
        assertEquals("text/javascript; charset=UTF-8", response.headers.get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals("public, max-age=31536000, immutable", response.headers.get(HttpHeaderNames.CACHE_CONTROL));
        assertEquals(SCRIPT, response.body);
    }

    @Test
    public void rootLevelFileIsServed() throws Exception {
        Response response = get("/vite.svg");
        assertEquals(200, response.status);
        assertEquals("image/svg+xml", response.headers.get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals("no-cache", response.headers.get(HttpHeaderNames.CACHE_CONTROL));
    }

    @Test
    public void missingFileIsNotFoundInsteadOfIndex() throws Exception {
        assertEquals(404, get("/assets/missing.js").status);
    }

    @Test
    public void pathTraversalIsRejected() throws Exception {
        assertEquals(404, get("/../secret.txt").status);
        assertEquals(404, get("/%2e%2e/secret.txt").status);
    }

    @Test
    public void headReportsLengthWithoutBody() throws Exception {
        Response response = send(HttpMethod.HEAD, "/");
        assertEquals(200, response.status);
        assertEquals(INDEX.length(), Integer.parseInt(response.headers.get(HttpHeaderNames.CONTENT_LENGTH)));
        assertEquals("", response.body);
    }

    @Test
    public void rejectsUnsupportedMethod() throws Exception {
        Response response = send(HttpMethod.POST, "/");
        assertEquals(405, response.status);
        assertEquals("GET, HEAD", response.headers.get(HttpHeaderNames.ALLOW));
    }

    private Response get(String uri) throws Exception {
        return send(HttpMethod.GET, uri);
    }

    private Response send(HttpMethod method, String uri) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri);
        RequestStructure structure = new RequestStructure();
        structure.setMethod(method.name());
        structure.setUrl(uri);
        structure.setRoot(uri.split("\\?", 2)[0]);
        try {
            // Go through the dispatcher to verify that React builds replace the page handler.
            NettyResponseHandler.handle(new NettyResponseService(app, request, channel.pipeline().firstContext()), structure);
        } finally {
            request.release();
        }
        Response result = new Response();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        Object message;
        try {
            while ((message = channel.readOutbound()) != null) {
                try {
                    if (message instanceof HttpResponse) {
                        result.status = ((HttpResponse) message).status().code();
                        result.headers = ((HttpResponse) message).headers();
                    }
                    if (message instanceof HttpContent) {
                        ((HttpContent) message).content().getBytes(0, body, ((HttpContent) message).content().readableBytes());
                    }
                    if (message instanceof FileRegion) {
                        FileRegion region = (FileRegion) message;
                        long position = 0;
                        while (position < region.count()) {
                            position += region.transferTo(Channels.newChannel(body), position);
                        }
                    }
                } finally {
                    ReferenceCountUtil.release(message);
                }
            }
        } finally {
            channel.finishAndReleaseAll();
        }
        result.body = body.toString(StandardCharsets.UTF_8);
        return result;
    }

    private static class Response {
        int status;
        HttpHeaders headers;
        String body;
    }
}
