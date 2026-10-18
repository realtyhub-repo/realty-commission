package service.comisiones.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import service.comisiones.config.ComisionesProperties;
import service.comisiones.config.RabbitConfig;
import service.comisiones.entity.OutboxEvento;
import service.comisiones.repository.OutboxEventoRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventoRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ComisionesProperties propiedades;

    @Scheduled(fixedDelayString = "${comisiones.outbox.intervalo-ms:5000}")
    @Transactional
    public void publicarPendientes() {
        List<OutboxEvento> pendientes = outboxRepository.reclamarPendientes(propiedades.outbox().lote());

        for (OutboxEvento evento : pendientes) {
            try {
                enviar(evento);
                evento.setPublicadoEn(LocalDateTime.now());
            } catch (RuntimeException e) {
                evento.setIntentos(evento.getIntentos() + 1);
                log.warn("No se pudo publicar {} ({}): {}", evento.getRoutingKey(), evento.getId(), e.getMessage());
                break;
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarPublicados() {
        LocalDateTime limite = LocalDateTime.now().minusDays(propiedades.outbox().retencionDias());
        int eliminados = outboxRepository.eliminarPublicadosAnteriores(limite);
        if (eliminados > 0) {
            log.info("Outbox: {} eventos publicados eliminados", eliminados);
        }
    }

    private void enviar(OutboxEvento evento) {
        Message mensaje = MessageBuilder
                .withBody(evento.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setMessageId(evento.getId().toString())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();

        long espera = propiedades.outbox().confirmacionMs();

        rabbitTemplate.invoke(operaciones -> {
            operaciones.send(RabbitConfig.EXCHANGE, evento.getRoutingKey(), mensaje);
            operaciones.waitForConfirmsOrDie(espera);
            return null;
        });
    }
}
