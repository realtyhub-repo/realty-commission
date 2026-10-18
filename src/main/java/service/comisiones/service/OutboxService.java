package service.comisiones.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import service.comisiones.entity.OutboxEvento;
import service.comisiones.repository.OutboxEventoRepository;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventoRepository outboxRepository;
    private final JacksonJsonMessageConverter converter;

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String routingKey, Object evento) {
        Message mensaje = converter.toMessage(evento, new MessageProperties());

        OutboxEvento registro = OutboxEvento.builder()
                .routingKey(routingKey)
                .payload(new String(mensaje.getBody(), StandardCharsets.UTF_8))
                .build();

        outboxRepository.save(registro);
    }
}
