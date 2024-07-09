package com.example;

import com.example.sequence.SequenceProcessor;
import com.example.sequence.SequenceRequest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class HttpServer {
    private final int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        SequenceProcessor processor = SequenceProcessor.getInstance();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(4 * 1024));
                            pipeline.addLast(new IdleStateHandler(0, 0, 10));
                            pipeline.addLast(new HttpServerHandler(processor));
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Server started on port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new HttpServer(port).start();
    }

    static class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
        SequenceProcessor processor;

        public HttpServerHandler(SequenceProcessor processor) {
            this.processor = processor;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
            FullHttpRequest req = (FullHttpRequest) msg;
            processor.publish(new SequenceRequest(req.uri().substring(1), ctx));
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                System.out.println(event.state());
                ctx.close().addListener(future -> {
                    System.out.println("close channel:" + future.isSuccess());

                });
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}