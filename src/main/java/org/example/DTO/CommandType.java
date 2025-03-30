package org.example.DTO;

import java.io.Serializable;

public enum CommandType implements Serializable {
    CREATE_TOPIC,
    VIEW,
    CREATE_VOTE,
    VOTE,
    DELETE
}
