package framework.chat.service.service.mq;

import framework.chat.service.config.RabbitMqConfig;
import framework.chat.service.domain.DTO.MessageSendReqDTO;
import framework.chat.service.service.MessageService;
import framework.domain.ServiceException;
import framework.redis.service.RedissonLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(bindings = {@QueueBinding(value = @Queue(), exchange = @Exchange(value = RabbitMqConfig.EXCHANGE_NAME, type = ExchangeTypes.FANOUT))})
public class MessageStoreConsume {

    private static final String LOCK_KEY = "chat:db:lock";

    @Autowired
    private RedissonLockService redissonLockService;

    @Autowired
    private MessageService messageService;

    @RabbitHandler
    public void handleMessage(MessageSendReqDTO messageSendReqDTO) {

        // 获取分布式锁
        RLock lock = redissonLockService.acquire(LOCK_KEY, -1);

        if (lock == null) {
            return;
        }

        try {
            // 幂等性处理
            // 存在，不处理了
            if (messageService.get(messageSendReqDTO.getMessageId()) != null) {
                return;
            }

            // 不存在，持久化存储
            if (!messageService.add(messageSendReqDTO)) {
                throw new ServiceException("聊天消息存储失败");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 锁释放
            if (lock.isHeldByCurrentThread() && lock.isLocked()) {
                redissonLockService.releaseLock(lock);
            }
        }

    }
}
