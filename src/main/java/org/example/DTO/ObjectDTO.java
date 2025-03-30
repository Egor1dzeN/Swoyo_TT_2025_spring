package org.example.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@ToString
@Setter
public class ObjectDTO implements Serializable {
    private String username;
    private CommandType commandType;
    private HashMap<String, String> params;
    private Object data;

    public ObjectDTO(String username, Object data, HashMap<String, String> params, CommandType commandType) {
        this.username = username;
        this.data = data;
        this.params = params;
        this.commandType = commandType;
    }
}
