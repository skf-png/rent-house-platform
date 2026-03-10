package framework.admin.service.house.service;


import framework.admin.service.house.domain.DTO.HouseAddOrEditReqDTO;

public interface HouseService {
    Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO);
}
