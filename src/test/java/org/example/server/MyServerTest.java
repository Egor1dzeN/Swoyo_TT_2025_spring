package org.example.server;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.example.DTO.CommandType;
import org.example.DTO.ObjectDTO;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyServerTest {
    @Test
    void testServerHandlerCreateTopic() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new StringDecoder(),
                new StringEncoder(),
                new MyServerHandler());
        String topic = "test1";
        String dir = System.getProperty("user.dir") + "\\chapters\\" + topic;
        File file = new File(dir);
        boolean flag = true;
        if (file.exists()) {
            flag = file.delete();
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("-n", topic);
        ObjectDTO objectDTO = new ObjectDTO("admin", null, map, CommandType.CREATE_TOPIC);
        channel.writeInbound(objectDTO);
        Object response = channel.readOutbound();
        System.out.println(response);
        assertInstanceOf(ObjectDTO.class, response);
        if (flag) {
            assertEquals("Раздел с именем " + topic + " создан", ((ObjectDTO) response).getData());
        }
    }

    @Test
    void testServerHandlerCreateVote1() {
        EmbeddedChannel channel = new EmbeddedChannel(
                new StringDecoder(),
                new StringEncoder(),
                new MyServerHandler());
        HashMap<String, String> map = new HashMap<>();
        map.put("-t", "topic");
        map.put("-n", "name_vote");
        map.put("-v", "topic_vote");
        map.put("-c", 12 + "");
        String listString = String.join(";", List.of("True 0; False 0;"));
        map.put("-l", listString);
        ObjectDTO objectDTO = new ObjectDTO("admin", null, map, CommandType.CREATE_VOTE);
        channel.writeInbound(objectDTO);
        Object response = channel.readOutbound();
        System.out.println(response);
        assertInstanceOf(ObjectDTO.class, response);
        assertEquals(((ObjectDTO) response).getData(), "Такой раздел не существует");
    }

    @Test
    void testServerHandlerCreateVote2() {
        testServerHandlerCreateTopic();
        EmbeddedChannel channel = new EmbeddedChannel(
                new StringDecoder(),
                new StringEncoder(),
                new MyServerHandler());
        HashMap<String, String> map = new HashMap<>();
        map.put("-t", "test1");
        map.put("-n", "name_vote");
        map.put("-v", "topic_vote");
        map.put("-c", 12 + "");
        String listString = String.join(";", List.of("True 0; False 0;"));
        map.put("-l", listString);
        ObjectDTO objectDTO = new ObjectDTO("admin", null, map, CommandType.CREATE_VOTE);
        channel.writeInbound(objectDTO);
        Object response = channel.readOutbound();
        System.out.println(response);
        assertInstanceOf(ObjectDTO.class, response);
        assertEquals(((ObjectDTO) response).getData(), "Голосование успешно создано");
    }
    @Test
    void testServerHandlerView(){
        testServerHandlerCreateTopic();
        EmbeddedChannel channel = new EmbeddedChannel(
                new StringDecoder(),
                new StringEncoder(),
                new MyServerHandler());
        HashMap<String, String> map = new HashMap<>();
        map.put("-t", "hello");
        map.put("-v", "test");
        ObjectDTO objectDTO = new ObjectDTO("admin", null, map, CommandType.CREATE_VOTE);
        channel.writeInbound(objectDTO);
        Object response = channel.readOutbound();
        System.out.println(response);
        assertInstanceOf(ObjectDTO.class, response);
        assertEquals(((ObjectDTO) response).getData(), "Такой раздел не существует");
    }
    @Test
    void testServerHandlerVote1(){
        testServerHandlerCreateVote2();
        HashMap<String, String> map = new HashMap<>();
        EmbeddedChannel channel = new EmbeddedChannel(
                new StringDecoder(),
                new StringEncoder(),
                new MyServerHandler());
        map.put("-t", "test1");
        map.put("-v", "name_vote");
        map.put("-a", 1 + "");
        ObjectDTO objectDTO = new ObjectDTO("admin", null, map, CommandType.VOTE);
        channel.writeInbound(objectDTO);
        Object response = channel.readOutbound();
        assertInstanceOf(ObjectDTO.class, response);
        assertEquals(((ObjectDTO) response).getData(), "Success");
    }
    @Test
    void testServerHandlerVote2(){
        HashMap<String, String> map = new HashMap<>();
        EmbeddedChannel channel = new EmbeddedChannel(
                new StringDecoder(),
                new StringEncoder(),
                new MyServerHandler());
        map.put("-t", "test1__");
        map.put("-v", "name_vote");
        map.put("-a", 1 + "");
        ObjectDTO objectDTO = new ObjectDTO("admin", null, map, CommandType.VOTE);
        channel.writeInbound(objectDTO);
        Object response = channel.readOutbound();
        assertInstanceOf(ObjectDTO.class, response);
        assertEquals(((ObjectDTO) response).getData(), "FileNotFound");
    }
}