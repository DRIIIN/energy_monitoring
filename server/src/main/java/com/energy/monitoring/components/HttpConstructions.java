package com.energy.monitoring.components;

/* Константные значения для кусочков HTTP-сообщений */
public class HttpConstructions {

    public class EndPoints {
        public static final String LOGING          = "/api/auth/login";
        public static final String LOGOUT          = "/api/auth/logout";
        public static final String REGISTER        = "/api/auth/register";
        public static final String CHANGE_PASSWORD = "/api/auth/change-password";
        public static final String VALIDATE        = "/api/auth/validate";
        public static final String PROFILE         = "/api/auth/profile";
    }

    public class Methods {
        public static final String GET    = "GET";
        public static final String POST   = "POST";
        public static final String PUT    = "PUT";
        public static final String DELETE = "DELETE";
    }

    public class JsonBlocks {
        public static final String USERNAME      = "username";
        public static final String PASSWORD      = "password";
        public static final String TOKEN         = "token";
        public static final String AUTHORIZATION = "authorization";
    }

    public class UserRoles {
        public static final String OPERATOR = "operator";
        public static final String ADMIN    = "admin";
    }

    public class ContentTypes {
        public static final String JSON = "application/json";
    }
}
