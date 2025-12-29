package com.energy.monitoring.components;

/* Константные значения имён полей таблиц базы данных */
public class DataBaseFildNames {
    public class Tables {
        public class User {
            public static final String ID        = "id";
            public static final String LOGIN     = "login";
            public static final String PASSWORD  = "password";
            public static final String IS_ACTIVE = "is_active";
        }

        public class Coordinator {
            public static final String ID         = "id";
            public static final String USER_ID    = "user_id";
            public static final String NAME       = "name";
            public static final String MAC        = "mac";
            public static final String IP         = "ip";
            public static final String PORT       = "port";
            public static final String STATUS     = "status";
            public static final String CREATED_AT = "created_at";
            public static final String LAST_SEEN  = "last_seen";
        }

        public class Meter {
            public static final String ID              = "id";
            public static final String COORDINATOR_ID  = "coordinator_id";
            public static final String ZB_LONG_ADDR    = "zb_zong_addr";
            public static final String ZB_SHORT_ADDR   = "zb_short_addr";
            public static final String NAME            = "name";
            public static final String STATUS          = "status";
            public static final String CREATED_AT      = "created_at";
            public static final String LAST_SEEN       = "last_seen";
            public static final String VOLTAGE         = "voltage";
            public static final String CURRENT         = "current";
            public static final String ACTIVE_POWER    = "active_power";
            public static final String REACTIVE_POWER  = "reactive_power";
            public static final String APPARENT_POWER  = "apparent_power";
            public static final String POWER_FACTOR    = "power_factor";
            public static final String FREQUENCY       = "frequency";
            public static final String NEUTRAL_CURRENT = "neutral_current";
        }
    }

    public class TableNames {
        public static final String USERS        = "users";
        public static final String COORDINATORS = "coordinators";
        public static final String METERS       = "meters";
    }
}
