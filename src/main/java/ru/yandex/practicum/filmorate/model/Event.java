package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
public class Event {
    protected int eventId;
    protected int userId;
    protected int entityId;
    protected long timestamp;
    protected String eventType;
    protected String operation;

    public static Map<String, Object> eventToMap(int userId, int entityId, int typeId, int operationId) {
        Map<String, Object> values = new HashMap<>();
        values.put("USER_ID", userId);
        values.put("ENTITY_ID", entityId);
        values.put("TYPE_ID", typeId);
        values.put("OPERATION_ID", operationId);
        return values;
    }
}
