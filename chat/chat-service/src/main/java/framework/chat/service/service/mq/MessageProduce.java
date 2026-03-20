package framework.chat.service.service.mq;

import framework.chat.service.config.RabbitMqConfig;
import framework.chat.service.domain.DTO.MessageSendReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageProduce {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(MessageSendReqDTO messageSendReqDTO) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, "", messageSendReqDTO);
        } catch (Exception e) {
            log.error("咨询消息发送异常");
        }
    }
}
