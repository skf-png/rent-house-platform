package framework.chat.service.domain.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionGetReqDTO {

    @NotNull(message = "用户1 id不能为空！")
    private Long userId1;

    @NotNull(message = "用户2 id不能为空！")
    private Long userId2;

}
