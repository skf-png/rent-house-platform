package framework.admin.service.house.service.filter;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;
import org.springframework.stereotype.Component;

@Component
public class RegionFilter implements IHouseFilter{
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return reqDTO.getRegionId() == null || houseDTO.getRegionId().equals(reqDTO.getRegionId());
    }
}
