package service.comisiones.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import service.comisiones.config.RabbitConfig;
import service.comisiones.dto.event.ContractSignedEvent;
import service.comisiones.exceptions.NoReintentableException;
import service.comisiones.service.ComisionRegistroService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContratoFirmadoListener {

    private final ComisionRegistroService registroService;

    @RabbitListener(queues = RabbitConfig.QUEUE_CONTRACT_SIGNED)
    public void alFirmarseContrato(ContractSignedEvent evento) {
        try {
            registroService.registrarDesdeContrato(evento);
        } catch (NoReintentableException e) {
            log.error("Evento contract.signed enviado a la DLQ (contrato {}): {}",
                    evento.contratoId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
