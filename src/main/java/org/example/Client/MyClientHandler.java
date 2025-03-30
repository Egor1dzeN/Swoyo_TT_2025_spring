package org.example.Client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.Client.DTO.ObjectDTO;

public class MyClientHandler extends SimpleChannelInboundHandler<ObjectDTO> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ObjectDTO objectDTO) {
        System.out.println("Answer from server: \n" + objectDTO.getData());
        MyClient.latch.countDown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
