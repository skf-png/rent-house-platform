package framework.admin.service.house.service.mq;

import framework.admin.api.appuser.domain.DTO.AppUserDTO;
import framework.admin.service.house.service.HouseService;
import framework.admin.service.user.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import framework.rabbitwork.config.RabbitMqCommonConfig;


import java.util.List;

@Component
@Slf4j
@RabbitListener(
        bindings = {
        @QueueBinding(
                value = @Queue(
                value = "edit_user_queue",
                durable = "true"
        ),
        exchange = @Exchange(value = RabbitConfig.EXCHANGE_NAME, type = ExchangeTypes.FANOUT))
})
public class EditAppUserMessageReceiver {
    @Autowired
    private HouseService  houseService;

    @RabbitHandler()
    public void process(AppUserDTO appUserDTO) {
        //1. 参数校验
        if (appUserDTO == null || appUserDTO.getUserId() == null) {
            log.error("转发信息错误！appUserDTO{}", appUserDTO);
            return;
        }

        log.info("接收到修改信息{}", appUserDTO);

        try {
            //2. 获取用户下的所有房源
            List<Long> houseIds = houseService.listByUserId(appUserDTO.getUserId());

            //3. 重新缓存所有house
            for (Long houseId : houseIds) {
                houseService.cacheHouse(houseId);
            }

        } catch (Exception e) {
            log.error("缓存错误！appUserDTO{}", appUserDTO, e);
        }
    }
}
