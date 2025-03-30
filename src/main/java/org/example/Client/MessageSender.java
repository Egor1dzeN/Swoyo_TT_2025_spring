package org.example.Client;

import io.netty.channel.Channel;
import lombok.Data;
import org.example.DTO.ObjectDTO;

import java.util.concurrent.CountDownLatch;

@Data
public class MessageSender {
    private ObjectDTO objectDTO;
    private Channel channel;

    public MessageSender(ObjectDTO objectDTO, Channel channel) {
        this.objectDTO = objectDTO;
        this.channel = channel;
    }

    public void sendMessage(CountDownLatch latch) {
        channel.writeAndFlush(objectDTO).addListener(future -> {
            if (!future.isSuccess()) {
                System.err.println("Ошибка отправки: " + future.cause());
                latch.countDown();
            }
        });
    }
}
