package org.example.handler;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CachedObjectHandler<T> {

    T jsonToObject(String jsonValue);

    String objectToJSON(T object);

    Optional<LocalDateTime> getCreationDate(T object);
}
