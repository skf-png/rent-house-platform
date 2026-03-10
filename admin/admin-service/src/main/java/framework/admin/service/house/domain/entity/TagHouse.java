package framework.admin.service.house.domain.entity;

import framework.core.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: yibo
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class TagHouse extends BaseDO {
    private String tagCode;
    private Long houseId;
}
