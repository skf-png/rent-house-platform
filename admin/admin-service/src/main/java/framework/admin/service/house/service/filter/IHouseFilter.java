package framework.admin.service.house.service.filter;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;

public interface IHouseFilter {
    /**
     * 过滤房源
     *
     * @param houseDTO
     * @param reqDTO
     * @return
     */
    Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO);
}
