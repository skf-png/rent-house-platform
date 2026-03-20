package framework.chat.service.service.websocket;

import framework.chat.service.config.ServerEncoder;
import framework.chat.service.config.WebSocketConfig;
import framework.chat.service.domain.DTO.MessageSendReqDTO;
import framework.chat.service.domain.DTO.WebSocketDTO;
import framework.chat.service.domain.enums.MessageStatusEnum;
import framework.chat.service.domain.enums.MessageTypeEnum;
import framework.chat.service.domain.enums.WebSocketDataTypeEnum;
import framework.chat.service.domain.enums.WebSocketHeader;
import framework.chat.service.service.SnowflakeIdService;
import framework.chat.service.service.mq.MessageProduce;
import framework.core.utils.JsonUtils;
import framework.domain.ResultCode;
import framework.domain.ServiceException;
import framework.security.domain.DTO.LoginUserDTO;
import framework.security.service.TokenService;
import framework.security.utils.SecurityUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/websocket",
configurator = WebSocketConfig.class, encoders = ServerEncoder.class)
@Component
@Slf4j
public class WebSocketServer {
    private Session session;
    private Long userId;
    private TokenService tokenService;
    private SnowflakeIdService snowflakeIdService;
    private MessageProduce messageProduce;

    private static ApplicationContext applicationContext;

    /**
     * 存放服务区和每个客户端对应的WebSocket对象。
     * 建立连接之后去设值，断开连接之后需要删除
     * 线程安全的哈希表
     */
    private static ConcurrentHashMap<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        try {
            // 手动从 Spring 上下文获取 TokenService Bean
            this.tokenService = applicationContext.getBean(TokenService.class);
            this.snowflakeIdService = applicationContext.getBean(SnowflakeIdService.class);
            this.messageProduce = applicationContext.getBean(MessageProduce.class);

            //1. 获取token
            Map<String, Object> userProperties = session.getUserProperties();

            String token = (String) userProperties.get(WebSocketHeader.TOKEN_KEY);

            //2. 去前缀
            token = SecurityUtil.replaceTokenPrefix(token);

            if (StringUtils.isBlank(token)) {
                throw new ServiceException(ResultCode.INVALID_PARA.getCode(), "token不能为空");
            }

            //3. 解析token
            LoginUserDTO userDTO = tokenService.getLoginUser(token);

            if (userDTO == null || userDTO.getUserId() == null) {
                throw new ServiceException(ResultCode.INVALID_PARA.getCode(), "token有误");
            }

            //4. 设置信息
            this.session = session;
            this.userId = userDTO.getUserId();
            webSocketMap.put(userId, this);
            log.info("用户{}已登录", userId);
        } catch (Exception e) {
            log.error("连接异常，已关闭", e);
            session.close();
        }
    }


    @OnMessage
    public void onMessage(String message) {
        log.info("接收到消息{}", message);

        try {
            //1. json->obj
            WebSocketDTO<?> webSocketDTO = JsonUtils.StringToObject(message, WebSocketDTO.class);

            if (webSocketDTO == null) {
                log.error("消息转换错误{}", message);
                return;
            }

            //2. 处理各种类型数据
            handleMessage(webSocketDTO.getType(), webSocketDTO.getData());

        } catch (Exception e) {
            log.error("消息推送失败！", e);
        }
    }

    private <T> void handleMessage(String type, T message) {
        WebSocketDataTypeEnum typeEnum = WebSocketDataTypeEnum.getByType(type);
        if (null == typeEnum) {
            handleUnknownMessage(type);
            return;
        }
        switch (typeEnum) {
            case TEXT :
                // 处理文本消息(测试)
                handleTextMessage((String)message);
                break;
            case HEART_BEAT:
                // 处理心跳消息
                handleHeartBeatMessage();
                break;
            case CHAT:
                // 处理聊天消息
                handleChatMessage((String)message);
                break;
            default:
                // 处理未知消息
                handleUnknownMessage(type);
                break;
        }
    }

    private void handleChatMessage(String s) {
        try {
            // 反序列化消息
            MessageSendReqDTO sendMessage = JsonUtils.StringToObject(s, MessageSendReqDTO.class);
            if (sendMessage == null) {
                throw new ServiceException("聊天消息格式异常");
            }
            // 设置属性
            sendMessage.setMessageId(snowflakeIdService.nextId());
            sendMessage.setStatus(MessageStatusEnum.MESSAGE_UNREAD.getCode());
            sendMessage.setVisited(MessageStatusEnum.MESSAGE_NOT_VISITED.getCode());

            // 广播消息
            messageProduce.sendMessage(sendMessage);

        } catch (Exception e) {
            log.error("信息处理错误！{}", s, e);
        }
    }

    private void handleHeartBeatMessage() {
        // 对应心跳消息来说，接收到谁的Ping 就返回给谁Pong
        WebSocketDTO<String> webSocketDTO = new WebSocketDTO<>(
                WebSocketDataTypeEnum.HEART_BEAT.getType(), "pong");
        sendMessage(webSocketDTO);
    }

    private void handleTextMessage(String s) {
        try {
            Thread.sleep(3000);
            String message = "服务端：" + s;
            sendMessage(new WebSocketDTO<>(WebSocketDataTypeEnum.TEXT.getType(), message));
        } catch (Exception e) {
            log.error("处理文本消息异常！", e);
        }

    }

    private void handleUnknownMessage(String type) {
        log.error("未知的消息类型 type{}", type);
    }

    /**
     * 给当前连接会话推送消息
     *
     * @param webSocketDTO
     */
    private void sendMessage(WebSocketDTO<?> webSocketDTO) {
        try {
            this.session.getBasicRemote().sendObject(webSocketDTO.getData());
        } catch (Exception e) {
            log.error("ws 消息推送失败，webSocketDTO:{}",
                    JsonUtils.ObjectToString(webSocketDTO), e);
        }

    }

    /**
     * 给指定的用户推送消息
     */
    public static void sendMessage(Long userId, WebSocketDTO<?> webSocketDTO) {
        // 不在自己服务器的就丢弃掉
        if (!webSocketMap.containsKey(userId)) {
            return;
        }

        webSocketMap.get(userId).sendMessage(webSocketDTO);
        log.info("消息推送成功{}", webSocketDTO.toString());
    }

    @OnClose
    public void onClose() {
        // 删除状态
        if (userId != null && webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
        }
        log.info("用户{}连接已关闭", userId);
        this.session = null;
        this.userId = null;
    }

    @OnError
    public void onError(Throwable throwable) {
        log.error("WebSocketServer连接失败");
    }

}
