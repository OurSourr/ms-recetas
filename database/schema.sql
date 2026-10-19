
-- ============================================================
-- OUR SOUR - MICRO SERVICIO DE RECETAS Y DESCUBRIMIENTO
-- Esquema inicial de base de datos
-- MySQL 8.4
--
-- IMPORTANTE:
-- Ejecutar únicamente sobre una base de datos nueva o vacía.
-- Este script no elimina tablas ni registros existentes.
-- No es un script de migración de versiones.
-- ============================================================

CREATE DATABASE IF NOT EXISTS `our_sour_recetas`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `our_sour_recetas`;

-- ============================================================
-- 1. CATEGORIA_INGREDIENTE
-- ============================================================

CREATE TABLE `categoria_ingrediente` (
    `id_categoria` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(80) NOT NULL,
    `descripcion` VARCHAR(250) DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,

    PRIMARY KEY (`id_categoria`),
    UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 2. INGREDIENTE
-- ============================================================

CREATE TABLE `ingrediente` (
    `id_ingrediente` INT NOT NULL AUTO_INCREMENT,
    `id_categoria` INT NOT NULL,
    `nombre` VARCHAR(120) NOT NULL,
    `descripcion` VARCHAR(300) DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,

    PRIMARY KEY (`id_ingrediente`),

    UNIQUE KEY `nombre` (`nombre`),

    KEY `idx_ingrediente_categoria_activo`
        (`id_categoria`, `activo`),

    CONSTRAINT `fk_ingrediente_categoria`
        FOREIGN KEY (`id_categoria`)
        REFERENCES `categoria_ingrediente` (`id_categoria`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 3. RECETA
-- ============================================================

CREATE TABLE `receta` (
    `id_receta` INT NOT NULL AUTO_INCREMENT,
    `titulo` VARCHAR(160) NOT NULL,
    `descripcion` VARCHAR(600) DEFAULT NULL,
    `publicada` TINYINT(1) NOT NULL DEFAULT 0,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,

    PRIMARY KEY (`id_receta`),

    KEY `idx_receta_publicada_activo`
        (`publicada`, `activo`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 4. RECETA_INGREDIENTE
-- Tabla asociativa entre receta e ingrediente
-- ============================================================

CREATE TABLE `receta_ingrediente` (
    `id_receta` INT NOT NULL,
    `id_ingrediente` INT NOT NULL,
    `cantidad` DECIMAL(10,2) DEFAULT NULL,
    `unidad` VARCHAR(30) DEFAULT NULL,
    `obligatorio` TINYINT(1) NOT NULL DEFAULT 1,
    `orden` INT NOT NULL DEFAULT 1,

    PRIMARY KEY (`id_receta`, `id_ingrediente`),

    KEY `idx_receta_ingrediente_ingrediente`
        (`id_ingrediente`),

    CONSTRAINT `fk_receta_ingrediente_receta`
        FOREIGN KEY (`id_receta`)
        REFERENCES `receta` (`id_receta`),

    CONSTRAINT `fk_receta_ingrediente_ingrediente`
        FOREIGN KEY (`id_ingrediente`)
        REFERENCES `ingrediente` (`id_ingrediente`),

    CONSTRAINT `chk_receta_ingrediente_cantidad`
        CHECK (`cantidad` IS NULL OR `cantidad` > 0),

    CONSTRAINT `chk_receta_ingrediente_orden`
        CHECK (`orden` > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 5. PASO_RECETA
-- ============================================================

CREATE TABLE `paso_receta` (
    `id_paso` INT NOT NULL AUTO_INCREMENT,
    `id_receta` INT NOT NULL,
    `numero_paso` INT NOT NULL,
    `instruccion` TEXT NOT NULL,

    PRIMARY KEY (`id_paso`),

    UNIQUE KEY `uq_paso_receta_numero`
        (`id_receta`, `numero_paso`),

    CONSTRAINT `fk_paso_receta`
        FOREIGN KEY (`id_receta`)
        REFERENCES `receta` (`id_receta`),

    CONSTRAINT `chk_paso_receta_numero`
        CHECK (`numero_paso` > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 6. RECETA_PRODUCTO
-- Referencia a productos administrados por otro microservicio
-- No se define una FK hacia una base de datos externa.
-- ============================================================

CREATE TABLE `receta_producto` (
    `id_receta` INT NOT NULL,
    `id_producto_externo` INT NOT NULL,
    `principal` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`id_receta`, `id_producto_externo`),

    CONSTRAINT `fk_receta_producto_receta`
        FOREIGN KEY (`id_receta`)
        REFERENCES `receta` (`id_receta`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- FIN DEL ESQUEMA
-- ============================================================
