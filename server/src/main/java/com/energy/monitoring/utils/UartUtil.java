package com.energy.monitoring.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.energy.monitoring.config.Config;
import com.energy.monitoring.config.ConfigKeys;
import com.fazecast.jSerialComm.SerialPort;

/* Инструменты для взаимодействия с координаторами по uart */
public class UartUtil {
    private static final Logger logger = LoggerFactory.getLogger(UartUtil.class); // Объект Logger для текущего класса
    
    // Возвращает Строку с названием com-порта, на котором висит координатор с mac-адресом macAddress, если такого нет - null 
    public static String getCommPortByMacAddress(String macAddress) {
        String[] macAddresses = Config.getString(ConfigKeys.Uart.MAC_ARDRESSES).split(",");
        String[] portNames    = Config.getString(ConfigKeys.Uart.PORT_NAMES).split(",");
        for (int i = 0; i < macAddress.length(); i++) {
            if (macAddresses[i].equals(macAddress) && portNames.length > i && portNames[i] != null) {
                return portNames[i];
            }
        }
        return null;
    }

    // Отправляет запрос c кодом commandCode и параметрами parameters на коорданатор с mac-адресом macAddress и возвращает ответ
    public static byte[] sendCommandToCoordinator(String macAddress, int commandCode, byte[] parameters) {
        String portNumber = getCommPortByMacAddress(macAddress);
        if (portNumber == null) {
            logger.error("Port with devise {} not founded", macAddress);

            return null;
        }

        SerialPort serialPort = SerialPort.getCommPort(portNumber);
        serialPort.setComPortParameters(Config.getInt(ConfigKeys.Uart.BAUND_RATE), Config.getInt(ConfigKeys.Uart.DATA_BITS), 
                                        Config.getInt(ConfigKeys.Uart.STOP_BITS), Config.getInt(ConfigKeys.Uart.PARITY));
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, Config.getInt(ConfigKeys.Uart.RESPONSE_TIMEOUT), 0);

        if (serialPort.openPort()) {
            logger.info("Serial port opened successfully");

            byte[] request = CommandsUtil.createRequest(commandCode, parameters);

            logger.info("Write request to {}: {}", macAddress, request);
            serialPort.writeBytes(request, request.length);

            byte[] response = new byte[Config.getInt(ConfigKeys.Uart.MAX_LEN)];
            int responseLen = serialPort.readBytes(response, response.length);

            if (responseLen > 0) {
                logger.info("Read response wrom {}: {}", macAddress, response);

                return CommandsUtil.parseResponse(response);
            } else {
                logger.warn("Not be reading data in UART");

                return null;
            }
        } else {
            logger.error("Port not be opened");

            return null;
        }
    }
}