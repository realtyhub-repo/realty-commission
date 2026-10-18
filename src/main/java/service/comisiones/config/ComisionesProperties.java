package service.comisiones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "comisiones")
public record ComisionesProperties(Outbox outbox) {

    public record Outbox(long intervaloMs, int lote, long confirmacionMs, int retencionDias) {
    }
}
