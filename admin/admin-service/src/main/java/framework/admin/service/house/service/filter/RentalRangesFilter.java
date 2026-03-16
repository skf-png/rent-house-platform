package framework.admin.service.house.service.filter;

import cn.hutool.core.collection.CollectionUtil;
import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 租金范围筛选
 */
@Slf4j
@Component
public class RentalRangesFilter implements IHouseFilter{
    @Override
    public Boolean filter(HouseDTO houseDTO, SearchHouseListReqDTO reqDTO) {
        return CollectionUtil.isEmpty(reqDTO.getRentalRanges())
                || isFilter(houseDTO.getPrice(), reqDTO);
    }

    public boolean isFilter(Double price, SearchHouseListReqDTO reqDTO) {
        if (price == null) {
            return false;
        }

        boolean isPriceInRange = false;
        for (String rentalRange : reqDTO.getRentalRanges()) {
            // 1800
            // [range_1, range_3]
            switch (rentalRange) {
                case "range_1":
                    isPriceInRange = price < 1000;
                    break;
                case "range_2":
                    isPriceInRange = price >= 1000 && price < 1500;
                    break;
                case "range_3":
                    isPriceInRange = price >= 1500 && price < 2000;
                    break;
                case "range_4":
                    isPriceInRange = price >= 2000 && price < 3000;
                    break;
                case "range_5":
                    isPriceInRange = price >= 3000 && price < 5000;
                    break;
                case "range_6":
                    isPriceInRange = price >= 5000;
                    break;
                default:
                    log.error("超出资金筛选范围, rentalRange:{}", rentalRange);
                    break;
            }
            if (isPriceInRange) {
                return true;
            }
        }
        return false;
    }
}
