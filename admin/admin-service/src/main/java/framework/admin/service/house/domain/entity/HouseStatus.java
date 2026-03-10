package framework.admin.service.house.domain.entity;

import framework.core.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: yibo
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class HouseStatus extends BaseDO {
    private Long houseId;
    private String status;
    // 出租时间码
    private String rentTimeCode;
    // 存毫秒级时间戳
    private Long rentStartTime;
    private Long rentEndTime;
}
