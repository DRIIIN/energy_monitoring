-- PostgreSQL Script for Energy Monitoring Database
-- Created: Sat Dec 27 14:33:16 2025

-- Создание базы данных (выполняется отдельно)
-- CREATE DATABASE energy_monitoring_database 
--     WITH ENCODING = 'UTF8' 
--     LC_COLLATE = 'en_US.UTF-8' 
--     LC_CTYPE = 'en_US.UTF-8';

-- Подключение к базе данных (выполняется отдельно)
-- \c energy_monitoring_database;

-- -----------------------------------------------------
-- Table users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  login VARCHAR(128) NOT NULL,
  password VARCHAR(256) NOT NULL,
  is_active SMALLINT NOT NULL DEFAULT 0 CHECK (is_active IN (0, 1))
);

-- -----------------------------------------------------
-- Table coordinators
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS coordinators (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  name VARCHAR(128) NOT NULL,
  mac VARCHAR(16) NOT NULL,
  ip VARCHAR(15) NULL,
  port SMALLINT NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'offline' CHECK (status IN ('online', 'offline', 'error')),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_seen TIMESTAMP NULL
);

-- Создание индекса для таблицы coordinators
CREATE INDEX IF NOT EXISTS coordinators_user_id_idx ON coordinators(user_id);

-- -----------------------------------------------------
-- Table meters
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS meters (
  id SERIAL PRIMARY KEY,
  coordinator_id INT NOT NULL REFERENCES coordinators(id) ON DELETE CASCADE ON UPDATE CASCADE,
  zb_long_addr VARCHAR(23) NOT NULL,
  zb_short_addr SMALLINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'offline' CHECK (status IN ('online', 'offline', 'error')),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_seen TIMESTAMP NULL,
  voltage DECIMAL(10,2) NULL,
  current DECIMAL(10,2) NULL,
  active_power DECIMAL(10,2) NULL,
  reactive_power DECIMAL(10,2) NULL,
  apparent_power DECIMAL(10,2) NULL,
  power_factor DECIMAL(10,2) NULL,
  frequency DECIMAL(10,2) NULL,
  neutral_current DECIMAL(10,2) NULL
);

-- Создание индекса для таблицы meters
CREATE INDEX IF NOT EXISTS meters_coordinator_id_idx ON meters(coordinator_id);

-- -----------------------------------------------------
-- Table logs
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS logs (
  id SERIAL PRIMARY KEY,
  coordinator_id INT NOT NULL REFERENCES coordinators(id) ON DELETE CASCADE ON UPDATE CASCADE,
  meter_id INT NOT NULL REFERENCES meters(id) ON DELETE CASCADE ON UPDATE CASCADE,
  type VARCHAR(10) NOT NULL DEFAULT 'info' CHECK (type IN ('info', 'warning', 'error', 'command')),
  message TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов для таблицы logs
CREATE INDEX IF NOT EXISTS logs_coordinator_id_idx ON logs(coordinator_id);
CREATE INDEX IF NOT EXISTS logs_meter_id_idx ON logs(meter_id);