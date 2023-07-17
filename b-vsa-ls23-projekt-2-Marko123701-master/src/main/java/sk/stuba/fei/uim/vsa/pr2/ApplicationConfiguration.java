package sk.stuba.fei.uim.vsa.pr2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);

    public static String BASE_URI = getEnvOrDefault("BASE_URI", "");
    public static final String PROTOCOL = getEnvOrDefault("PROTOCOL", "http");
    public static final String HOSTNAME = getEnvOrDefault("HOSTNAME", "localhost");
    public static final String APP_PATH = getEnvOrDefault("APP_PATH", "api");
    public static final String PORT = getEnvOrDefault("PORT", "8080");

    static {
        if (BASE_URI.isEmpty()) {
            BASE_URI = PROTOCOL + "://" + HOSTNAME + ":" + PORT + "/" + APP_PATH;
        }
        log.info("Set base uri: " + BASE_URI);
    }

    public static String getEnvOrDefault(String key, String defaultValue) {
        String env = System.getenv(key);
        return env != null && !env.isEmpty() ? env : (defaultValue != null ? defaultValue : "");
    }


}
