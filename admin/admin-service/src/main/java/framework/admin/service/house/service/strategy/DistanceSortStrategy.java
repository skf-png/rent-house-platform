package framework.admin.service.house.service.strategy;


import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DistanceSortStrategy implements ISortStrategy{
    //使用单例模式
    private static DistanceSortStrategy INSTANCE = new DistanceSortStrategy();

    private DistanceSortStrategy(){}

    public static DistanceSortStrategy getInstance(){
        return INSTANCE;
    }

    @Override
    public List<HouseDTO> sort(List<HouseDTO> houseDTOList, SearchHouseListReqDTO reqDTO) {
        return houseDTOList.stream()
                .sorted(Comparator.comparingDouble(
                        houseDTO->
                            houseDTO.calculateDistance(reqDTO.getLatitude(), reqDTO.getLongitude()))
                ).collect(Collectors.toList());
    }
}
