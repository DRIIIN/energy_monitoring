package com.energy.monitoring.components;

/* Константные конструкции обращений к таблицам базы данных */
public class SqlRequests {
    public class User {
        public static final String CREATE_USER     =                   "INSERT INTO " + DataBaseFildNames.TableNames.USERS      + " (" 
                                                                                      + DataBaseFildNames.Tables.User.LOGIN     + ", " 
                                                                                      + DataBaseFildNames.Tables.User.PASSWORD  + ", " 
                                                                                      + DataBaseFildNames.Tables.User.IS_ACTIVE + ") VALUES (?, ?, ?)";

        public static final String AUTENTIFICATE   =                 "SELECT * FROM " + DataBaseFildNames.TableNames.USERS      + " WHERE " 
                                                                                      + DataBaseFildNames.Tables.User.LOGIN     + " = ?";

        public static final String USER_EXISTS     = "SELECT COUNT(*) as count FROM " + DataBaseFildNames.TableNames.USERS      + " WHERE " 
                                                                                      + DataBaseFildNames.Tables.User.LOGIN     + " = ?";

        public static final String ACTIVATE_USER   =                        "UPDATE " + DataBaseFildNames.TableNames.USERS      + " SET is_active = 1 WHERE " 
                                                                                      + DataBaseFildNames.Tables.User.ID        + " = ?";

        public static final String DEACTIVATE_USER =                        "UPDATE " + DataBaseFildNames.TableNames.USERS      + " SET is_active = 0 WHERE " 
                                                                                      + DataBaseFildNames.Tables.User.ID        + " = ?";
    }

    public class Coordinator {
        public static final String CREATE_COORDINATOR           =                   "INSERT INTO " + DataBaseFildNames.TableNames.COORDINATORS      + " (" 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.USER_ID   + ", " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.NAME      + ", " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.IP        + ", " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.PORT      + ", " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.STATUS    + ") VALUES (?, ?, ?, ?, ?)";

        public static final String GET_USER_COORDINATORS        =                 "SELECT * FROM " + DataBaseFildNames.TableNames.COORDINATORS      + " WHERE " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.USER_ID   + " = ? ORDER BY " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.NAME;
        public static final String GET_COORDINATOR              =                 "SELECT * FROM " + DataBaseFildNames.TableNames.COORDINATORS      + " WHERE " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.ID        + " = ?";

        public static final String UPDATE_COORDINATOR_STATUS    =                        "UPDATE " + DataBaseFildNames.TableNames.COORDINATORS      + " SET " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.STATUS    + " = ?, " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.LAST_SEEN + " = ? WHERE " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.ID        + " = ?";

        public static final String DELETE_COORDINATOR           =                   "DELETE FROM " + DataBaseFildNames.TableNames.COORDINATORS      + " WHERE " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.ID        + " = ?";

        public static final String COORDINATORS_BELONGS_TO_USER = "SELECT COUNT(*) as count FROM " + DataBaseFildNames.TableNames.COORDINATORS      + " WHERE " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.ID        + " = ? AND " 
                                                                                                   + DataBaseFildNames.Tables.Coordinator.USER_ID   + " = ?";
    }

    public class Meter {
        public static final String CREATE_COORDINATOR        =   "INSERT INTO " + DataBaseFildNames.TableNames.METERS            + " (" 
                                                                                + DataBaseFildNames.Tables.Meter.COORDINATOR_ID  + ", " 
                                                                                + DataBaseFildNames.Tables.Meter.NAME            + ", " 
                                                                                + DataBaseFildNames.Tables.Meter.ZB_LONG_ADDR    + ", " 
                                                                                + DataBaseFildNames.Tables.Meter.ZB_SHORT_ADDR   + ", " 
                                                                                + DataBaseFildNames.Tables.Meter.STATUS          + ") VALUES (?, ?, ?, ?, ?)";

        public static final String GET_METERS_BY_COORDINATOR = "SELECT * FROM " + DataBaseFildNames.TableNames.METERS            + " WHERE " 
                                                                                + DataBaseFildNames.Tables.Meter.COORDINATOR_ID  + " = ? ORDER BY " 
                                                                                + DataBaseFildNames.Tables.Meter.NAME;

        public static final String GET_METER                 = "SELECT * FROM " + DataBaseFildNames.TableNames.METERS            + " WHERE " 
                                                                                + DataBaseFildNames.Tables.Meter.ID              + " = ?";

        public static final String UPDATE_METER_DATA         =        "UPDATE " + DataBaseFildNames.TableNames.METERS            + " SET " 
                                                                                + DataBaseFildNames.Tables.Meter.VOLTAGE         + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.CURRENT         + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.ACTIVE_POWER    + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.REACTIVE_POWER  + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.APPARENT_POWER  + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.POWER_FACTOR    + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.FREQUENCY       + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.NEUTRAL_CURRENT + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.LAST_SEEN       + " = ? WHERE " 
                                                                                + DataBaseFildNames.Tables.Meter.ID              + " = ?";

        public static final String UPDATE_METER_STATUS       =        "UPDATE " + DataBaseFildNames.TableNames.METERS      + " SET " 
                                                                                + DataBaseFildNames.Tables.Meter.STATUS    + " = ?, " 
                                                                                + DataBaseFildNames.Tables.Meter.LAST_SEEN + " = ? WHERE " 
                                                                                + DataBaseFildNames.Tables.Meter.ID        + " = ?";

        public static final String DELETE_METER              =   "DELETE FROM " + DataBaseFildNames.TableNames.METERS      + " WHERE " 
                                                                                + DataBaseFildNames.Tables.Meter.ID        + " = ?";
    }
}
