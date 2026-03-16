package framework.admin.service.house.service.filter;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;
import framework.admin.service.house.domain.enums.HouseStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class StatusFilter implements IHouseFilter{
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return houseDTO.getStatus().equalsIgnoreCase(HouseStatusEnum.UP.name());
    }
}
