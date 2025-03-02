package tech.bingulhan.webserver.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import tech.bingulhan.webserver.app.AureliusApplication;
import tech.bingulhan.webserver.server.handler.HttpRequestHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpNettyServer {

    private String[] args;
    private AureliusApplication application;

    private ExecutorService webServerService;

    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public HttpNettyServer(AureliusApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    public void start() {
        this.webServerService = Executors.newCachedThreadPool();

        int availableProcessors = Runtime.getRuntime().availableProcessors();


        bossGroup = new NioEventLoopGroup(1);

        workerGroup = new NioEventLoopGroup(availableProcessors * 2);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {

                            ch.pipeline().addLast(
                                    new HttpResponseEncoder(),
                                    new HttpRequestDecoder(),
                                    new HttpObjectAggregator(1048576),
                                    new HttpRequestHandler());
                        }
                    })

             .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);


            ChannelFuture future = bootstrap.bind(application.getPort()).sync();
            serverChannel = future.channel();

            if (application.isUi()) {
                this.webServerService.execute(() -> application.getApplicationUI().load(args));
            }

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public void shutdown() {

            if (serverChannel != null && serverChannel.isActive()) {
                serverChannel.close();
            }

            if (bossGroup != null && !bossGroup.isShuttingDown()) {
                bossGroup.shutdownGracefully();
            }

            if (workerGroup != null && !workerGroup.isShuttingDown()) {
                workerGroup.shutdownGracefully();
            }

            if (webServerService != null && !webServerService.isShutdown()) {
                webServerService.shutdown();
            }

            System.exit(0);

    }
}
