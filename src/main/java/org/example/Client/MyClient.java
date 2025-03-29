package org.example.Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyClient {
    private static String username;
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

            enterAnsSendMessage(f);
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

    public void enterAnsSendMessage(Channel channel) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Введите команду: ");
            String request = scanner.nextLine();
            System.out.println("Вы ввели: " + request);
            if (Pattern.matches("^login -u=[a-zA-Z0-9_]+$", request)) {
                login(request);
            }
            if (username == null) {
                System.out.println("Вы не авторизованы напишите login -u=<username> для авторизации");
                continue;
            }
            // Проверяем create topic
            if (Pattern.matches("^create topic -n=[a-zA-Z0-9_]+$", request)) {
//            return true;
            }
            // Проверяем view (без параметров)
            else if (Pattern.matches("^view$", request)) {
//            return true;
            }
            // Проверяем view с параметром -t
            else if (Pattern.matches("^view -t=[a-zA-Z0-9_]+$", request)) {
//            return true;
            }
            // Проверяем create vote
            else if (Pattern.matches("^create vote -t=[a-zA-Z0-9_]+$", request)) {
//            return true;
            }
            // Проверяем view с -t и -v
            else if (Pattern.matches("^view -t=[a-zA-Z0-9_]+ -v=[a-zA-Z0-9_]+$", request)) {
//            return true;
            }
            // Проверяем vote
            else if (Pattern.matches("^vote -t=[a-zA-Z0-9_]+ -v=[a-zA-Z0-9_]+$", request)) {
//            return true;
            }
            else if (Pattern.matches("^delete -t=[a-zA-Z0-9_]+ -v=[a-zA-Z0-9_]+$", request)) {
//            return true;
            }
            else if (Pattern.matches("^exit$", request)) {
//            return true;
            }
        }
        // Если ни один паттерн не подошел
//        return false;
    }

    private void login(String string) {
        Pattern pattern = Pattern.compile("^login -u=([a-zA-Z0-9_]+)$");
        Matcher matcher = pattern.matcher(string);
        if (matcher.matches()) {
            username = matcher.group(1);
//            return loginUser(username); // Вызываем метод входа
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            new MyClient("localhost", 8081).start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
