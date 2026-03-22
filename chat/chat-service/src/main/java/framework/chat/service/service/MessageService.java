package framework.chat.service.service;

import framework.chat.service.domain.DTO.*;
import framework.chat.service.domain.VO.MessageVO;
import framework.chat.service.domain.entity.Message;

import java.util.List;

public interface MessageService {
    MessageDTO get(Long messageId);

    boolean add(MessageSendReqDTO messageSendReqDTO);

    List<MessageVO> list(MessageListReqDTO messageListReqDTO);

    void batchVisited(MessageVisitedReqDTO messageVisitedReqDTO);

    void batchRead(MessageReadReqDTO messageReadReqDTO);
}
