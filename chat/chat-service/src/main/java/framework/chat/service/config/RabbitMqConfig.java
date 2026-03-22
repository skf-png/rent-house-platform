package framework.chat.service.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "chat_message_exchange";

    /**
     * 声明一个交换机
     */
    @Bean(EXCHANGE_NAME)
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(EXCHANGE_NAME, true, false);
    }

    // 交换机的名称
    public final static String EXCHANGE_NAME_EDIT = "edit_user_exchange";
    @Bean(EXCHANGE_NAME_EDIT)
    public FanoutExchange editUserExchange() {
        return new FanoutExchange(EXCHANGE_NAME_EDIT, true, true);
    }
}
