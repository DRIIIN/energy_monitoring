package com.energy.monitoring.utils;

import java.util.Arrays;

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
    public static byte[] sendCommandToCoordinator(String macAddress, byte commandCode, byte[] parameters) {
        logger.info("sendCommandToCoordinator");
        String portNumber = getCommPortByMacAddress(macAddress);
        logger.info("{}", portNumber);
        if (portNumber == null) {
            logger.error("Port with devise {} not founded", macAddress);

            return null;
        }

        SerialPort serialPort = SerialPort.getCommPort(portNumber);
        serialPort.setComPortParameters(Config.getInt(ConfigKeys.Uart.BAUND_RATE), Config.getInt(ConfigKeys.Uart.DATA_BITS), 
                                        Config.getInt(ConfigKeys.Uart.STOP_BITS), Config.getInt(ConfigKeys.Uart.PARITY));
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, Config.getInt(ConfigKeys.Uart.RESPONSE_TIMEOUT), 0);

        if (serialPort.openPort()) {
            logger.info("Serial port {} with {} opened successfully", portNumber, macAddress);

            byte[] request = CommandsUtil.createRequest(commandCode, parameters);
            serialPort.writeBytes(request, request.length);
            logger.info("Write request to {}: {}", macAddress, request);

            byte[] response = new byte[Config.getInt(ConfigKeys.Uart.MAX_LEN)];
            int responseLen = serialPort.readBytes(response, response.length);
            response = Arrays.copyOfRange(response, 0, responseLen);
            logger.info("Read respons from {}: {}", macAddress, response);

            serialPort.closePort();
            logger.info("Port {} with {} closed successfully", portNumber, macAddress);

            if (responseLen > 0) {
                // logger.info("Parsed response: {}", CommandsUtil.parseResponse(response));

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