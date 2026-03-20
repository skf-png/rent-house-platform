package framework.chat.service.service.mq;

import framework.chat.service.config.RabbitMqConfig;
import framework.chat.service.domain.DTO.MessageSendReqDTO;
import framework.chat.service.domain.DTO.WebSocketDTO;
import framework.chat.service.domain.enums.WebSocketDataTypeEnum;
import framework.chat.service.service.websocket.WebSocketServer;
import framework.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(bindings = {@QueueBinding(value = @Queue(), exchange = @Exchange(value = RabbitMqConfig.EXCHANGE_NAME, type = ExchangeTypes.FANOUT))})
public class MessageForwardConsume {
    @RabbitHandler
    public void process(MessageSendReqDTO sendMessage) {
        try {
            WebSocketDTO<String> webSocketDTO = new WebSocketDTO<>();
            webSocketDTO.setType(WebSocketDataTypeEnum.CHAT.getType());
            webSocketDTO.setData(JsonUtils.ObjectToString(sendMessage));
            WebSocketServer.sendMessage(sendMessage.getToId(), webSocketDTO);
        } catch (Exception e) {
            log.error("转发消息失败", sendMessage.toString(), e);
        }
    }
}
