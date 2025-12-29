"""
Эмулятор координатора ZigBee
"""

import socket
import threading
import time
import random
import struct
import json
import logging
import sys
import argparse
from datetime import datetime
from typing import Dict, List, Optional, Tuple, Any
from dataclasses import dataclass
from enum import IntEnum
import select

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('coordinator_emulator.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

# Команды ZigBee (из Таблицы 1)
class ZigBeeCommand(IntEnum):
    CMD_OPEN_SESSION             = 0x00
    CMD_SET_MAC_ADDRESS          = 0x01
    CMD_REMOVE_NODE              = 0x05
    CMD_REBOOT_NODE              = 0x06
    CMD_GET_ACTIVE_NODES         = 0x07
    CMD_SET_PERMIT_JOINING       = 0x08
    CMD_GET_NETWORK_INFO         = 0x09
    CMD_RECREATE_NETWORK_RANDOM  = 0x0B
    CMD_GET_ALL_NODES            = 0x0C
    CMD_GET_FIRMWARE_VERSION     = 0x19
    CMD_UPLOAD_FIRMWARE          = 0x1A
    CMD_SEND_FIRMWARE            = 0x1B
    CMD_RECREATE_NETWORK_CHANNEL = 0x23
    CMD_SEND_METER_REQUEST       = 0xAB
    CMD_SET_DEBUG_MODE           = 0xDB
    CMD_CLOSE_SESSION            = 0xFF

# Типы узлов
class NodeType(IntEnum):
    COORDINATOR       = 0x00
    ROUTER            = 0x01
    END_DEVICE        = 0x02
    SLEEPY_END_DEVICE = 0x03

# Коды ошибок
class ErrorCode(IntEnum):
    SUCCESS = 0x00
    INVALID_PARAMETERS = 0x01
    NODE_NOT_FOUND = 0x02
    COMMAND_NOT_SUPPORTED = 0x03
    NETWORK_ERROR = 0x04
    DEVICE_BUSY = 0x05

@dataclass
class MeterData:
    """Данные прибора учёта"""
    voltage: float  # В
    current: float  # А
    active_power: float  # кВт
    reactive_power: float  # кВар
    apparent_power: float  # кВ*А
    power_factor: float  # -
    frequency: float  # Гц
    neutral_current: float  # А
    
    def to_bytes(self) -> bytes:
        """Конвертировать в байтовый массив"""
        return struct.pack('>8d', 
            self.voltage,
            self.current,
            self.active_power,
            self.reactive_power,
            self.apparent_power,
            self.power_factor,
            self.frequency,
            self.neutral_current
        )
    
    def update_randomly(self):
        """Случайное обновление данных"""
        self.voltage += random.uniform(-1.0, 1.0)
        self.voltage = max(210.0, min(240.0, self.voltage))
        
        self.current += random.uniform(-0.5, 0.5)
        self.current = max(0.0, min(50.0, self.current))
        
        self.active_power += random.uniform(-50.0, 50.0)
        self.active_power = max(0.0, min(10000.0, self.active_power))
        
        self.reactive_power += random.uniform(-25.0, 25.0)
        self.reactive_power = max(0.0, min(5000.0, self.reactive_power))
        
        self.apparent_power = (self.active_power**2 + self.reactive_power**2)**0.5
        
        self.power_factor += random.uniform(-0.02, 0.02)
        self.power_factor = max(0.5, min(1.0, self.power_factor))
        
        self.frequency += random.uniform(-0.05, 0.05)
        self.frequency = max(49.5, min(50.5, self.frequency))
        
        self.neutral_current += random.uniform(-0.1, 0.1)
        self.neutral_current = max(0.0, min(5.0, self.neutral_current))

@dataclass
class ZigBeeNode:
    """Узел ZigBee сети"""
    long_address: int
    name: str
    short_address: int
    node_type: NodeType
    rssi: int
    lqi: int
    status: str = "online"
    firmware_version: str = "ZigBee-3.0-1.0.0"
    meter_data: Optional[MeterData] = None
    debug_mode: bool = False
    debug_level: int = 0
    last_seen: float = None
    
    def __post_init__(self):
        if self.last_seen is None:
            self.last_seen = time.time()
    
    def to_active_node_bytes(self) -> bytes:
        """Конвертировать в байты для списка активных узлов"""
        return struct.pack('>QHBBbB',
            self.long_address,
            self.short_address,
            self.node_type,
            0x00,  # reserved
            self.rssi,
            self.lqi
        )
    
    def to_all_nodes_bytes(self) -> bytes:
        """Конвертировать в байты для списка всех узлов"""
        status_byte = 0x03 if self.status == "online" else 0x00
        return struct.pack('>QB', self.long_address, status_byte)
    
    def get_firmware_bytes(self) -> bytes:
        """Получить байты версии прошивки"""
        firmware_bytes = self.firmware_version.encode('ascii')
        # Дополняем до 22 байт
        firmware_bytes = firmware_bytes.ljust(22, b'\x00')
        return struct.pack('>Q22s', self.long_address, firmware_bytes)

class ZigBeeCoordinatorEmulator:
    """Эмулятор координатора ZigBee"""
    
    def __init__(self, port: int = 8080, pan_id: int = 0x1234, 
                 network_channel: int = 11, extended_pan_id: str = None):
        self.port = port
        self.pan_id = pan_id
        self.network_channel = network_channel
        self.extended_pan_id = extended_pan_id or self._generate_extended_pan_id()
        self.permit_joining = True
        self.running = False
        self.server_socket = None
        self.active_connections = {}
        self.network_nodes: Dict[int, ZigBeeNode] = {}
        self.command_handlers = {}
        self.update_thread = None
        self.lock = threading.Lock()
        
        self._initialize_command_handlers()
        self._initialize_demo_nodes()
        
        logger.info(f"ZigBee Coordinator Emulator initialized")
        logger.info(f"PAN ID: 0x{pan_id:04X}")
        logger.info(f"Network Channel: {network_channel}")
        logger.info(f"Extended PAN ID: {self.extended_pan_id}")
    
    def _generate_extended_pan_id(self) -> str:
        """Сгенерировать случайный Extended PAN ID"""
        return ':'.join(f'{random.randint(0, 255):02X}' for _ in range(8))
    
    def _initialize_command_handlers(self):
        """Инициализация обработчиков команд"""
        self.command_handlers = {
            ZigBeeCommand.CMD_OPEN_SESSION: self._handle_open_session,
            ZigBeeCommand.CMD_SET_MAC_ADDRESS: self._handle_set_mac_address,
            ZigBeeCommand.CMD_REMOVE_NODE: self._handle_remove_node,
            ZigBeeCommand.CMD_REBOOT_NODE: self._handle_reboot_node,
            ZigBeeCommand.CMD_GET_ACTIVE_NODES: self._handle_get_active_nodes,
            ZigBeeCommand.CMD_SET_PERMIT_JOINING: self._handle_set_permit_joining,
            ZigBeeCommand.CMD_GET_NETWORK_INFO: self._handle_get_network_info,
            ZigBeeCommand.CMD_RECREATE_NETWORK_RANDOM: self._handle_recreate_network_random,
            ZigBeeCommand.CMD_GET_ALL_NODES: self._handle_get_all_nodes,
            ZigBeeCommand.CMD_GET_FIRMWARE_VERSION: self._handle_get_firmware_version,
            ZigBeeCommand.CMD_UPLOAD_FIRMWARE: self._handle_upload_firmware,
            ZigBeeCommand.CMD_SEND_FIRMWARE: self._handle_send_firmware,
            ZigBeeCommand.CMD_RECREATE_NETWORK_CHANNEL: self._handle_recreate_network_channel,
            ZigBeeCommand.CMD_SEND_METER_REQUEST: self._handle_send_meter_request,
            ZigBeeCommand.CMD_SET_DEBUG_MODE: self._handle_set_debug_mode,
            ZigBeeCommand.CMD_CLOSE_SESSION: self._handle_close_session,
        }
    
    def _initialize_demo_nodes(self):
        """Инициализация демонстрационных узлов"""
        demo_nodes = [
            ZigBeeNode(0x0000000000000001, "Router-1", 1, NodeType.ROUTER, -65, 95),
            ZigBeeNode(0x0000000000000002, "Meter-1", 2, NodeType.END_DEVICE, -72, 88),
            ZigBeeNode(0x0000000000000003, "Meter-2", 3, NodeType.END_DEVICE, -68, 92),
            ZigBeeNode(0x0000000000000004, "Router-2", 4, NodeType.ROUTER, -70, 90),
            ZigBeeNode(0x0000000000000005, "Meter-3", 5, NodeType.SLEEPY_END_DEVICE, -80, 80),
        ]
        
        for node in demo_nodes:
            if "Meter" in node.name:
                node.meter_data = MeterData(
                    voltage=220.0 + random.uniform(0, 10),
                    current=5.0 + random.uniform(0, 20),
                    active_power=1000.0 + random.uniform(0, 5000),
                    reactive_power=500.0 + random.uniform(0, 1000),
                    apparent_power=1200.0 + random.uniform(0, 5500),
                    power_factor=0.85 + random.uniform(0, 0.15),
                    frequency=49.8 + random.uniform(0, 0.4),
                    neutral_current=random.uniform(0, 2.0)
                )
            self.network_nodes[node.long_address] = node
        
        logger.info(f"Initialized {len(demo_nodes)} demo nodes")
    
    def start(self):
        """Запуск эмулятора"""
        if self.running:
            logger.warning("Coordinator emulator is already running")
            return
        
        try:
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind(('0.0.0.0', self.port))
            self.server_socket.listen(10)
            self.server_socket.settimeout(1.0)
            
            self.running = True
            
            # Запуск потока обновления данных
            self.update_thread = threading.Thread(target=self._update_meter_data, daemon=True)
            self.update_thread.start()
            
            # Запуск потока принятия подключений
            accept_thread = threading.Thread(target=self._accept_connections, daemon=True)
            accept_thread.start()
            
            logger.info("=" * 50)
            logger.info("ZigBee Coordinator Emulator Started")
            logger.info(f"Listening on port: {self.port}")
            logger.info(f"Nodes in network: {len(self.network_nodes)}")
            logger.info("=" * 50)
            
            return True
            
        except Exception as e:
            logger.error(f"Failed to start coordinator emulator: {e}")
            self.stop()
            return False
    
    def stop(self):
        """Остановка эмулятора"""
        if not self.running:
            return
        
        logger.info("Stopping coordinator emulator...")
        self.running = False
        
        # Закрываем все соединения
        with self.lock:
            for client_socket in list(self.active_connections.keys()):
                try:
                    client_socket.close()
                except:
                    pass
            self.active_connections.clear()
        
        # Закрываем серверный сокет
        if self.server_socket:
            try:
                self.server_socket.close()
            except:
                pass
        
        logger.info("Coordinator emulator stopped")
    
    def _accept_connections(self):
        """Принятие входящих подключений"""
        while self.running:
            try:
                client_socket, client_address = self.server_socket.accept()
                client_socket.settimeout(30.0)  # Таймаут 30 секунд
                
                logger.info(f"New connection from {client_address[0]}:{client_address[1]}")
                
                with self.lock:
                    self.active_connections[client_socket] = client_address
                
                # Обработка клиента в отдельном потоке
                client_thread = threading.Thread(
                    target=self._handle_client,
                    args=(client_socket, client_address),
                    daemon=True
                )
                client_thread.start()
                
            except socket.timeout:
                continue
            except Exception as e:
                if self.running:
                    logger.warning(f"Error accepting connection: {e}")
    
    def _handle_client(self, client_socket: socket.socket, client_address: Tuple[str, int]):
        """Обработка клиентского подключения"""
        client_ip, client_port = client_address
        
        try:
            # Отправляем ответ на открытие сессии
            self._send_response(client_socket, ZigBeeCommand.CMD_OPEN_SESSION, bytes([ErrorCode.SUCCESS]))
            logger.info(f"Session opened with {client_ip}:{client_port}")
            
            while self.running and not client_socket._closed:
                try:
                    # Читаем команду
                    data = self._receive_data(client_socket, 2)  # Читаем код команды и длину
                    if not data:
                        break
                    
                    command_code = data[0]
                    param_length = data[1]
                    
                    # Читаем параметры
                    parameters = b''
                    if param_length > 0:
                        parameters = self._receive_data(client_socket, param_length)
                        if len(parameters) != param_length:
                            logger.warning(f"Incomplete parameters from {client_ip}")
                            break
                    
                    logger.info(f"Command 0x{command_code:02X} from {client_ip}, params: {len(parameters)} bytes")
                    
                    # Обработка команды
                    response = self._process_command(command_code, parameters)
                    
                    # Отправляем ответ
                    self._send_response(client_socket, command_code, response)
                    
                    # Если команда закрытия сессии
                    if command_code == ZigBeeCommand.CMD_CLOSE_SESSION:
                        logger.info(f"Session closed by {client_ip}")
                        break
                    
                except socket.timeout:
                    continue
                except Exception as e:
                    logger.warning(f"Error handling client {client_ip}: {e}")
                    break
                    
        except Exception as e:
            logger.error(f"Error in client handler for {client_ip}: {e}")
        finally:
            with self.lock:
                if client_socket in self.active_connections:
                    del self.active_connections[client_socket]
            
            try:
                client_socket.close()
            except:
                pass
            
            logger.info(f"Connection closed: {client_ip}:{client_port}")
    
    def _receive_data(self, sock: socket.socket, length: int) -> bytes:
        """Чтение данных из сокета"""
        data = b''
        while len(data) < length:
            try:
                chunk = sock.recv(length - len(data))
                if not chunk:
                    break
                data += chunk
            except socket.timeout:
                break
        return data
    
    def _send_response(self, sock: socket.socket, command_code: int, response: bytes):
        """Отправка ответа клиенту"""
        try:
            sock.sendall(response)
        except Exception as e:
            logger.warning(f"Error sending response: {e}")
    
    def _process_command(self, command_code: int, parameters: bytes) -> bytes:
        """Обработка команды"""
        handler = self.command_handlers.get(command_code)
        if handler:
            try:
                return handler(parameters)
            except Exception as e:
                logger.error(f"Error in command handler 0x{command_code:02X}: {e}")
                return bytes([ErrorCode.NETWORK_ERROR])
        else:
            logger.warning(f"Unknown command: 0x{command_code:02X}")
            return bytes([ErrorCode.COMMAND_NOT_SUPPORTED])
    
    # ========== ОБРАБОТЧИКИ КОМАНД ==========
    
    def _handle_open_session(self, parameters: bytes) -> bytes:
        """Обработка открытия сессии"""
        return bytes([ErrorCode.SUCCESS])
    
    def _handle_set_mac_address(self, parameters: bytes) -> bytes:
        """Установка кастомного MAC-адреса"""
        if len(parameters) != 16:  # 8 + 8 байт
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        old_address = struct.unpack('>Q', parameters[0:8])[0]
        new_address = struct.unpack('>Q', parameters[8:16])[0]
        
        with self.lock:
            if old_address in self.network_nodes:
                node = self.network_nodes[old_address]
                node.long_address = new_address
                self.network_nodes[new_address] = node
                del self.network_nodes[old_address]
                
                logger.info(f"MAC address changed: 0x{old_address:016X} -> 0x{new_address:016X}")
                return bytes([ErrorCode.SUCCESS])
        
        return bytes([ErrorCode.NODE_NOT_FOUND])
    
    def _handle_remove_node(self, parameters: bytes) -> bytes:
        """Удаление узла из сети"""
        if len(parameters) != 8:
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        address = struct.unpack('>Q', parameters)[0]
        
        with self.lock:
            if address in self.network_nodes:
                del self.network_nodes[address]
                logger.info(f"Node removed: 0x{address:016X}")
                return bytes([ErrorCode.SUCCESS])
        
        return bytes([ErrorCode.NODE_NOT_FOUND])
    
    def _handle_reboot_node(self, parameters: bytes) -> bytes:
        """Перезагрузка узла"""
        if len(parameters) != 8:
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        address = struct.unpack('>Q', parameters)[0]
        
        with self.lock:
            if address in self.network_nodes:
                node = self.network_nodes[address]
                node.status = "rebooting"
                logger.info(f"Node reboot initiated: {node.name}")
                
                # Эмуляция перезагрузки
                def reboot_complete():
                    time.sleep(2.0)
                    with self.lock:
                        if address in self.network_nodes:
                            self.network_nodes[address].status = "online"
                
                threading.Thread(target=reboot_complete, daemon=True).start()
                
                return bytes([ErrorCode.SUCCESS])
        
        return bytes([ErrorCode.NODE_NOT_FOUND])
    
    def _handle_get_active_nodes(self, parameters: bytes) -> bytes:
        """Получение списка активных узлов"""
        with self.lock:
            active_nodes = [node for node in self.network_nodes.values() 
                          if node.status == "online"]
            
            response = bytes([ErrorCode.SUCCESS])
            for node in active_nodes:
                response += node.to_active_node_bytes()
            
            logger.info(f"Active nodes requested, returning {len(active_nodes)} nodes")
            return response
    
    def _handle_set_permit_joining(self, parameters: bytes) -> bytes:
        """Разрешить/запретить подключение новых узлов"""
        if len(parameters) != 1:
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        self.permit_joining = parameters[0] == 0x01
        logger.info(f"Permit joining set to: {self.permit_joining}")
        return bytes([ErrorCode.SUCCESS])
    
    def _handle_get_network_info(self, parameters: bytes) -> bytes:
        """Получение основной информации о сети"""
        # Конвертируем Extended PAN ID в байты
        epan_bytes = bytes.fromhex(self.extended_pan_id.replace(':', ''))
        
        response = struct.pack('>BBQBB',
            ErrorCode.SUCCESS,
            self.network_channel,
            self.pan_id,
            *epan_bytes,
            0x01 if self.permit_joining else 0x00
        )
        
        logger.info("Network info requested")
        return response
    
    def _handle_recreate_network_random(self, parameters: bytes) -> bytes:
        """Пересоздание сети на случайном канале"""
        old_channel = self.network_channel
        self.network_channel = random.randint(11, 26)
        self.pan_id = random.randint(0x0000, 0xFFFF)
        self.extended_pan_id = self._generate_extended_pan_id()
        
        logger.info(f"Network recreated: channel {old_channel} -> {self.network_channel}, "
                   f"PAN ID: 0x{self.pan_id:04X}")
        
        # Эмуляция переподключения узлов
        with self.lock:
            for node in self.network_nodes.values():
                node.status = "reconnecting"
        
        def reconnect_nodes():
            time.sleep(3.0)
            with self.lock:
                for node in self.network_nodes.values():
                    node.status = "online"
        
        threading.Thread(target=reconnect_nodes, daemon=True).start()
        
        return bytes([ErrorCode.SUCCESS])
    
    def _handle_get_all_nodes(self, parameters: bytes) -> bytes:
        """Получение перечня подключавшихся узлов"""
        with self.lock:
            response = bytes([ErrorCode.SUCCESS])
            for node in self.network_nodes.values():
                response += node.to_all_nodes_bytes()
            
            logger.info(f"All nodes requested, returning {len(self.network_nodes)} nodes")
            return response
    
    def _handle_get_firmware_version(self, parameters: bytes) -> bytes:
        """Получение версии прошивки узла"""
        if len(parameters) != 8:
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        address = struct.unpack('>Q', parameters)[0]
        
        with self.lock:
            if address in self.network_nodes:
                node = self.network_nodes[address]
                response = node.get_firmware_bytes()
                logger.info(f"Firmware version requested for node: {node.name}")
                return bytes([ErrorCode.SUCCESS]) + response
        
        return bytes([ErrorCode.NODE_NOT_FOUND])
    
    def _handle_upload_firmware(self, parameters: bytes) -> bytes:
        """Загрузка обновления для узла"""
        logger.info(f"Firmware upload started, size: {len(parameters)} bytes")
        
        # Эмуляция загрузки
        time.sleep(1.0)
        logger.info("Firmware upload completed")
        
        return bytes([ErrorCode.SUCCESS])
    
    def _handle_send_firmware(self, parameters: bytes) -> bytes:
        """Отправка обновления узлу"""
        if len(parameters) < 9:  # 8 байт адрес + минимум 1 байт данных
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        address = struct.unpack('>Q', parameters[0:8])[0]
        firmware_data = parameters[8:]
        
        with self.lock:
            if address in self.network_nodes:
                node = self.network_nodes[address]
                logger.info(f"Sending firmware to node: {node.name}, "
                          f"data size: {len(firmware_data)} bytes")
                
                # Эмуляция обновления прошивки
                def update_firmware():
                    node.status = "updating"
                    time.sleep(5.0)
                    with self.lock:
                        if address in self.network_nodes:
                            node.firmware_version = f"ZigBee-3.0-{random.randint(1, 5)}." \
                                                   f"{random.randint(1, 5)}." \
                                                   f"{random.randint(0, 9)}"
                            node.status = "online"
                            logger.info(f"Firmware update completed for node: {node.name}")
                
                threading.Thread(target=update_firmware, daemon=True).start()
                
                return bytes([ErrorCode.SUCCESS])
        
        return bytes([ErrorCode.NODE_NOT_FOUND])
    
    def _handle_recreate_network_channel(self, parameters: bytes) -> bytes:
        """Пересоздание сети на заданном канале"""
        if len(parameters) != 1:
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        new_channel = parameters[0]
        if new_channel < 11 or new_channel > 26:
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        old_channel = self.network_channel
        self.network_channel = new_channel
        
        logger.info(f"Network channel changed: {old_channel} -> {new_channel}")
        
        # Эмуляция переподключения
        with self.lock:
            for node in self.network_nodes.values():
                node.status = "channel changing"
        
        def change_channel_complete():
            time.sleep(2.0)
            with self.lock:
                for node in self.network_nodes.values():
                    node.status = "online"
        
        threading.Thread(target=change_channel_complete, daemon=True).start()
        
        return bytes([ErrorCode.SUCCESS])
    
    def _handle_send_meter_request(self, parameters: bytes) -> bytes:
        """Отправка ПИРС-запросов на прибор учёта"""
        if len(parameters) < 8:
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        address = struct.unpack('>Q', parameters[0:8])[0]
        request_data = parameters[8:]  # ПИРС-запрос
        
        with self.lock:
            if address in self.network_nodes:
                node = self.network_nodes[address]
                if node.meter_data:
                    logger.info(f"Meter request for node: {node.name}")
                    
                    # Формируем ответ с данными прибора учёта
                    meter_bytes = node.meter_data.to_bytes()
                    return bytes([ErrorCode.SUCCESS]) + meter_bytes
        
        return bytes([ErrorCode.NODE_NOT_FOUND])
    
    def _handle_set_debug_mode(self, parameters: bytes) -> bytes:
        """Установка отладочного режима узла"""
        if len(parameters) != 10:  # 8 байт адрес + 1 тип + 1 уровень
            return bytes([ErrorCode.INVALID_PARAMETERS])
        
        address = struct.unpack('>Q', parameters[0:8])[0]
        debug_type = parameters[8]
        debug_level = parameters[9]
        
        with self.lock:
            if address in self.network_nodes:
                node = self.network_nodes[address]
                node.debug_mode = True
                node.debug_level = debug_level
                
                logger.info(f"Debug mode set for node: {node.name}, "
                          f"type: {debug_type}, level: {debug_level}")
                return bytes([ErrorCode.SUCCESS])
        
        return bytes([ErrorCode.NODE_NOT_FOUND])
    
    def _handle_close_session(self, parameters: bytes) -> bytes:
        """Закрытие сессии"""
        logger.info("Closing session")
        return bytes([ErrorCode.SUCCESS])
    
    def _update_meter_data(self):
        """Периодическое обновление данных приборов учёта"""
        while self.running:
            time.sleep(10.0)  # Обновление каждые 10 секунд
            
            with self.lock:
                for node in self.network_nodes.values():
                    if node.meter_data:
                        node.meter_data.update_randomly()
                        
                        # Случайное изменение статуса (5% вероятность)
                        if random.random() < 0.05:
                            node.status = "online" if random.random() < 0.7 else "offline"
                        
                        # Обновление RSSI и LQI
                        node.rssi = random.randint(-80, -40)
                        node.lqi = random.randint(60, 100)
                        node.last_seen = time.time()
            
            logger.debug("Meter data updated for all nodes")
    
    def get_status(self) -> Dict[str, Any]:
        """Получить статус координатора"""
        with self.lock:
            return {
                "port": self.port,
                "pan_id": f"0x{self.pan_id:04X}",
                "network_channel": self.network_channel,
                "extended_pan_id": self.extended_pan_id,
                "permit_joining": self.permit_joining,
                "running": self.running,
                "active_connections": len(self.active_connections),
                "network_nodes": len(self.network_nodes),
            }
    
    def add_node(self, name: str, node_type: NodeType = NodeType.END_DEVICE):
        """Добавить новый узел в сеть"""
        with self.lock:
            # Генерируем уникальный адрес
            while True:
                address = random.randint(0x0006, 0xFFFFFFFFFFFFFFFF)
                if address not in self.network_nodes:
                    break
            
            short_address = len(self.network_nodes) + 6
            
            node = ZigBeeNode(
                long_address=address,
                name=name,
                short_address=short_address,
                node_type=node_type,
                rssi=random.randint(-80, -40),
                lqi=random.randint(60, 100)
            )
            
            if "Meter" in name:
                node.meter_data = MeterData(
                    voltage=220.0 + random.uniform(0, 10),
                    current=5.0 + random.uniform(0, 20),
                    active_power=1000.0 + random.uniform(0, 5000),
                    reactive_power=500.0 + random.uniform(0, 1000),
                    apparent_power=1200.0 + random.uniform(0, 5500),
                    power_factor=0.85 + random.uniform(0, 0.15),
                    frequency=49.8 + random.uniform(0, 0.4),
                    neutral_current=random.uniform(0, 2.0)
                )
            
            self.network_nodes[address] = node
            logger.info(f"Node added: {name} (0x{address:016X})")
            return node
    
    def remove_node(self, address_hex: str):
        """Удалить узел из сети"""
        try:
            address = int(address_hex, 16)
            with self.lock:
                if address in self.network_nodes:
                    node_name = self.network_nodes[address].name
                    del self.network_nodes[address]
                    logger.info(f"Node removed: {node_name} (0x{address:016X})")
                    return True
                else:
                    logger.warning(f"Node not found: 0x{address:016X}")
                    return False
        except ValueError:
            logger.error(f"Invalid address format: {address_hex}")
            return False
    
    def list_nodes(self):
        """Вывести список всех узлов"""
        with self.lock:
            if not self.network_nodes:
                print("No nodes in network")
                return
            
            print("\nNodes in network:")
            print("=" * 80)
            print(f"{'Name':<15} {'Address':<18} {'Type':<10} {'Status':<10} {'RSSI':<6} {'LQI':<6}")
            print("-" * 80)
            
            for node in self.network_nodes.values():
                print(f"{node.name:<15} 0x{node.long_address:016X} "
                      f"{node.node_type.name:<10} {node.status:<10} "
                      f"{node.rssi:<6} {node.lqi:<6}")
            print("=" * 80)
            print(f"Total: {len(self.network_nodes)} nodes")

class CoordinatorCLI:
    """Командный интерфейс для управления эмулятором"""
    
    def __init__(self):
        self.coordinator = None
        self.running = False
    
    def start(self):
        """Запуск CLI"""
        print("=" * 60)
        print("ZigBee Coordinator Emulator - Command Line Interface")
        print("=" * 60)
        
        port = self._get_port()
        self.coordinator = ZigBeeCoordinatorEmulator(port=port)
        
        if not self.coordinator.start():
            print("Failed to start coordinator")
            return
        
        self.running = True
        self._run_command_loop()
    
    def _get_port(self) -> int:
        """Получить порт от пользователя"""
        while True:
            try:
                port_input = input("Enter port number (default 8080): ").strip()
                if not port_input:
                    return 8080
                
                port = int(port_input)
                if 1 <= port <= 65535:
                    return port
                else:
                    print("Port must be between 1 and 65535")
            except ValueError:
                print("Invalid port number")
    
    def _run_command_loop(self):
        """Основной цикл команд"""
        self._show_help()
        
        while self.running:
            try:
                command = input("\ncoordinator> ").strip().lower()
                
                if not command:
                    continue
                
                if command == "exit":
                    self.stop()
                elif command == "help":
                    self._show_help()
                elif command == "status":
                    self._show_status()
                elif command == "list":
                    self._list_nodes()
                elif command.startswith("add "):
                    self._add_node(command[4:].strip())
                elif command.startswith("remove "):
                    self._remove_node(command[7:].strip())
                elif command == "nodes":
                    self._show_node_details()
                elif command == "simulate":
                    self._simulate_event()
                elif command == "restart":
                    self._restart_coordinator()
                elif command == "save":
                    self._save_config()
                elif command == "load":
                    self._load_config()
                else:
                    print(f"Unknown command: {command}")
                    print("Type 'help' for available commands")
                    
            except KeyboardInterrupt:
                print("\nInterrupted")
                self.stop()
                break
            except Exception as e:
                print(f"Error: {e}")
    
    def _show_help(self):
        """Показать справку по командам"""
        help_text = """
Available commands:
  help          - Show this help message
  status        - Show coordinator status
  list          - List all nodes in network
  nodes         - Show detailed node information
  add <name>    - Add new node to network
  remove <addr> - Remove node by address (hex)
  simulate      - Simulate network event
  restart       - Restart coordinator
  save          - Save configuration
  load          - Load configuration
  exit          - Stop coordinator and exit
        """
        print(help_text)
    
    def _show_status(self):
        """Показать статус координатора"""
        if not self.coordinator:
            print("Coordinator not initialized")
            return
        
        status = self.coordinator.get_status()
        
        print("\nCoordinator Status:")
        print("=" * 40)
        for key, value in status.items():
            print(f"{key.replace('_', ' ').title():<20}: {value}")
        print("=" * 40)
    
    def _list_nodes(self):
        """Список узлов"""
        if self.coordinator:
            self.coordinator.list_nodes()
    
    def _add_node(self, node_name: str):
        """Добавить узел"""
        if not node_name:
            print("Usage: add <node_name>")
            return
        
        print("Select node type:")
        print("  1. Router")
        print("  2. End Device")
        print("  3. Sleepy End Device")
        
        try:
            type_choice = input("Enter choice (default 2): ").strip()
            if not type_choice:
                type_choice = "2"
            
            type_map = {
                "1": NodeType.ROUTER,
                "2": NodeType.END_DEVICE,
                "3": NodeType.SLEEPY_END_DEVICE
            }
            
            node_type = type_map.get(type_choice, NodeType.END_DEVICE)
            
            node = self.coordinator.add_node(node_name, node_type)
            print(f"Node added successfully: {node.name} (0x{node.long_address:016X})")
            
        except Exception as e:
            print(f"Error adding node: {e}")
    
    def _remove_node(self, address: str):
        """Удалить узел"""
        if not address:
            print("Usage: remove <hex_address>")
            return
        
        if self.coordinator.remove_node(address):
            print("Node removed successfully")
        else:
            print("Failed to remove node")
    
    def _show_node_details(self):
        """Показать детальную информацию об узлах"""
        if not self.coordinator:
            return
        
        with self.coordinator.lock:
            if not self.coordinator.network_nodes:
                print("No nodes in network")
                return
            
            print("\nDetailed Node Information:")
            print("=" * 100)
            
            for node in self.coordinator.network_nodes.values():
                print(f"\nNode: {node.name}")
                print(f"  Address: 0x{node.long_address:016X}")
                print(f"  Short Address: {node.short_address}")
                print(f"  Type: {node.node_type.name}")
                print(f"  Status: {node.status}")
                print(f"  RSSI: {node.rssi} dBm, LQI: {node.lqi}")
                print(f"  Firmware: {node.firmware_version}")
                
                if node.meter_data:
                    print("  Meter Data:")
                    print(f"    Voltage: {node.meter_data.voltage:.2f} V")
                    print(f"    Current: {node.meter_data.current:.2f} A")
                    print(f"    Active Power: {node.meter_data.active_power:.2f} kW")
                    print(f"    Reactive Power: {node.meter_data.reactive_power:.2f} kVar")
                    print(f"    Apparent Power: {node.meter_data.apparent_power:.2f} kVA")
                    print(f"    Power Factor: {node.meter_data.power_factor:.3f}")
                    print(f"    Frequency: {node.meter_data.frequency:.2f} Hz")
                    print(f"    Neutral Current: {node.meter_data.neutral_current:.2f} A")
            
            print("=" * 100)
    
    def _simulate_event(self):
        """Симулировать сетевое событие"""
        print("\nSimulate Network Event:")
        print("  1. Node join")
        print("  2. Node leave")
        print("  3. Network interference")
        print("  4. Power outage")
        print("  5. Firmware update")
        
        choice = input("Enter choice: ").strip()
        
        if choice == "1":
            # Симуляция присоединения нового узла
            node_types = ["Router", "Meter", "Sensor", "Controller"]
            node_name = f"{random.choice(node_types)}-{random.randint(100, 999)}"
            self.coordinator.add_node(node_name)
            print(f"Simulated: Node {node_name} joined the network")
            
        elif choice == "2":
            # Симуляция выхода узла
            if self.coordinator.network_nodes:
                node = random.choice(list(self.coordinator.network_nodes.values()))
                self.coordinator.remove_node(f"{node.long_address:X}")
                print(f"Simulated: Node {node.name} left the network")
            else:
                print("No nodes to remove")
                
        elif choice == "3":
            # Симуляция помех в сети
            print("Simulated: Network interference detected")
            with self.coordinator.lock:
                for node in self.coordinator.network_nodes.values():
                    node.rssi -= random.randint(10, 30)
                    node.lqi -= random.randint(20, 40)
                    
        elif choice == "4":
            # Симуляция отключения питания
            print("Simulated: Power outage")
            with self.coordinator.lock:
                for node in self.coordinator.network_nodes.values():
                    if node.meter_data:
                        node.meter_data.voltage = 0.0
                        node.meter_data.current = 0.0
                        node.meter_data.active_power = 0.0
                        node.status = "offline"
                        
        elif choice == "5":
            # Симуляция обновления прошивки
            if self.coordinator.network_nodes:
                node = random.choice(list(self.coordinator.network_nodes.values()))
                print(f"Simulated: Firmware update for {node.name}")
                node.status = "updating"
                # Через 3 секунды завершаем обновление
                threading.Timer(3.0, lambda: setattr(node, 'status', 'online')).start()
    
    def _restart_coordinator(self):
        """Перезапустить координатор"""
        print("Restarting coordinator...")
        port = self.coordinator.port
        self.coordinator.stop()
        time.sleep(1)
        self.coordinator = ZigBeeCoordinatorEmulator(port=port)
        self.coordinator.start()
        print("Coordinator restarted")
    
    def _save_config(self):
        """Сохранить конфигурацию"""
        # TODO: Реализовать сохранение конфигурации
        print("Save configuration - Not implemented yet")
    
    def _load_config(self):
        """Загрузить конфигурацию"""
        # TODO: Реализовать загрузку конфигурации
        print("Load configuration - Not implemented yet")
    
    def stop(self):
        """Остановить координатор"""
        print("\nStopping coordinator...")
        if self.coordinator:
            self.coordinator.stop()
        self.running = False
        print("Goodbye!")

def main():
    """Основная функция"""
    parser = argparse.ArgumentParser(description='ZigBee Coordinator Emulator')
    parser.add_argument('--port', type=int, default=8080, help='Port to listen on')
    parser.add_argument('--cli', action='store_true', help='Start with command line interface')
    parser.add_argument('--pan-id', type=lambda x: int(x, 16), default=0x1234, 
                       help='PAN ID in hex (default: 0x1234)')
    parser.add_argument('--channel', type=int, default=11, 
                       help='Network channel (11-26, default: 11)')
    parser.add_argument('--log-level', default='INFO', 
                       choices=['DEBUG', 'INFO', 'WARNING', 'ERROR'],
                       help='Logging level')
    
    args = parser.parse_args()
    
    # Устанавливаем уровень логирования
    logging.getLogger().setLevel(getattr(logging, args.log_level))
    
    if args.cli:
        # Запуск с командным интерфейсом
        cli = CoordinatorCLI()
        cli.start()
    else:
        # Запуск простого эмулятора
        print("Starting ZigBee Coordinator Emulator...")
        print(f"Port: {args.port}")
        print(f"PAN ID: 0x{args.pan_id:04X}")
        print(f"Channel: {args.channel}")
        print("Press Ctrl+C to stop\n")
        
        coordinator = ZigBeeCoordinatorEmulator(
            port=args.port,
            pan_id=args.pan_id,
            network_channel=args.channel
        )
        
        def signal_handler(signum, frame):
            print("\nShutting down...")
            coordinator.stop()
            sys.exit(0)
        
        import signal
        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)
        
        if coordinator.start():
            # Держим программу активной
            try:
                while coordinator.running:
                    time.sleep(1)
            except KeyboardInterrupt:
                coordinator.stop()
        else:
            sys.exit(1)

if __name__ == "__main__":
    main()
