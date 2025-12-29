package com.energy.monitoring.components;

import java.util.HashMap;
import java.util.Map;

public class CoordinatorCommands {
    private static final Map<Integer, String> COMMAND_NAMES = new HashMap<>();
    
    static {
        COMMAND_NAMES.put((int)Codes.OPEN_SESSION,          Names.OPEN_SESSION);
        COMMAND_NAMES.put((int)Codes.SET_METER_ADDRESS,     Names.SET_METER_ADDRESS);
        COMMAND_NAMES.put((int)Codes.LEAVE_NETWORK,         Names.LEAVE_NETWORK);
        COMMAND_NAMES.put((int)Codes.NODE_REBOOT,           Names.NODE_REBOOT);
        COMMAND_NAMES.put((int)Codes.DISCOVER_NETWORK,      Names.DISCOVER_NETWORK);
        COMMAND_NAMES.put((int)Codes.PERMIT_NETWORK_JOIN,   Names.PERMIT_NETWORK_JOIN);
        COMMAND_NAMES.put((int)Codes.GET_NETWORK_INFO,      Names.GET_NETWORK_INFO);
        COMMAND_NAMES.put((int)Codes.RECREATE_NETWORK,      Names.RECREATE_NETWORK);
        COMMAND_NAMES.put((int)Codes.GET_CHILD_TABLE,       Names.GET_CHILD_TABLE);
        COMMAND_NAMES.put((int)Codes.GET_NODE_VERSION,      Names.GET_NODE_VERSION);
        COMMAND_NAMES.put((int)Codes.LAUNCH_BOOTLOADER,     Names.LAUNCH_BOOTLOADER);
        COMMAND_NAMES.put((int)Codes.UPLOAD_BOOTLOADER,     Names.UPLOAD_BOOTLOADER);
        COMMAND_NAMES.put((int)Codes.RECREATE_NETWORK_EX,   Names.RECREATE_NETWORK_EX);
        COMMAND_NAMES.put((int)Codes.SINK_COMMAND_MYRMIDON, Names.SINK_COMMAND_MYRMIDON);
        COMMAND_NAMES.put((int)Codes.SET_BEBUG_CONFIG,      Names.SET_BEBUG_CONFIG);
        COMMAND_NAMES.put((int)Codes.CLOSE_SESSION,         Names.CLOSE_SESSION);
    }

    public static String getCommandNameByCode(int commandCode) {
        return COMMAND_NAMES.getOrDefault(commandCode, Names.UNKNOWN);
    }

    public class Codes {
        public static final byte OPEN_SESSION          = (byte)0x00;
        public static final byte SET_METER_ADDRESS     = (byte)0x01;
        public static final byte LEAVE_NETWORK         = (byte)0x05;
        public static final byte NODE_REBOOT           = (byte)0x06;
        public static final byte DISCOVER_NETWORK      = (byte)0x07;
        public static final byte PERMIT_NETWORK_JOIN   = (byte)0x08;
        public static final byte GET_NETWORK_INFO      = (byte)0x09;
        public static final byte RECREATE_NETWORK      = (byte)0x0B;
        public static final byte GET_CHILD_TABLE       = (byte)0x0C;
        public static final byte GET_NODE_VERSION      = (byte)0x19;
        public static final byte LAUNCH_BOOTLOADER     = (byte)0x1A;
        public static final byte UPLOAD_BOOTLOADER     = (byte)0x1B;
        public static final byte RECREATE_NETWORK_EX   = (byte)0x23;
        public static final byte SINK_COMMAND_MYRMIDON = (byte)0xAB;
        public static final byte SET_BEBUG_CONFIG      = (byte)0xDB;
        public static final byte CLOSE_SESSION         = (byte)0xFF;
    }

    public class Names {
        public static final String OPEN_SESSION          = "Открытие сессии";
        public static final String SET_METER_ADDRESS     = "Установка кастомного MAC-адреса";
        public static final String LEAVE_NETWORK         = "Изгнание узла из сети";
        public static final String NODE_REBOOT           = "Перезагрузка узла";
        public static final String DISCOVER_NETWORK      = "Получение списка активных узлов";
        public static final String PERMIT_NETWORK_JOIN   = "Разрешить/запретить подключение новых узлов";
        public static final String GET_NETWORK_INFO      = "Получение основной информации о сети";
        public static final String RECREATE_NETWORK      = "Пересоздание сети на случайном канале";
        public static final String GET_CHILD_TABLE       = "Получение перечня подключавшихся узлов";
        public static final String GET_NODE_VERSION      = "Получение версии прошивки узла";
        public static final String LAUNCH_BOOTLOADER     = "Загрузка обновления для узла";
        public static final String UPLOAD_BOOTLOADER     = "Отправка обновления узлу";
        public static final String RECREATE_NETWORK_EX   = "Пересоздание сети на заданном канале";
        public static final String SINK_COMMAND_MYRMIDON = "Отправка ПИРС-запросов";
        public static final String SET_BEBUG_CONFIG      = "Установка отладочного режима";
        public static final String CLOSE_SESSION         = "Закрытие сессии";

        public static final String UNKNOWN               = "Unknown command";
    }
}
