package com.example.sequence;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class SequenceRequest {
    private String uid;
    private ChannelHandlerContext ctx;

    public SequenceRequest(String uid, ChannelHandlerContext ctx) {
        this.uid = uid;
        this.ctx = ctx;
    }

    public String getUid() {
        return uid;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void error() {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);

        ctx.writeAndFlush(response).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("success wrote.");
            }
        });

    }

    public void output(long ans) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.content().writeBytes(Long.toString(ans).getBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.writeAndFlush(response).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("success wrote.");
            }
        });

    }

}
