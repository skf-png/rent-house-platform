package framework.admin.service.house.domain.DTO;

import framework.domain.domain.DTO.BasePageReqDTO;
import lombok.Data;


@Data
public class HouseListReqDTO extends BasePageReqDTO {

    /**
     * 房源ID
     */
    private Long houseId;

    /**
     * 房源名称
     */
    private String title;

    /**
     * 房源类型
     */
    private String rentType;

    /**
     * 房源状态
     */
    private String status;

    /**
     * 所在城市
     */
    private Long cityId;

    /**
     * 所在小区
     */
    private String communityName;

}
