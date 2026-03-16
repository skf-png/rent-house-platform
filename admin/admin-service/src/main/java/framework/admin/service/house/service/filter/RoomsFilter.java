package framework.admin.service.house.service.filter;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class RoomsFilter implements IHouseFilter{
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return CollectionUtils.isEmpty(reqDTO.getRooms())
                || reqDTO.getRooms().contains(houseDTO.getRooms());
    }
}
