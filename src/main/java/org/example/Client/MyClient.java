package org.example.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MyClient {
    private final String host;
    private final int port;

    public MyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        System.out.println("Connecting to " + host + ":" + port);
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new MyServerHandler());
                        }
                    });

            Channel f = b.connect(host, port).sync().channel();

            // Отправляем сообщение серверу
            String message = "Привет, Netty!";
//            Thread.sleep(100000000);
            ByteBuf buf = Unpooled.copiedBuffer(message, io.netty.util.CharsetUtil.UTF_8);

            f.writeAndFlush(buf);
//            f.disconnect();
            try {
                if (f != null) {
                    f.close().sync();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
                System.out.println("Клиент отключен");
                System.exit(0);
            }
//            f.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new MyClient("localhost", 8081).start();
    }
}
