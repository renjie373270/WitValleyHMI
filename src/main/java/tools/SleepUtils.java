package tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepUtils {

    private static final Logger log = LoggerFactory.getLogger(SleepUtils.class);

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
    }
}
