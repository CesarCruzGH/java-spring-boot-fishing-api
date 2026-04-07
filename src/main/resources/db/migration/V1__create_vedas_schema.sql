-- V2__create_vedas_schema.sql

-- 0. Tabla Zona
CREATE TABLE IF NOT EXISTS zona (
    id                      BIGINT PRIMARY KEY,
    nombre                  VARCHAR(200) NOT NULL,
    macro_zona              VARCHAR(20) NOT NULL,
    tipo_restriccion        VARCHAR(30) NOT NULL,
    categoria_hidrica       VARCHAR(20) NOT NULL,
    es_anp                  BOOLEAN DEFAULT FALSE,
    municipio_sede           VARCHAR(100),
    notas_especificas       TEXT
);

-- 1. Tabla Pez
CREATE TABLE IF NOT EXISTS pez (
    id                  BIGINT PRIMARY KEY,
    nombre_comun        VARCHAR(100) NOT NULL,
    nombre_cientifico   VARCHAR(150),
    nombre_maya         VARCHAR(50),
    descripcion         TEXT,
    riesgo_ciguatera    VARCHAR(10),
    es_invasiva         BOOLEAN DEFAULT FALSE,
    es_protegida        BOOLEAN DEFAULT FALSE,
    tipo_agua           VARCHAR(10),
    migratorio          BOOLEAN DEFAULT FALSE
);

-- 2. Tabla Regulacion
CREATE TABLE IF NOT EXISTS regulacion (
    id                  BIGINT PRIMARY KEY,
    pez_id              BIGINT NOT NULL,
    zona_id             BIGINT NOT NULL,
    categoria_pesca     VARCHAR(30) NOT NULL,
    talla_minima        DECIMAL(5,2),
    talla_maxima        DECIMAL(5,2),
    tipo_medicion       VARCHAR(30),
    cuota_diaria        INT,
    requiere_permiso    BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_regulacion_pez FOREIGN KEY (pez_id) REFERENCES pez(id),
    CONSTRAINT fk_regulacion_zona FOREIGN KEY (zona_id) REFERENCES zona(id)
);

-- 3. Tabla Periodo Veda
CREATE TABLE IF NOT EXISTS periodo_veda (
    id                  BIGINT PRIMARY KEY,
    regulacion_id       BIGINT NOT NULL,
    tipo_veda           VARCHAR(20) NOT NULL,
    mes_inicio          TINYINT,
    dia_inicio          TINYINT,
    mes_fin             TINYINT,
    dia_fin             TINYINT,
    fuente_dof          VARCHAR(255),
    CONSTRAINT fk_periodo_veda_regulacion FOREIGN KEY (regulacion_id) REFERENCES regulacion(id)
);

-- 4. Tabla Ingestion Log
CREATE TABLE IF NOT EXISTS ingestions_logs (
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

-- 5. Tabla Arte Pesca
CREATE TABLE IF NOT EXISTS arte_pesca (
    id                  BIGINT PRIMARY KEY,
    regulacion_id       BIGINT NOT NULL,
    nombre              VARCHAR(200) NOT NULL,
    es_prohibido        BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_arte_pesca_regulacion FOREIGN KEY (regulacion_id) REFERENCES regulacion(id)
);

