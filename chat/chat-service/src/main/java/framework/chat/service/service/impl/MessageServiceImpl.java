package framework.chat.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import framework.chat.service.domain.DTO.MessageDTO;
import framework.chat.service.domain.DTO.MessageSendReqDTO;
import framework.chat.service.domain.DTO.SessionStatusDetailDTO;
import framework.chat.service.domain.entity.Message;
import framework.chat.service.domain.entity.Session;
import framework.chat.service.domain.enums.MessageStatusEnum;
import framework.chat.service.domain.enums.MessageTypeEnum;
import framework.chat.service.mapper.MessageMapper;
import framework.chat.service.mapper.SessionMapper;
import framework.chat.service.service.ChatCacheService;
import framework.chat.service.service.MessageService;
import framework.chat.service.service.SnowflakeIdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private SnowflakeIdService snowflakeIdService;
    @Autowired
    private ChatCacheService chatCacheService;

    @Override
    public MessageDTO get(Long messageId) {
        // 参数校验
        if (messageId==null){
            return null;
        }

        // 查询mysql
        Message message = messageMapper.selectById(messageId);
        if (message==null){
            return null;
        }

        // 返回
        MessageDTO messageDTO = new MessageDTO();
        BeanUtils.copyProperties(message,messageDTO);
        messageDTO.setMessageId(String.valueOf(message.getId()));

        return messageDTO;
    }

    @Override
    public boolean add(MessageSendReqDTO messageSendReqDTO) {
        // 参数校验，type是否存在
        if (messageSendReqDTO==null || messageSendReqDTO.getSessionId()==null
        || MessageTypeEnum.getByCode(messageSendReqDTO.getType())==null){
            log.error("消息为空，或者类型不存在，messageSendReqDTO{}", messageSendReqDTO.toString());
            return false;
        }

        // 查找session
        Session session = sessionMapper.selectById(messageSendReqDTO.getSessionId());
        if (session==null){
            log.error("session不存在{}", session.getId());
            return false;
        }

        // 新增消息
        Message message = new Message();
        message.setId(null == messageSendReqDTO.getMessageId()
                ? snowflakeIdService.nextId()
                : messageSendReqDTO.getMessageId());
        message.setFromId(messageSendReqDTO.getFromId());
        message.setSessionId(messageSendReqDTO.getSessionId());
        message.setType(messageSendReqDTO.getType());
        message.setContent(StringUtils.isEmpty(messageSendReqDTO.getContent())
                ? ""
                : messageSendReqDTO.getContent());
        message.setStatus(null == messageSendReqDTO.getStatus()
                ? MessageStatusEnum.MESSAGE_UNREAD.getCode()
                : messageSendReqDTO.getStatus());
        message.setVisited(null == messageSendReqDTO.getVisited()
                ? MessageStatusEnum.MESSAGE_NOT_VISITED.getCode()
                : messageSendReqDTO.getVisited());
        message.setCreateTime(Long.parseLong(messageSendReqDTO.getCreateTime()));
        messageMapper.insert(message);

        // 新增会话id下的消息列表
        MessageDTO messageDTO = new MessageDTO();
        BeanUtils.copyProperties(message,messageDTO);
        messageDTO.setMessageId(String.valueOf(message.getId()));
        chatCacheService.addMessageDOTToCache(message.getSessionId(), messageDTO);

        // 更新详情信息
        SessionStatusDetailDTO sessionDetail = chatCacheService.getSessionDTOByCache(session.getId());
        if  (sessionDetail==null){
            log.error("详情不存在");
            return false;
        }

        // 设置对方未浏览数量
        SessionStatusDetailDTO.UserInfo toUser = sessionDetail.getToUser(messageSendReqDTO.getFromId());
        toUser.setNotVisitedCount(toUser.getNotVisitedCount() + 1);
        sessionDetail.setLastSessionTime(message.getCreateTime());
        sessionDetail.setLastMessageDTO(messageDTO);

        // 如果是卡片类型需要更新houseId
        if (MessageTypeEnum.MESSAGE_CARD.getCode().equals(message.getType())){
            Set<Long> houseIds = sessionDetail.getHouseIds();
            String houseId = JSONObject.parseObject(messageSendReqDTO.getContent()).getString("houseId");
            houseIds.add(Long.parseLong(houseId));
            sessionDetail.setHouseIds(houseIds);
        }

        chatCacheService.cacheSessionDTO(session.getId(),  sessionDetail);

        // 新增用户下的会话列表
        chatCacheService.addUserSessionToCache(session.getUserId1(), session.getId(), sessionDetail.getLastSessionTime());
        chatCacheService.addUserSessionToCache(session.getUserId2(), session.getId(), sessionDetail.getLastSessionTime());

        return true;
    }
}
