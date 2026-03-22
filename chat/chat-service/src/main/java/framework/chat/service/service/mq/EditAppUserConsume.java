package framework.chat.service.service.mq;

import framework.admin.api.appuser.domain.DTO.AppUserDTO;
import framework.chat.service.config.RabbitMqConfig;
import framework.chat.service.domain.DTO.SessionStatusDetailDTO;
import framework.chat.service.service.ChatCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RabbitListener(
        bindings = {
                @QueueBinding(
                        value = @Queue(
                                value = "edit_user_queue1",
                                durable = "true"
                        ),
                        exchange = @Exchange(value = RabbitMqConfig.EXCHANGE_NAME_EDIT, type = ExchangeTypes.FANOUT, autoDelete = "true"))
        })
public class EditAppUserConsume {
    private final ChatCacheService chatCacheService;

    public EditAppUserConsume(ChatCacheService chatCacheService) {
        this.chatCacheService = chatCacheService;
    }

    @RabbitHandler()
    public void process(AppUserDTO appUserDTO) {
        log.info("接收到用户更改请求user{}", appUserDTO.toString());
        // 获取用户所有涉及的会话
        Set<Long> sessionIds = chatCacheService.getUserSessionByCache(appUserDTO.getUserId());

        // 修改所有的会话详情
        for (Long sessionId : sessionIds) {
            SessionStatusDetailDTO sessionDetail = chatCacheService.getSessionDTOByCache(sessionId);
            SessionStatusDetailDTO.UserInfo fromUser = sessionDetail.getFromUser(appUserDTO.getUserId());
            fromUser.getUser().setAvatar(appUserDTO.getAvatar());
            fromUser.getUser().setNickName(appUserDTO.getNickName());
            chatCacheService.cacheSessionDTO(sessionDetail.getSessionId(), sessionDetail);
        }
    }
}
