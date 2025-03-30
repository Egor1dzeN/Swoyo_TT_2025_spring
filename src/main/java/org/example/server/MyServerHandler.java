package org.example.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.Client.DTO.CommandType;
import org.example.Client.DTO.ObjectDTO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyServerHandler extends SimpleChannelInboundHandler<ObjectDTO> {
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        ByteBuf in = (ByteBuf) msg;
//        System.out.println("Сервер получил: " + in.toString(io.netty.util.CharsetUtil.UTF_8));
//
//        // Отправляем полученное сообщение обратно
//        ctx.writeAndFlush(msg);
////        MyClient.latch.countDown();
////        ctx.close();
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ObjectDTO objectDTO) {
        try {
            System.out.println("Request from client: " + objectDTO);
            if (objectDTO.getCommandType() == CommandType.CREATE_TOPIC) {
                if (objectDTO.getParams().containsKey("-n")) {
                    objectDTO.setData(createTopic(objectDTO.getParams().get("-n")));
                } else {
                    throw new Exception("Not found value of key -n");
                }
            } else if (objectDTO.getCommandType() == CommandType.VIEW) {
                objectDTO.setData(viewList(objectDTO.getParams().getOrDefault("-t", null), objectDTO.getParams().getOrDefault("-v", null)));
            }
            channelHandlerContext.writeAndFlush(objectDTO).addListener(future -> {
                if (!future.isSuccess()) {
                    System.out.println("Error to send " + future.cause().getMessage());
                }
            });
        } catch (Exception e) {
            objectDTO.setData("Error: " + e.getMessage());
            channelHandlerContext.writeAndFlush(objectDTO).addListener(future -> {
                if (!future.isSuccess()) {
                    System.out.println("Error to send " + future.cause());
                }
            });
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("client connected");
        System.out.println(ctx.name());
        System.out.println(ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Клиент отключился: " + ctx.channel().remoteAddress());
        ctx.close();
    }

    public String createTopic(String name) throws IOException {
        System.out.println("Create topic: " + name);
        StringBuilder sb = new StringBuilder();
        String dir = System.getProperty("user.dir");
        dir += "\\chapters\\" + name;
        File file = new File(dir);

        if (file.exists()) {
            sb.append("Такой раздел уже существует");
            return sb.toString();
        }

        Files.createDirectory(Path.of(dir));
        sb.append("Раздел с именем ").append(name).append(" создан");
        return sb.toString();
    }

    public String viewList(String nameTopic, String nameVote) throws IOException {
        String dir = System.getProperty("user.dir");
        dir += "\\chapters";
        File dir4 = new File(dir);
        StringBuilder ans = new StringBuilder();
        int count = 0;
        //Берем все существующие на сервере разделы
        if (nameTopic == null) {
            for (File file : Objects.requireNonNull(dir4.listFiles())) {
                count++;
                ans.append("Раздел - ").append(file.getName()).append(". ");
                ans.append(" Кол-во голосований - ").append(Objects.requireNonNull(file.listFiles()).length);
            }
        } else {
            if (nameVote == null || nameVote.isEmpty()) {
                for (File file : Objects.requireNonNull(dir4.listFiles())) {
                    //Проверяем на название
                    if (file.getName().equals(nameTopic)) {
                        count++;
                        ans.append("Раздел - ").append(file.getName()).append(" - ");
                        ans.append(" Кол-во голосований - ").append(Objects.requireNonNull(file.listFiles()).length).append(';');
                    }
                }
            }else{
                String path_dir = "chapters\\" + nameTopic + "\\" + nameVote + ".txt";
                File file = new File(path_dir);
                //Ищем файл голосования в разделе <topic> с именем <vote>
                if (!file.exists()) {
                    ans.append("Файл не найден");
                }
                //Читаем файл голосования
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                String line = reader.readLine();
                List<String> lines = new ArrayList<>();
                while (line != null) {
                    lines.add(line);
                    line = reader.readLine();
                }

                //Отправляем список ответа в формате
                /*
                 * тему голосования
                 * варианты ответа и количество пользователей выбравших данный вариант
                 */
                for (int i = 0; i < lines.size(); ++i) {
                    if (i == 0) {
                        ans.append("Тема: ").append(lines.get(i));
                    } else
//                        if (lines.get(i).isEmpty()) {
//
//                    } else
                        if (i == lines.size() - 1)
                        ans.append("Разработчик  - ").append(lines.get(i));
                    else {
                        //String words[] = list.get(i).split(" ");
                        ans.append(i).append(". ").append(lines.get(i));
                    }


                }
                fr.close();
                reader.close();
            }

        }


        //Если разделов на нашлось
        if (count == 0)
            ans = new StringBuilder("Разделы с именем " + nameTopic + " не найдены");
        //Отправляем ответ
        return ans.toString();
    }
}
