package framework.portal.house.service;

import framework.portal.house.domain.VO.HouseDataVO;

public interface HouseService {
    /**
     * 查询房源详细信息
     *
     * @param houseId
     * @return
     */
    HouseDataVO houseDetail(Long houseId);
}
