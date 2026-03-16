package framework.admin.service.house.service.strategy;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;

import java.util.List;

public interface ISortStrategy {

    /**
     * 排序
     */
    List<HouseDTO> sort(List<HouseDTO> houseDTOList, SearchHouseListReqDTO reqDTO);

}
