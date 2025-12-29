package com.energy.monitoring.utils;

/* Инструменты для работы с сообщениями координатора*/
public class CommandsUtil { 
    // Формирует координатору запрос с кодом commandCode и параметрами parameters
    public static byte[] createRequest(int commandCode, byte[] parameters) {
        byte[] command = new byte[3 + parameters.length];
        command[0] = (byte) 0xAA;
        command[1] = (byte) 0x01;
        command[2] = (byte) commandCode;
        command[3] = (byte) parameters.length;
        System.arraycopy(parameters, 0, command, 4, parameters.length);
        
        byte[] commandWithCRC = CrcUtil.addCRC(command);

        return commandWithCRC;
    }

    // Принимает ответ от координатора response и возвращает набор байт, где первый - код команды, на которую был ответ, а затем все параметры 
    public static byte[] parseResponse(byte[] response) {
        if (response.length >= 5) {
            byte[] message = new byte[response.length - 4];
            message[0] = response[3];
            System.arraycopy(response, 4, message, 1, response.length - 5);

            return message;
        }

        return null;
    }
}
