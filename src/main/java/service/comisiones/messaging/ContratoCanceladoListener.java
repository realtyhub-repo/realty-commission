package service.comisiones.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import service.comisiones.config.RabbitConfig;
import service.comisiones.dto.event.ContractCancelledEvent;
import service.comisiones.service.ComisionRegistroService;

@Component
@RequiredArgsConstructor
public class ContratoCanceladoListener {

    private final ComisionRegistroService registroService;

    @RabbitListener(queues = RabbitConfig.QUEUE_CONTRACT_CANCELLED)
    public void alCancelarseContrato(ContractCancelledEvent evento) {
        registroService.anularPorContrato(evento);
    }
}
