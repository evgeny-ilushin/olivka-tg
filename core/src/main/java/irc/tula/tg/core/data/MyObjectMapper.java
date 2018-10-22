package irc.tula.tg.core.data;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.Optional;

public interface MyObjectMapper {
    <T> T read(String fileOrString, Class<T> valueType);
    void write(String fileOrString, Object object);
}
