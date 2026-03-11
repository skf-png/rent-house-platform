package framework.admin.service.house.service;


import framework.admin.service.house.domain.DTO.HouseAddOrEditReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;
import framework.admin.service.house.domain.DTO.HouseDescDTO;
import framework.admin.service.house.domain.DTO.HouseListReqDTO;
import framework.core.DTO.BasePageDTO;

public interface HouseService {
    Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO);

    HouseDTO detail(Long houseId);

    BasePageDTO<HouseDescDTO> list(HouseListReqDTO houseListReqDTO);
}
