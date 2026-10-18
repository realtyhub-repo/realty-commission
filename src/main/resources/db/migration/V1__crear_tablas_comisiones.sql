CREATE TABLE acuerdo_comercial (
    id             UUID PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    vigente_desde  DATE         NOT NULL,
    vigente_hasta  DATE,
    creado_por     UUID         NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_acuerdo_rango CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde),
    CONSTRAINT ex_acuerdo_sin_solape EXCLUDE USING gist (daterange(vigente_desde, vigente_hasta, '[]') WITH &&)
);

CREATE TABLE regla_comision (
    id                  UUID PRIMARY KEY,
    acuerdo_id          UUID           NOT NULL REFERENCES acuerdo_comercial (id),
    tipo_contrato       VARCHAR(20)    NOT NULL,
    base_calculo        VARCHAR(20)    NOT NULL,
    valor               NUMERIC(12, 4) NOT NULL,
    porcentaje_agente   NUMERIC(5, 2)  NOT NULL,
    porcentaje_oficina  NUMERIC(5, 2)  NOT NULL,
    porcentaje_central  NUMERIC(5, 2)  NOT NULL,

    CONSTRAINT uk_regla_acuerdo_tipo UNIQUE (acuerdo_id, tipo_contrato),
    CONSTRAINT ck_regla_tipo CHECK (tipo_contrato IN ('ARRAS', 'ALQUILER', 'COMPRAVENTA')),
    CONSTRAINT ck_regla_base CHECK (base_calculo IN ('PORCENTAJE_PRECIO', 'MESES_CANON')),
    CONSTRAINT ck_regla_base_tipo CHECK (base_calculo <> 'MESES_CANON' OR tipo_contrato = 'ALQUILER'),
    CONSTRAINT ck_regla_valor CHECK (valor > 0),
    CONSTRAINT ck_regla_valor_porcentaje CHECK (base_calculo <> 'PORCENTAJE_PRECIO' OR valor <= 100),
    CONSTRAINT ck_regla_reparto CHECK (
        porcentaje_agente >= 0 AND porcentaje_oficina >= 0 AND porcentaje_central >= 0
        AND porcentaje_agente + porcentaje_oficina + porcentaje_central = 100
    )
);

CREATE TABLE comision (
    id                  UUID PRIMARY KEY,
    contrato_id         UUID           NOT NULL,
    lead_id             UUID           NOT NULL,
    propiedad_id        UUID           NOT NULL,
    cliente_id          UUID           NOT NULL,
    agente_id           UUID           NOT NULL,
    oficina_id          UUID           NOT NULL,
    central_id          UUID           NOT NULL,
    gerente_oficina_id  UUID,
    acuerdo_id          UUID           NOT NULL REFERENCES acuerdo_comercial (id),
    regla_id            UUID           NOT NULL REFERENCES regla_comision (id),
    tipo_contrato       VARCHAR(20)    NOT NULL,
    base_calculo        VARCHAR(20)    NOT NULL,
    valor_regla         NUMERIC(12, 4) NOT NULL,
    precio_base         NUMERIC(15, 2) NOT NULL,
    monto_total         NUMERIC(15, 2) NOT NULL,
    estado              VARCHAR(20)    NOT NULL,
    firmado_en          TIMESTAMP      NOT NULL,
    aprobada_por        UUID,
    aprobada_en         TIMESTAMP,
    pagada_por          UUID,
    pagada_en           TIMESTAMP,
    referencia_pago     VARCHAR(120),
    version             BIGINT         NOT NULL DEFAULT 0,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_comision_contrato UNIQUE (contrato_id),
    CONSTRAINT ck_comision_tipo CHECK (tipo_contrato IN ('ARRAS', 'ALQUILER', 'COMPRAVENTA')),
    CONSTRAINT ck_comision_base CHECK (base_calculo IN ('PORCENTAJE_PRECIO', 'MESES_CANON')),
    CONSTRAINT ck_comision_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'PAGADA', 'ANULADA', 'REVERTIDA')),
    CONSTRAINT ck_comision_precio CHECK (precio_base > 0),
    CONSTRAINT ck_comision_monto CHECK (monto_total >= 0),
    CONSTRAINT ck_comision_aprobada CHECK (
        estado NOT IN ('APROBADA', 'PAGADA') OR (aprobada_por IS NOT NULL AND aprobada_en IS NOT NULL)
    ),
    CONSTRAINT ck_comision_pagada CHECK (
        estado <> 'PAGADA' OR (pagada_por IS NOT NULL AND pagada_en IS NOT NULL AND referencia_pago IS NOT NULL)
    )
);

CREATE INDEX idx_comision_agente_id ON comision (agente_id);
CREATE INDEX idx_comision_gerente_oficina_id ON comision (gerente_oficina_id);
CREATE INDEX idx_comision_oficina_id ON comision (oficina_id);
CREATE INDEX idx_comision_estado ON comision (estado);
CREATE INDEX idx_comision_firmado_en ON comision (firmado_en);

CREATE TABLE comision_reparto (
    id                   UUID PRIMARY KEY,
    comision_id          UUID           NOT NULL REFERENCES comision (id),
    beneficiario_tipo    VARCHAR(10)    NOT NULL,
    beneficiario_id      UUID           NOT NULL,
    beneficiario_nombre  VARCHAR(200)   NOT NULL,
    porcentaje           NUMERIC(5, 2)  NOT NULL,
    monto                NUMERIC(15, 2) NOT NULL,

    CONSTRAINT uk_reparto_comision_tipo UNIQUE (comision_id, beneficiario_tipo),
    CONSTRAINT ck_reparto_tipo CHECK (beneficiario_tipo IN ('AGENTE', 'OFICINA', 'CENTRAL')),
    CONSTRAINT ck_reparto_porcentaje CHECK (porcentaje >= 0 AND porcentaje <= 100),
    CONSTRAINT ck_reparto_monto CHECK (monto >= 0)
);

CREATE INDEX idx_reparto_beneficiario ON comision_reparto (beneficiario_id);

CREATE TABLE comision_historial (
    id               UUID PRIMARY KEY,
    comision_id      UUID        NOT NULL REFERENCES comision (id),
    estado_anterior  VARCHAR(20),
    estado_nuevo     VARCHAR(20) NOT NULL,
    usuario_id       UUID,
    motivo           VARCHAR(500),
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_historial_anterior CHECK (
        estado_anterior IS NULL OR estado_anterior IN ('PENDIENTE', 'APROBADA', 'PAGADA', 'ANULADA', 'REVERTIDA')
    ),
    CONSTRAINT ck_historial_nuevo CHECK (estado_nuevo IN ('PENDIENTE', 'APROBADA', 'PAGADA', 'ANULADA', 'REVERTIDA'))
);

CREATE INDEX idx_historial_comision_id ON comision_historial (comision_id);

CREATE TABLE outbox_evento (
    id            UUID PRIMARY KEY,
    routing_key   VARCHAR(100) NOT NULL,
    payload       TEXT         NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    publicado_en  TIMESTAMP,
    intentos      INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_pendientes ON outbox_evento (created_at) WHERE publicado_en IS NULL;
CREATE INDEX idx_outbox_publicados ON outbox_evento (publicado_en) WHERE publicado_en IS NOT NULL;
