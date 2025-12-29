package com.energy.monitoring.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Конфигурационные параметры проекта */
public class Config {
    private static final Logger     logger            = LoggerFactory.getLogger(Config.class); // Объект Logger для текущего класса
    
    private static final Properties properties        = new Properties();                      // Хранение конфигурационных параметров из файла
    private static final Properties defaultProperties = new Properties();                      // Хранение стандартных конфигурационных параметров 

    // Значения по умолчанию
    static {
        defaultProperties.setProperty(ConfigKeys.Server.PORT,                   "8081"     );
        defaultProperties.setProperty(ConfigKeys.Server.HOST,                   "127.0.0.1");
        defaultProperties.setProperty(ConfigKeys.Server.MAX_THREADS,            "256"      );
        defaultProperties.setProperty(ConfigKeys.Server.CLIENT_WEITING_TIMEOUT, "30000"    );
        
        defaultProperties.setProperty(ConfigKeys.DataBase.URL,                  "jdbc:postgresql://localhost:5432/energy_monitoring_database");
        defaultProperties.setProperty(ConfigKeys.DataBase.USER,                 "postgres"                                                   );
        defaultProperties.setProperty(ConfigKeys.DataBase.PASSWORD,             "12345"                                                      );
        
        defaultProperties.setProperty(ConfigKeys.JsonWebToken.SECRET,           "super-secret-key-:)"     );
        defaultProperties.setProperty(ConfigKeys.JsonWebToken.EXPIRATION_HOURS, "24"                      );
        defaultProperties.setProperty(ConfigKeys.JsonWebToken.ISSUER,           "energy-monitoring-system");

        defaultProperties.setProperty(ConfigKeys.PasswordHasher.ITERATIONS,     "65536"               );
        defaultProperties.setProperty(ConfigKeys.PasswordHasher.KEY_LENGTH,     "256"                 );
        defaultProperties.setProperty(ConfigKeys.PasswordHasher.ALGORITHM,      "PBKDF2WithHmacSHA256");
        defaultProperties.setProperty(ConfigKeys.PasswordHasher.SALT_LENGTH,    "16"                  );

        defaultProperties.setProperty(ConfigKeys.Uart.MAC_ARDRESSES,            ""    );
        defaultProperties.setProperty(ConfigKeys.Uart.PORT_NAMES,               ""    );
        defaultProperties.setProperty(ConfigKeys.Uart.BAUND_RATE,               "9600");
        defaultProperties.setProperty(ConfigKeys.Uart.DATA_BITS,                "8"   );
        defaultProperties.setProperty(ConfigKeys.Uart.STOP_BITS,                "1"   );
        defaultProperties.setProperty(ConfigKeys.Uart.PARITY,                   "0"   );
        defaultProperties.setProperty(ConfigKeys.Uart.MAX_LEN,                  "73"  );
        defaultProperties.setProperty(ConfigKeys.Uart.RESPONSE_TIMEOUT,         "5000");
    }

    // Загрузка значений из конфигурационного файла configFile
    public static void load(String configFile) {
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            logger.info("Configuration loaded from: {}", configFile);
        } catch (IOException e) {
            logger.warn("Could not load configuration from {}, using defaults. Error: {}", configFile, e.getMessage());
        }
    }
    
    // Если из конфигурационного файла было получено значение с ключём key типа String, то вернётся значение из файла, иначе - defaultValue типа String по этому же ключу
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    // Если из конфигурационного файла было получено значение с ключём key типа String, то вернётся значение из файла, иначе - Стандартное значение типа String по этому же ключу
    public static String getString(String key) {
        return properties.getProperty(key, defaultProperties.getProperty(key));
    }
    
    // Если из конфигурационного файла было получено значение с ключём key типа Int, то вернётся значение из файла, иначе - defaultValue типа Int по этому же ключу
    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key));
        } catch (NumberFormatException e) {
            informatedAboutSetedDefaultValue(key, String.valueOf(defaultValue));
            return defaultValue;
        }
    }

    // Если из конфигурационного файла было получено значение с ключём key типа Int, то вернётся значение из файла, иначе - Стандартное значение типа Int по этому же ключу
    public static int getInt(String key) {
        return getInt(key, Integer.parseInt(defaultProperties.getProperty(key)));
    }
    
    // Если из конфигурационного файла было получено значение с ключём key типа Long, то вернётся значение из файла, иначе - defaultValue типа Long по этому же ключу
    public static long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(getString(key));
        } catch (NumberFormatException e) {
            informatedAboutSetedDefaultValue(key, String.valueOf(defaultValue));
            return defaultValue;
        }
    }
    
    // Если из конфигурационного файла было получено значение с ключём key типа Long, то вернётся значение из файла, иначе - Стандартное значение типа Long по этому же ключу
    public static long getLong(String key) {
        return getLong(key, Long.parseLong(defaultProperties.getProperty(key)));
    }
    
    // Если из конфигурационного файла было получено значение с ключём key типа Double, то вернётся значение из файла, иначе - defaultValue типа Double по этому же ключу
    public static double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(getString(key));
        } catch (NumberFormatException e) {
            informatedAboutSetedDefaultValue(key, String.valueOf(defaultValue));
            return defaultValue;
        }
    }

    // Если из конфигурационного файла было получено значение с ключём key типа Double, то вернётся значение из файла, иначе - Стандартное значение типа Double по этому же ключу
    public static long getDouble(String key) {
        return getLong(key, Long.parseLong(defaultProperties.getProperty(key)));
    }
    
    // Если из конфигурационного файла было получено значение с ключём key типа Boolean, то вернётся значение из файла, иначе - defaultValue типа Boolean по этому же ключу
    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(getString(key));
        } catch (Exception e) {
            informatedAboutSetedDefaultValue(key, String.valueOf(defaultValue));
            return defaultValue;
        }
    }

    // Если из конфигурационного файла было получено значение с ключём key типа Boolean, то вернётся значение из файла, иначе - Стандартное значение типа Boolean по этому же ключу
    public static boolean getBoolean(String key) {
        return getBoolean(key, Boolean.parseBoolean(defaultProperties.getProperty(key)));
    }
    
    // Возвращается набор всех конфигурационных параметров
    public static Properties getAllProperties() {
        Properties allProps = new Properties(defaultProperties);
        allProps.putAll(properties);
        return allProps;
    }
    
    // Если конфигурационного значения по ключу key не существует, то добавляется навое значение с этим ключём и значением value, иначе - просто обновляется значение
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    // Очистка всех конфигурационных параметров
    public static void clear() {
        properties.clear();
    }

    // Делает запись в потток предупреждений сообщение о выставление стандартного значения defaultValue по ключу key
    private static void informatedAboutSetedDefaultValue(String key, String defaultValue) {
        logger.warn("Not finde a value by key \"{}\", using default value \"{}\"", key, defaultValue);
    }
}