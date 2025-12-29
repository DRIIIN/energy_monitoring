#!/usr/bin/env python3
"""
Скрипт для быстрого запуска эмулятора координатора
"""

import sys
import os
import subprocess
import time
import argparse
from threading import Thread

def start_single_coordinator(port=8080, pan_id=0x1234, channel=11):
    """Запустить один координатор"""
    from zigbee_coordinator_emulator import ZigBeeCoordinatorEmulator
    
    print(f"\nStarting coordinator on port {port}...")
    print(f"PAN ID: 0x{pan_id:04X}")
    print(f"Channel: {channel}")
    print("=" * 50)
    
    coordinator = ZigBeeCoordinatorEmulator(
        port=port,
        pan_id=pan_id,
        network_channel=channel
    )
    
    if coordinator.start():
        print(f"Coordinator is running on port {port}")
        print("Press Ctrl+C to stop\n")
        
        try:
            while coordinator.running:
                time.sleep(1)
        except KeyboardInterrupt:
            print("\nStopping coordinator...")
            coordinator.stop()
    else:
        print("Failed to start coordinator")

def start_multiple_coordinators(base_port=9000, count=3):
    """Запустить несколько координаторов"""
    print(f"\nStarting {count} coordinators...")
    
    processes = []
    
    for i in range(count):
        port = base_port + i
        pan_id = 0x1000 + i
        channel = 11 + i
        
        cmd = [
            sys.executable, "zigbee_coordinator_emulator.py",
            "--port", str(port),
            "--pan-id", hex(pan_id),
            "--channel", str(channel)
        ]
        
        print(f"  Coordinator {i+1}: port={port}, PAN=0x{pan_id:04X}, channel={channel}")
        
        process = subprocess.Popen(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        processes.append((process, port))
        
        time.sleep(0.5)  # Небольшая задержка между запусками
    
    print(f"\nAll {count} coordinators started!")
    print(f"Ports: {base_port} - {base_port + count - 1}")
    print("\nPress Enter to stop all coordinators...")
    
    try:
        input()
    except KeyboardInterrupt:
        pass
    
    print("\nStopping all coordinators...")
    for process, port in processes:
        process.terminate()
        process.wait()
        print(f"  Coordinator on port {port} stopped")

def start_with_cli():
    """Запустить с командным интерфейсом"""
    from zigbee_coordinator_emulator import CoordinatorCLI
    
    cli = CoordinatorCLI()
    cli.start()

def test_connection(host='127.0.0.1', port=8080):
    """Тестирование подключения к координатору"""
    import socket
    
    print(f"\nTesting connection to {host}:{port}...")
    
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(2)
        sock.connect((host, port))
        
        # Отправляем тестовую команду (открытие сессии)
        sock.send(b'\x00\x00')
        
        response = sock.recv(1024)
        if response:
            print(f"✓ Successfully connected to coordinator")
            print(f"  Response: {response.hex()}")
            
            if response[0] == 0x00:
                print("  Status: Coordinator is operational")
            else:
                print(f"  Status: Error code 0x{response[0]:02X}")
        else:
            print("✗ No response from coordinator")
        
        sock.close()
        
    except ConnectionRefusedError:
        print(f"✗ Connection refused - coordinator not running on port {port}")
    except socket.timeout:
        print("✗ Connection timeout")
    except Exception as e:
        print(f"✗ Error: {e}")

def main():
    parser = argparse.ArgumentParser(description='ZigBee Coordinator Emulator Launcher')
    parser.add_argument('action', choices=['start', 'multi', 'cli', 'test'], 
                       nargs='?', default='start', help='Action to perform')
    parser.add_argument('--port', type=int, default=8080, help='Port number')
    parser.add_argument('--pan-id', type=lambda x: int(x, 16), default=0x1234,
                       help='PAN ID in hex')
    parser.add_argument('--channel', type=int, default=11, help='Network channel')
    parser.add_argument('--count', type=int, default=3, help='Number of coordinators')
    parser.add_argument('--base-port', type=int, default=9000, help='Base port for multiple')
    
    args = parser.parse_args()
    
    if args.action == 'start':
        start_single_coordinator(args.port, args.pan_id, args.channel)
    elif args.action == 'multi':
        start_multiple_coordinators(args.base_port, args.count)
    elif args.action == 'cli':
        start_with_cli()
    elif args.action == 'test':
        test_connection('127.0.0.1', args.port)

if __name__ == "__main__":
    main()
