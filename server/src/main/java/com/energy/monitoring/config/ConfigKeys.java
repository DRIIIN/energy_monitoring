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
    
}
