import irc.tula.tg.core.Cave;
import irc.tula.tg.core.RDBResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class RDBTest {
    @Test
    void testSimple1() {
        RDBResource r = new RDBResource("/tmp/rand_when.rdb", Cave.getEncoding());
        log.info("RDB converted to JSON: {}", r.toJSON());
        assertEquals(10, r.size());
        assertEquals(10, r.getDelay().intValue());

        for (int i = 0; i < 10; i++) {
            log.info("" + (i+1) + ": " + r.nextString());
        }
    }
}
