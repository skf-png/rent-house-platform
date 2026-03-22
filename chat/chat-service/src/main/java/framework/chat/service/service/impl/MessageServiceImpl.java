package framework.chat.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import framework.chat.service.domain.DTO.*;
import framework.chat.service.domain.VO.MessageVO;
import framework.chat.service.domain.entity.Message;
import framework.chat.service.domain.entity.Session;
import framework.chat.service.domain.enums.MessageStatusEnum;
import framework.chat.service.domain.enums.MessageTypeEnum;
import framework.chat.service.mapper.MessageMapper;
import framework.chat.service.mapper.SessionMapper;
import framework.chat.service.service.ChatCacheService;
import framework.chat.service.service.MessageService;
import framework.chat.service.service.SnowflakeIdService;
import framework.domain.ServiceException;
import framework.security.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private TokenService tokenService;

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

    @Override
    public List<MessageVO> list(MessageListReqDTO messageListReqDTO) {
        // 获取消息全集合
        Set<MessageDTO> messageSet = chatCacheService.getMessageDTOSByCache(messageListReqDTO.getSessionId());
        if (messageSet==null){
            return Arrays.asList();
        }

        // 遍历，构造结果
        int cnt = messageListReqDTO.getCount();
        List<MessageVO> res =  new ArrayList<>();
        for (MessageDTO messageDTO : messageSet) {

            // 如果id大于等于该消息id，说明到达了要返回的位置
            if (0 >= messageDTO.getMessageId().compareTo(messageListReqDTO.getLastMessageId())){

                // 如果是本身，判断是否要添加
                if (messageDTO.getMessageId().equals(messageListReqDTO.getLastMessageId())
                && messageListReqDTO.getNeedCurMessage()){

                    MessageVO messageVO = new MessageVO();
                    BeanUtils.copyProperties(messageDTO,messageVO);
                    res.add(messageVO);
                    cnt--;

                } else if (!messageDTO.getMessageId().equals(messageListReqDTO.getLastMessageId())){
                    MessageVO messageVO = new MessageVO();
                    BeanUtils.copyProperties(messageDTO,messageVO);
                    res.add(messageVO);
                    cnt--;
                }
            }

            // cnt够了，就break
            if (cnt <= 0) {
                break;
            }
        }

        // 结果倒序
        Collections.reverse(res);

        return res;
    }

    @Override
    public void batchVisited(MessageVisitedReqDTO messageVisitedReqDTO) {
        // 获取对方的useId
        Long loginUserId = tokenService.getLoginUser().getUserId();
        if (loginUserId == null) {
            throw new ServiceException("token错误！");
        }

        // 获取session
        Session session = sessionMapper.selectById(messageVisitedReqDTO.getSessionId());
        Long otherUserId = loginUserId.equals(session.getUserId1()) ? session.getUserId2() : session.getUserId1();

        // 更新mysql数据库
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getSessionId, session.getId())
                .eq(Message::getFromId, otherUserId)
                .eq(Message::getVisited, MessageStatusEnum.MESSAGE_NOT_VISITED.getCode())
                .set(Message::getVisited, MessageStatusEnum.MESSAGE_VISITED.getCode()));

        // 修改redis的访问状态
        Set<MessageDTO> messageSet = chatCacheService.getMessageDTOSByCache(session.getId());

        for (MessageDTO messageDTO : messageSet) {
            // 登录用户的不修改
            if (messageDTO.getFromId().equals(loginUserId)){
                continue;
            }

            // 如果遇到已读的了，停止遍历
            if (messageDTO.getVisited().equals(MessageStatusEnum.MESSAGE_VISITED.getCode())){
                break;
            }

            // 更新数据
            messageDTO.setVisited(MessageStatusEnum.MESSAGE_VISITED.getCode());
            chatCacheService.removeMessageDTOCache(session.getId(), messageDTO.getMessageId());
            chatCacheService.addMessageDOTToCache(session.getId(), messageDTO);
        }

        // 修改详情（最后一条消息的状态，用户的未浏览数）
        SessionStatusDetailDTO sessionDetail = chatCacheService.getSessionDTOByCache(session.getId());
        SessionStatusDetailDTO.UserInfo fromUser = sessionDetail.getFromUser(loginUserId);
        fromUser.setNotVisitedCount(0);
        sessionDetail.setLastMessageDTO(messageSet.iterator().next());
        chatCacheService.cacheSessionDTO(session.getId(), sessionDetail);
    }

    @Override
    public void batchRead(MessageReadReqDTO reqDTO) {
        // 修改MySql
        List<Long> messageIds = reqDTO.getMessageIds().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .in(Message::getId, messageIds)
                .set(Message::getStatus, MessageStatusEnum.MESSAGE_READ.getCode()));

        // 修改Redis
        Set<MessageDTO> messageDTOS = chatCacheService.getMessageDTOSByCache(reqDTO.getSessionId());
        if (CollectionUtils.isEmpty(messageDTOS)) {
            return;
        }

        int count = reqDTO.getMessageIds().size();
        for (MessageDTO messageDTO : messageDTOS) {
            if (reqDTO.getMessageIds().contains(messageDTO.getMessageId())) {
                messageDTO.setStatus(MessageStatusEnum.MESSAGE_READ.getCode());
                chatCacheService.addMessageDOTToCache(messageDTO.getSessionId(), messageDTO);
                count--;
            }

            if (count <= 0) {
                break;
            }

        }

        // 修改会话详情缓存:
        // 1. 登录用户记录的对方消息未浏览数
        // 2. 最后一条聊天消息（访问状态）
        SessionStatusDetailDTO sessionDTO = chatCacheService.getSessionDTOByCache(reqDTO.getSessionId());
        sessionDTO.setLastMessageDTO(messageDTOS.iterator().next());
        chatCacheService.removeMessageDTOCache(sessionDTO.getSessionId(), sessionDTO.getLastMessageDTO().getMessageId());
        chatCacheService.cacheSessionDTO(sessionDTO.getSessionId(), sessionDTO);
    }
}
