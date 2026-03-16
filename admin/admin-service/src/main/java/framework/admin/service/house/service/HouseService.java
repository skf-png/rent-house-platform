package framework.admin.service.house.service;


import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.*;
import framework.core.DTO.BasePageDTO;

import java.util.List;

public interface HouseService {
    Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO);

    HouseDTO detail(Long houseId);

    BasePageDTO<HouseDescDTO> list(HouseListReqDTO houseListReqDTO);

    void editStatus(HouseStatusEditReqDTO houseStatusEditReqDTO);

    List<Long>  listByUserId(Long userId);

    void cacheHouse(Long id);

    void refreshHouseIds();

    BasePageDTO<HouseDTO> searchList(SearchHouseListReqDTO searchHouseListReqDTO);
}
