package org.example.Client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.Client.DTO.ObjectDTO;

public class MyClientHandler extends SimpleChannelInboundHandler<ObjectDTO> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ObjectDTO objectDTO) throws Exception {
        System.out.println("Response from server: " + objectDTO);
        System.out.println("Data:"+objectDTO.getData());
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
