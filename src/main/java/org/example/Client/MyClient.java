package org.example.Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.example.DTO.CommandType;
import org.example.DTO.ObjectDTO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyClient {
    private final int TIMEOUT_IN_SECONDS = 5;
    private static String username;
    private final String host;
    private final int port;
    public static CountDownLatch latch;

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
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new MyClientHandler());
                        }
                    });

            Channel f = b.connect(host, port).sync().channel();
            enterAnsSendMessage(f);
            f.disconnect();
            f.closeFuture().sync();
//            f.closeFuture().sync();
        } finally {
            group.shutdownGracefully().addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("Client shutdown successfully");
                }
            });
        }
    }

    public void enterAnsSendMessage(Channel channel) {
        try {
            preRunning();
        } catch (IOException e) {
            System.err.println("Error of starting");
            return;
        }
        Scanner scanner = new Scanner(System.in);
        latch = new CountDownLatch(1);
        latch.countDown();
        while (true) {
            try {
                boolean awaitResult = latch.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                if (!awaitResult) {
                    System.out.println("Couldn't get a response from the server");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.print("Введите команду: ");
            String request = scanner.nextLine();
            if (Pattern.matches("^login -u=[a-zA-Z0-9_]+$", request)) {
                login(request);
            }
            if (username == null || username.isEmpty()) {
                System.out.println("Вы не авторизованы напишите login -u=<username> для авторизации");
                continue;
            }
            // Проверяем create topic
            if (Pattern.matches("^create topic -n=[a-zA-Z0-9_]+$", request)) {
                createTopic(request, channel);
//            return true;
            }
            // Проверяем view (без параметров)
            else if (Pattern.matches("^view$", request)) {
                view(request, channel);
//            return true;
            }
            // Проверяем view с параметром -t
            else if (Pattern.matches("^view -t=[a-zA-Z0-9_]+$", request)) {
                view(request, channel);
//            return true;
            }
            // Проверяем create vote
            else if (Pattern.matches("^create vote -t=[a-zA-Z0-9_]+$", request)) {
                createVote(request, channel);
//            return true;
            }
            // Проверяем view с -t и -v
            else if (Pattern.matches("^view -t=[a-zA-Z0-9_]+ -v=[a-zA-Z0-9_]+$", request)) {
                view(request, channel);
//            return true;
            }
            // Проверяем vote
            else if (Pattern.matches("^vote -t=[a-zA-Z0-9_]+ -v=[a-zA-Z0-9_]+$", request)) {
                vote(request, channel);
            } else if (Pattern.matches("^delete -t=[a-zA-Z0-9_]+ -v=[a-zA-Z0-9_]+$", request)) {
                deleteVoid(request, channel);
            } else if (Pattern.matches("^exit$", request)) {
                System.out.println("Turning off");
                return;
            } else {
                System.out.println("Неизвестная команда");
                continue;
            }
            latch = new CountDownLatch(1);
        }
        // Если ни один паттерн не подошел
//        return false;
    }

    private void login(String request) {
        Pattern pattern = Pattern.compile("^login -u=([a-zA-Z0-9_]+)$");
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            username = matcher.group(1);
        } else {
            System.err.println("Unknown error");
        }
    }

    public void deleteVoid(String request, Channel channel) {
        Pattern pattern = Pattern.compile("^delete -t=([a-zA-Z0-9_]+) -v=([a-zA-Z0-9_]+)$");
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            String topicName = matcher.group(1);
            String voteName = matcher.group(2);
            HashMap<String, String> map = new HashMap<>();
            map.put("-t", topicName);
            map.put("-v", voteName);
            ObjectDTO dto = new ObjectDTO(username, null, map, CommandType.DELETE);
            MessageSender messageSender = new MessageSender(dto, channel);
            messageSender.sendMessage(latch);
        }
    }

    public void vote(String request, Channel channel) {
        Pattern pattern = Pattern.compile("^vote -t=([a-zA-Z0-9_]+) -v=([a-zA-Z0-9_]+)$");
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Выберите число ответа:");
            int ans = scanner.nextInt();
            String topic = matcher.group(1);
            String vote = matcher.group(2);
            HashMap<String, String> map = new HashMap<>();
            map.put("-t", topic);
            map.put("-v", vote);
            map.put("-a", ans + "");
            ObjectDTO dto = new ObjectDTO(username, null, map, CommandType.VOTE);
            MessageSender messageSender = new MessageSender(dto, channel);
            messageSender.sendMessage(latch);
        }
    }

    public void createVote(String request, Channel channel) {
        Pattern pattern = Pattern.compile("^create vote -t=([a-zA-Z0-9_]+)$");
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите название голосования: ");
            String name_vote = scanner.nextLine();

            System.out.print("\nВведите тему голосования: ");
            String topic_vote = scanner.nextLine();

            System.out.print("\nВведите количество вариантов ответа: ");
            int count_vote = Integer.parseInt(scanner.nextLine());
            List<String> list = new ArrayList<>();

            System.out.println("\nВведите варианты ответов: ");
            for (int i = 1; i <= count_vote; ++i) {
                System.out.print(i + ". ");
                list.add(scanner.nextLine() + " 0");
            }

            String topic = matcher.group(1);
            HashMap<String, String> map = new HashMap<>();
            map.put("-t", topic);
            map.put("-n", name_vote);
            map.put("-v", topic_vote);
            map.put("-c", count_vote + "");
            String listString = String.join(";", list);
            map.put("-l", listString);
            ObjectDTO objectDTO = new ObjectDTO(username, null, map, CommandType.CREATE_VOTE);
            MessageSender messageSender = new MessageSender(objectDTO, channel);
            messageSender.sendMessage(MyClient.latch);
        }
    }

    public void createTopic(String request, Channel channel) {
        Pattern pattern = Pattern.compile("^create topic -n=([a-zA-Z0-9_]+)$");
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            String topicName = matcher.group(1);
            HashMap<String, String> map = new HashMap<>();
            map.put("-n", topicName);
            ObjectDTO objectDTO = new ObjectDTO(username, "Test", map, CommandType.CREATE_TOPIC);
            MessageSender messageSender = new MessageSender(objectDTO, channel);
            messageSender.sendMessage(MyClient.latch);
//            return loginUser(username); // Вызываем метод входа
        } else {
            System.err.println("Unknown error");
        }
    }

    public void view(String request, Channel channel) {
        HashMap<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("^view -t=([a-zA-Z0-9_]+) -v=([a-zA-Z0-9_]+)$");
        Matcher matcher = pattern.matcher(request);
        if (matcher.matches()) {
            String topicName = matcher.group(1);
            String voteName = matcher.group(2);
            map = new HashMap<>();
            map.put("-t", topicName);
            map.put("-v", voteName);
        }
        pattern = Pattern.compile("^view -t=([a-zA-Z0-9_]+)$");
        matcher = pattern.matcher(request);
        if (matcher.matches()) {
            String topicName = matcher.group(1);
            map = new HashMap<>();
            map.put("-t", topicName);
        }
        pattern = Pattern.compile("^view$");
        matcher = pattern.matcher(request);
        ObjectDTO objectDTO = new ObjectDTO(username, null, map, CommandType.VIEW);
        MessageSender messageSender = new MessageSender(objectDTO, channel);
        messageSender.sendMessage(MyClient.latch);
    }

    public void preRunning() throws IOException {
        String dir = System.getProperty("user.dir");
        String dir1 = dir + "\\chapters";

        // Создаем директорию (если она отсутствует), в которой будут храниться голосования
        File theDir = new File(dir1);
        if (!theDir.exists()) {
            Files.createDirectory(Path.of(dir1));
        }
        String dir2 = dir1 + "\\download";
        theDir = new File(dir2);
        if (!theDir.exists()) {
            Files.createDirectory(Path.of(dir2));
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m") && i + 1 < args.length) {
                if (args[i + 1].equalsIgnoreCase("debug")) {
                    MyClient.username = "admin";
                } else if (args[i + 1].equalsIgnoreCase("release")) {
                    MyClient.username = "";
                } else {
                    System.err.println("Unknown param -m:" + args[i + 1]);
                }
                break;
            }
        }
        try {
            new MyClient("localhost", 8081).start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
