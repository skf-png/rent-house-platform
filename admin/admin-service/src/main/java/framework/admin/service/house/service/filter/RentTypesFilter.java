package framework.admin.service.house.service.filter;

import cn.hutool.core.collection.CollectionUtil;
import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;
import org.springframework.stereotype.Component;

@Component
public class RentTypesFilter implements IHouseFilter{
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return CollectionUtil.isEmpty(reqDTO.getRentTypes()) ||
                reqDTO.getRentTypes().contains(houseDTO.getRentType());
    }
}
