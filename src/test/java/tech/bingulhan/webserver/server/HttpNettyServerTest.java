package tech.bingulhan.webserver.server;

import io.netty.channel.Channel;
import io.netty.channel.MultithreadEventLoopGroup;
import org.junit.Test;
import tech.bingulhan.webserver.app.AureliusApplication;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpNettyServerTest {
    @Test(timeout = 15000)
    public void configuredIoThreadCountIsUsedAndThreadsTerminate() throws Exception {
        AureliusApplication app = mock(AureliusApplication.class);
        when(app.getPort()).thenReturn(0);
        when(app.getThreadSize()).thenReturn(1);
        when(app.getRequestThreadSize()).thenReturn(2);
        when(app.getMaxPendingRequests()).thenReturn(16);
        HttpNettyServer server = new HttpNettyServer(app, new String[0]);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread runner = new Thread(() -> {
            try { server.start(); } catch (Throwable e) { failure.set(e); }
        });
        runner.start();
        Channel listener = null;
        MultithreadEventLoopGroup workers = null;
        try {
            long deadline = System.nanoTime() + 5_000_000_000L;
            while (System.nanoTime() < deadline && failure.get() == null) {
                listener = (Channel) field(server, "serverChannel");
                if (listener != null && listener.isActive()) break;
                Thread.sleep(10);
            }
            assertNull(failure.get());
            assertNotNull(listener);
            assertTrue(listener.isActive());
            workers = (MultithreadEventLoopGroup) field(server, "workerGroup");
            assertEquals(1, workers.executorCount());
        } finally {
            if (listener != null) listener.close().syncUninterruptibly();
            runner.join(8000);
        }
        assertFalse("Server thread must terminate", runner.isAlive());
        assertNull(failure.get());
        assertTrue("I/O threads must terminate before start returns", workers.isTerminated());
    }

    static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
