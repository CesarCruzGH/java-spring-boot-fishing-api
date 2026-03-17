-- V2__create_vedas_schema.sql

-- 1. Tabla Pez
CREATE TABLE IF NOT EXISTS pez (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_comun      VARCHAR(100) NOT NULL,
    especie      VARCHAR(100) NOT NULL,
    nombre_maya        VARCHAR(30),
    talla        VARCHAR(30),
    habitad        VARCHAR(30),
    tecnica_recomendada   VARCHAR(20),
    );

-- 2. Tabla Especie Veda
CREATE TABLE IF NOT EXISTS especie_veda (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    pez_id              BIGINT NOT NULL,
    zona                VARCHAR(30) NOT NULL,
    tipo_veda           VARCHAR(20) NOT NULL, -- Cambiado de ENUM a VARCHAR para H2

-- Fechas absolutas
    inicio_fijo         DATE,
    fin_fijo            DATE,

    -- Mes-día (Se eliminó UNSIGNED porque no existe en H2)
    inicio_mes          TINYINT,
    inicio_dia          TINYINT,
    fin_mes             TINYINT,
    fin_dia             TINYINT,

    -- Override manual
    cancelada           BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_cancelacion  VARCHAR(255),
    cancelada_en        TIMESTAMP, -- H2 prefiere TIMESTAMP sobre DATETIME

-- Auditoría
    fuente_pdf          VARCHAR(255),
    creado_en           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMP,

    CONSTRAINT fk_veda_pez FOREIGN KEY (pez_id) REFERENCES pez(id)
    );

-- 3. Tabla Ingestion Log
CREATE TABLE IF NOT EXISTS ingestion_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_archivo  VARCHAR(255) NOT NULL,
    hash_sha256     CHAR(64) NOT NULL UNIQUE,
    total_filas     INT,
    filas_exitosas  INT,
    filas_error     INT,
    estado          VARCHAR(20) NOT NULL, -- Cambiado de ENUM a VARCHAR
    procesado_en    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detalle_error   CLOB -- TEXT o CLOB es mejor para errores largos que VARCHAR
    );

-- 4. Índices (En H2 y SQL estándar se crean fuera del CREATE TABLE)
CREATE INDEX idx_veda_zona_tipo ON especie_veda (zona, tipo_veda);
CREATE INDEX idx_veda_fechas_fijas ON especie_veda (inicio_fijo, fin_fijo);
CREATE INDEX idx_veda_ciclo ON especie_veda (inicio_mes, inicio_dia, fin_mes, fin_dia);
