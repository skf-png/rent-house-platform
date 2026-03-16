package framework.admin.service.house.service.strategy;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PriceSortStrategy implements ISortStrategy {
    // 单例模式
    private final boolean asc;
    public PriceSortStrategy(boolean asc) {
        this.asc = asc;
    }
    private final static PriceSortStrategy ASC_INSTANCE = new PriceSortStrategy(true);
    private final static PriceSortStrategy DESC_INSTANCE = new PriceSortStrategy(false);

    public static PriceSortStrategy getInstance(boolean asc) {
        return asc ? ASC_INSTANCE : DESC_INSTANCE;
    }

    //排序
    @Override
    public List<HouseDTO> sort(List<HouseDTO> houseDTOList, SearchHouseListReqDTO reqDTO) {
        if (asc) {
            return houseDTOList.stream()
                    .sorted(Comparator.comparingDouble(HouseDTO::getPrice))
                    .collect(Collectors.toList());
        } else {
            return houseDTOList.stream()
                    .sorted(Comparator.comparingDouble(HouseDTO::getPrice).reversed())
                    .collect(Collectors.toList());
        }
    }
}
