package tech.bingulhan.webserver.response.impl.media;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.FileRegion;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.app.AureliusApplicationData;
import tech.bingulhan.webserver.app.mvc.MediaStructure;
import tech.bingulhan.webserver.response.NettyResponseService;
import tech.bingulhan.webserver.response.RequestStructure;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NettyResponseMediaHandlerTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void streamsFileWithoutAllocatingFullBody() throws Exception {
        byte[] content = new byte[1024 * 1024];
        for (int i = 0; i < content.length; i++) content[i] = (byte) i;
        EmbeddedChannel channel = request(HttpMethod.GET, content);
        Object headers = channel.readOutbound();
        Object body = null;
        try {
            assertTrue(headers instanceof HttpResponse);
            assertFalse("File must not be buffered into a FullHttpResponse", headers instanceof FullHttpResponse);
            assertEquals(content.length, HttpUtil.getContentLength((HttpResponse) headers));
            body = channel.readOutbound();
            assertTrue(body instanceof FileRegion);
            FileRegion region = (FileRegion) body;
            ByteArrayOutputStream copy = new ByteArrayOutputStream();
            long position = 0;
            while (position < region.count()) {
                long written = region.transferTo(Channels.newChannel(copy), position);
                assertTrue(written > 0);
                position += written;
            }
            assertArrayEquals(content, copy.toByteArray());
            assertTrue(channel.readOutbound() instanceof LastHttpContent);
        } finally {
            ReferenceCountUtil.release(headers);
            ReferenceCountUtil.release(body);
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void headReportsLengthWithoutSendingFile() throws Exception {
        EmbeddedChannel channel = request(HttpMethod.HEAD, new byte[123]);
        Object response = channel.readOutbound();
        try {
            assertEquals(200, ((HttpResponse) response).status().code());
            assertEquals(123, HttpUtil.getContentLength((HttpResponse) response));
            assertTrue(response instanceof FullHttpResponse);
            assertEquals(0, ((FullHttpResponse) response).content().readableBytes());
            assertNull(channel.readOutbound());
        } finally {
            ReferenceCountUtil.release(response);
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void rejectsUnsupportedMethod() throws Exception {
        EmbeddedChannel channel = request(HttpMethod.POST, new byte[1]);
        Object response = channel.readOutbound();
        try {
            assertEquals(405, ((HttpResponse) response).status().code());
            assertEquals("GET, HEAD", ((HttpResponse) response).headers().get(HttpHeaderNames.ALLOW));
        } finally {
            ReferenceCountUtil.release(response);
            channel.finishAndReleaseAll();
        }
    }

    private EmbeddedChannel request(HttpMethod method, byte[] bytes) throws Exception {
        Path file = temp.newFile("sample.mp4").toPath();
        Files.write(file, bytes);
        AureliusApplication app = mock(AureliusApplication.class);
        AureliusApplicationData data = mock(AureliusApplicationData.class);
        when(app.getData()).thenReturn(data);
        when(data.getMediaStructures()).thenReturn(Collections.singletonList(
                new MediaStructure("sample.mp4", file.toString())));
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/public/sample.mp4");
        RequestStructure structure = new RequestStructure();
        structure.setRoot(request.uri());
        structure.setMethod(method.name());
        try {
            new NettyResponseMediaHandler().handleResponse(
                    new NettyResponseService(app, request, channel.pipeline().firstContext()), structure);
        } finally {
            request.release();
        }
        return channel;
    }
}
