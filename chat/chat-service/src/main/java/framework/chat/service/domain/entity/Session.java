package framework.chat.service.domain.entity;

import framework.core.entity.BaseDO;
import lombok.Data;

@Data
public class Session extends BaseDO {
    private Long userId1;
    private Long userId2;
}
