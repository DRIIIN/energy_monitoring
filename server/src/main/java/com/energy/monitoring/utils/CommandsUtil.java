package com.energy.monitoring.utils;

import java.util.Arrays;

/* Инструменты для работы с сообщениями координатора*/
public class CommandsUtil { 
    // Формирует координатору запрос с кодом commandCode и параметрами parameters
    public static byte[] createRequest(byte commandCode, byte[] parameters) {
        int parametersLen = parameters != null ? parameters.length : 0;

        byte[] command = new byte[5 + parametersLen];
        command[0] = (byte) 0xAA;
        command[1] = (byte) 0x01;
        command[2] = (byte) commandCode;
        command[3] = (byte) parametersLen;
        if (parameters != null) {
            System.arraycopy(parameters, 0, command, 4, parametersLen);
        }

        command[command.length - 1] = CrcUtil.calculateCRC(Arrays.copyOfRange(command, 1, command.length - 1));

        return command;
    }

    // Принимает ответ от координатора response и возвращает набор байт, где первый - код команды, на которую был ответ, а затем все параметры 
    public static byte[] parseResponse(byte[] response) {
        final int MIN_LENGTH_OF_CORRECT_MESSAGE = 5;
        if (response.length >= MIN_LENGTH_OF_CORRECT_MESSAGE) {
            byte[] message;
            if (response[2] != 7) {
                message = new byte[response.length - 4];
                message[0] = response[2];
                System.arraycopy(response, 4, message, 1, response.length - 5);
            } else {
                final int LEN_OF_ONE_UNSWER = 13;
                int numberOfMeters = response.length / 18;
                message = new byte[1 + LEN_OF_ONE_UNSWER * numberOfMeters];
                message[0] = response[2];
                for (int i = 0; i < numberOfMeters; i++) {
                    System.arraycopy(response, 4 + (LEN_OF_ONE_UNSWER + MIN_LENGTH_OF_CORRECT_MESSAGE) * i, message, LEN_OF_ONE_UNSWER * i + 1, LEN_OF_ONE_UNSWER);
                }
            }

            return message;
        }

        return null;
    }
}
