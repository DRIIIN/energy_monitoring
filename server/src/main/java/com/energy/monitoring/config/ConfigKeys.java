package com.energy.monitoring.config;

/* Ключи конфигурационных параметров */
public class ConfigKeys {
    
    public class Server {
        public static final String PORT                   = "server.port";
        public static final String HOST                   = "server.host";
        public static final String MAX_THREADS            = "server.max_threads";
        public static final String CLIENT_WEITING_TIMEOUT = "server.client_timeout";
    }

    public class DataBase {
        public static final String URL      = "db.url";
        public static final String USER     = "db.user";
        public static final String PASSWORD = "db.password";
    }

    public class JsonWebToken {
        public static final String SECRET           = "jwt.secret";
        public static final String EXPIRATION_HOURS = "jwt.expiration.hours";
        public static final String ISSUER           = "jwt.issuer";
    }
    
    public class PasswordHasher {
        public static final String ITERATIONS  = "password.hasher.iterations";
        public static final String KEY_LENGTH  = "password.hasher.key_length";
        public static final String ALGORITHM   = "password.hasher.algorithm";
        public static final String SALT_LENGTH = "password.hasher.salt_length";
    }

    public class Uart {
        public static final String MAC_ARDRESSES    = "uart.mac_addresses";
        public static final String PORT_NAMES       = "uart.ports";
        public static final String BAUND_RATE       = "uart.baund";
        public static final String DATA_BITS        = "uart.data_bits";
        public static final String STOP_BITS        = "uart.stop_bits";
        public static final String PARITY           = "uart.parity";
        public static final String MAX_LEN          = "uart.max_len";
        public static final String RESPONSE_TIMEOUT = "uart.response_timeout";
    }
}
