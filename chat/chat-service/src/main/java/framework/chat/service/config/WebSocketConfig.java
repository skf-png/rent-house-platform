package framework.chat.service.config;

import framework.chat.service.domain.enums.WebSocketHeader;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.List;
import java.util.Map;

@Configuration
public class WebSocketConfig extends ServerEndpointConfig.Configurator {
    /**
     * 这个类注册每个加了 @ServerEndpoint 的 spring bean节点，算是 spring 整合 websocket的一个体现，
     * 没有的话会报404
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 建立握手时，连接之前的操作，可以获取到源信息。
     * @param sec
     * @param request
     * @param response
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request,
                                HandshakeResponse response) {
        //1. 获取头
        Map<String, List<String>> headers = request.getHeaders();

        //2. 获取token
        List<String> token = headers.get(WebSocketHeader.TOKEN_KEY);

        //3. 判断是否存在
        if (token == null) {
            throw new RuntimeException("token is null");
        }

        //4. 塞入session里面
        sec.getUserProperties().put(WebSocketHeader.TOKEN_KEY, token.get(0));
    }
}
