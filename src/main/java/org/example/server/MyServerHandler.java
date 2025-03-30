package org.example.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.Client.DTO.CommandType;
import org.example.Client.DTO.ObjectDTO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MyServerHandler extends SimpleChannelInboundHandler<ObjectDTO> {

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
            } else if (objectDTO.getCommandType() == CommandType.CREATE_VOTE) {
                objectDTO.setData(createVote(objectDTO.getUsername(), objectDTO.getParams()));
            } else if (objectDTO.getCommandType() == CommandType.VOTE) {
                objectDTO.setData(vote(objectDTO.getParams()));
            } else if (objectDTO.getCommandType() == CommandType.DELETE) {
                objectDTO.setData(delete(objectDTO.getUsername(), objectDTO.getParams()));
            }
            channelHandlerContext.writeAndFlush(objectDTO).addListener(future -> {
                if (!future.isSuccess()) {
                    System.out.println("Error to send " + future.cause().getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    public String delete(String username, Map<String, String> params) throws IOException {
        String topic = params.getOrDefault("-t", "test");
        String vote = params.getOrDefault("-v", "test");
        //Ищем такой файл с голосованием
        String dir = System.getProperty("user.dir");
        dir += "\\chapters\\" + topic + "\\" + vote + ".txt";
        File file = new File(dir);
        StringBuilder sb = new StringBuilder();
        if (!file.exists()) {
            sb.append("Файл не найден");
            return sb.toString();
        }
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);
        String line = reader.readLine();
        List<String> lines = new ArrayList<>();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        fr.close();
        reader.close();
        //Проверяем автора голосования с пользователем
        if (username.equals(lines.getLast())) {
            //Пытаемся удалить
            if (file.delete()) {
                sb.append("Успешно удалено");
            } else {
                sb.append("Файл не найден");
            }
        } else {
            sb.append("Нет прав доступа");
        }
        return sb.toString();
    }

    public String vote(Map<String, String> params) throws IOException {
        String topic = params.get("-t");
        String vote = params.get("-v");
        String path_dir = "chapters\\" + topic + "\\" + vote + ".txt";
        File file = new File(path_dir);
        StringBuilder sb = new StringBuilder();
        if (!file.exists()) {
            sb.append("FileNotFound");
            return sb.toString();
        }
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);
        String line = reader.readLine();
        List<String> lines = new ArrayList<>();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        //Получаем ответ с выбором варианта
        String choice = params.get("-a");
        String[] wordss = lines.get(Integer.parseInt(choice)).split(" ");
        int kol = Integer.parseInt(wordss[wordss.length - 1]) + 1;
        StringBuilder str = new StringBuilder();
        for (int j = 0; j < wordss.length - 1; ++j) {
            str.append(wordss[j]).append(" ");
        }
        str.append(kol);
        lines.set(Integer.parseInt(choice), str.toString());
        Path file1 = Paths.get("chapters\\" + topic + "\\" + vote + ".txt");
        //Перезаписываем голосование
        Files.write(file1, lines, StandardCharsets.UTF_8);
        fr.close();
        reader.close();
        sb.append("Success");
        return sb.toString();
    }

    public String createVote(String username, HashMap<String, String> params) throws IOException {
        String topic = params.get("-t");
        String dir_path = System.getProperty("user.dir") + "\\chapters\\" + topic;
        File file_topic = new File(dir_path);
        StringBuilder sb = new StringBuilder();
        if (!file_topic.exists()) {
            sb.append("Такой раздел не существует");
            return sb.toString();
        }
        //Запрашиваем название, тему голосования, кол-во вариантов ответа, варианты ответа
        String name_vote = params.get("-n");
        String topic_vote = params.get("-v");

        List<String> list;
        list = Arrays.stream(params.get("-l").split(";")).toList();
        //Список из строк, которые будут записаны в файл голосования
        List<String> lines = new ArrayList<>();
        lines.add(topic_vote);
        lines.addAll(list);
        lines.add(username);
        Path file = Paths.get("chapters\\" + topic + "\\" + name_vote + ".txt");
        //Записываем голосование в файл
        Files.write(file, lines, StandardCharsets.UTF_8);
        sb.append("Голосование успешно создано");
        return sb.toString();
    }

    public String createTopic(String name) throws IOException {
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
            } else {
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
                        ans.append("Тема: ").append(lines.get(i)).append('\n');
                    } else
//                        if (lines.get(i).isEmpty()) {
//
//                    } else
                        if (i == lines.size() - 1)
                            ans.append("Разработчик  - ").append(lines.get(i)).append('\n');
                        else {
                            //String words[] = list.get(i).split(" ");
                            ans.append(i).append(". ").append(lines.get(i)).append('\n');
                        }


                }
                fr.close();
                reader.close();
                return ans.toString();
            }

        }


        //Если разделов на нашлось
        if (count == 0)
            ans = new StringBuilder("Разделы с именем " + nameTopic + " не найдены");
        //Отправляем ответ
        return ans.toString();
    }
}
