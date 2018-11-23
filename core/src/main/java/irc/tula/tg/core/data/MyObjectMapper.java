package irc.tula.tg.core.data;

public interface MyObjectMapper {
    <T> T read(String fileOrString, Class<T> valueType);
    void write(String fileOrString, Object object);
}
