package tech.bingulhan.webserver.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.*;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.server.handler.HttpRequestHandler;

import java.util.concurrent.TimeUnit;

public class HttpNettyServer {
    private final String[] args;
    private final AureliusApplication application;
    private final ChannelGroup connections = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE, true);
    private volatile Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup requestGroup;
    private boolean started;
    private boolean stopping;

    public HttpNettyServer(AureliusApplication application, String[] args) {
        this.application = application;
        this.args = args.clone();
    }

    public void start() {
        synchronized (this) {
            if (started || stopping) throw new IllegalStateException("Server instance already started or stopped");
            started = true;
        }
        try {
            synchronized (this) {
                if (stopping) return;
                bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("aurelius-accept"));
                workerGroup = new NioEventLoopGroup(application.getThreadSize(), new DefaultThreadFactory("aurelius-io"));
                requestGroup = new DefaultEventExecutorGroup(application.getRequestThreadSize(),
                        new DefaultThreadFactory("aurelius-request"), application.getMaxPendingRequests(),
                        RejectedExecutionHandlers.reject());
            }
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            connections.add(channel);
                            channel.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(1048576),
                                    new HttpRequestHandler(application, requestGroup.next()));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            ChannelFuture bind;
            synchronized (this) {
                if (stopping) return;
                bind = bootstrap.bind(application.getPort());
                serverChannel = bind.channel();
            }
            bind.sync();
            if (application.isUi() && serverChannel.isActive()) {
                Thread.ofPlatform().name("aurelius-ui").daemon(true)
                        .start(() -> application.getApplicationUI().load(args));
            }
            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            shutdown();
            awaitTermination(requestGroup);
            awaitTermination(workerGroup);
            awaitTermination(bossGroup);
        }
    }

    /** Initiates shutdown without blocking a Netty or JavaFX thread. */
    public synchronized void shutdown() {
        stopping = true;
        if (serverChannel != null) serverChannel.close();
        connections.close();
        stop(requestGroup);
        stop(workerGroup);
        stop(bossGroup);
    }

    private static void stop(EventExecutorGroup group) {
        if (group != null) group.shutdownGracefully(0, 5, TimeUnit.SECONDS);
    }

    private static void awaitTermination(EventExecutorGroup group) {
        if (group != null) group.terminationFuture().syncUninterruptibly();
    }
}
