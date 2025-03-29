package org.example.Client.DTO;

import lombok.Getter;

@Getter
public class ObjectDTO {
    private String username;
    private CommandType commandType;
    private Object data;
    enum CommandType {
        CREATE_TOPIC,
        VIEW,
        CREATE_VOTE,
    }
}
