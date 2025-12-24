package com.energy.monitoring.components;

/* Константные значения для кусочков HTTP-сообщений */
public class HttpConstructions {
    public class EndPoints {
        public static final String MAIN_PAGE       = "/index.html";

        public static final String MAIN            = "/";
        public static final String API             = "/api/";
        public static final String HEALTH          = "/api/health";

        public static final String AUTH            = "/api/auth";
        public static final String LOGING          = "/api/auth/login";
        public static final String LOGOUT          = "/api/auth/logout";
        public static final String REGISTER        = "/api/auth/register";
        public static final String CHANGE_PASSWORD = "/api/auth/change-password";
        public static final String VALIDATE        = "/api/auth/validate";
        public static final String PROFILE         = "/api/auth/profile";

        public static final String COORDINATORS    = "/api/coordinators";
        public static final String COORDINATOR     = "/api/coordinators/";
        public static final String CONNECT         = "/connect";
    }

    public class Methods {
        public static final String GET     = "GET";
        public static final String POST    = "POST";
        public static final String PUT     = "PUT";
        public static final String DELETE  = "DELETE";
        public static final String OPTIONS = "OPTIONS";
    }

    public class JsonBlocks {
        public static final String USERNAME       = "username";
        public static final String PASSWORD       = "password";
        public static final String TOKEN          = "token";
        public static final String AUTHORIZATION  = "authorization";
        public static final String CONTENT_LENGTH = "content-length";
        public static final String ORIG           = "origin";
        public static final String CLIENT         = "client";
        public static final String IP             = "ip";
        public static final String NAME           = "name";
        public static final String PORT           = "port";

    }

    public class UserRoles {
        public static final String OPERATOR = "operator";
        public static final String ADMIN    = "admin";
    }

    public class DeviseStatuses {
        public static final String ONLINE  = "online";
        public static final String OFFLINE = "offline";
        public static final String ERROR   = "error";
    }

    public class ContentTypes {
        public static final String JSON  = "application/json; charset=utf-8";
        public static final String HTML  = "text/html; charset=utf-8";
        public static final String CSS   = "text/css; charset=utf-8";
        public static final String JS    = "application/javascript; charset=utf-8";
        public static final String PNG   = "image/png";
        public static final String JPEG  = "image/jpeg";
        public static final String ICO   = "image/x-icon";
        public static final String PLAIN = "text/plain; charset=utf-8";
    }
}
