package framework.admin.service.house.service;


import framework.admin.service.house.domain.DTO.*;
import framework.core.DTO.BasePageDTO;

public interface HouseService {
    Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO);

    HouseDTO detail(Long houseId);

    BasePageDTO<HouseDescDTO> list(HouseListReqDTO houseListReqDTO);

    void editStatus(HouseStatusEditReqDTO houseStatusEditReqDTO);
}
