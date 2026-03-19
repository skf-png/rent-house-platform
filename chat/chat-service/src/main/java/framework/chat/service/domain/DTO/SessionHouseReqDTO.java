package framework.chat.service.domain.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionHouseReqDTO {

    @NotNull(message = "会话id不能为空！")
    private Long sessionId;

    @NotNull(message = "房源id不能为空！")
    private Long houseId;
}