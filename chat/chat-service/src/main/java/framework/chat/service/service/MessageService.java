package framework.chat.service.service;

import framework.chat.service.domain.DTO.MessageDTO;
import framework.chat.service.domain.DTO.MessageSendReqDTO;
import framework.chat.service.domain.entity.Message;

public interface MessageService {
    MessageDTO get(Long messageId);

    boolean add(MessageSendReqDTO messageSendReqDTO);
}
