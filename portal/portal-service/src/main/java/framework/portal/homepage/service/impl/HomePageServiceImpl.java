package framework.portal.homepage.service.impl;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.api.house.domain.VO.HouseDetailVO;
import framework.admin.api.house.feign.HouseFeignClient;
import framework.admin.api.map.domain.DTO.LocationReqDTO;
import framework.admin.api.map.domain.DTO.PlaceSearchReqDTO;
import framework.admin.api.map.domain.VO.RegionCityVo;
import framework.admin.api.map.feign.MapFeignClient;
import framework.core.utils.BeanCopyUtil;
import framework.domain.R;
import framework.domain.ResultCode;
import framework.domain.ServiceException;
import framework.domain.domain.VO.BasePageVO;
import framework.portal.homepage.domain.DTO.CityDescDTO;
import framework.portal.homepage.domain.DTO.DictDataDTO;
import framework.portal.homepage.domain.DTO.HouseListReqDTO;
import framework.portal.homepage.domain.DTO.PullDataListReqDTO;
import framework.portal.homepage.domain.VO.CityDescVO;
import framework.portal.homepage.domain.VO.DictsVO;
import framework.portal.homepage.domain.VO.HouseDescVO;
import framework.portal.homepage.domain.VO.PullDataListVO;
import framework.portal.homepage.service.DictionaryService;
import framework.portal.homepage.service.HomePageService;
import framework.portal.homepage.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class HomePageServiceImpl implements HomePageService {
    @Autowired
    private MapFeignClient mapFeignClient;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private HouseFeignClient houseFeignClient;

    @Override
    public CityDescVO getCityDesc(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return null;
        }
        LocationReqDTO locationReqDTO = new LocationReqDTO();
        locationReqDTO.setLat(lat);
        locationReqDTO.setLng(lng);

        R<RegionCityVo> regionCityVoR = mapFeignClient.locateCityByLocation(locationReqDTO);

        if (regionCityVoR == null || regionCityVoR.getData() == null ||
                regionCityVoR.getCode() != ResultCode.SUCCESS.getCode()) {
            log.error("获取经纬度城市错误lat{} lng{}", lat, lng);
            return null;
        }

        CityDescVO cityDescVO = new CityDescVO();
        BeanCopyUtil.copyProperties(regionCityVoR.getData(), cityDescVO);

        return cityDescVO;
    }

    @Override
    public PullDataListVO getPullData(PullDataListReqDTO pullDataListReqDTO) {
        PullDataListVO pullDataListVO = new PullDataListVO();

        //1. 查询城市下区域列表
        List<CityDescDTO> cityDescDTOS = regionService.regionChildren(pullDataListReqDTO.getCityId());
        pullDataListVO.setRegionList(BeanCopyUtil.copyListProperties(cityDescDTOS, CityDescVO::new));

        //2. 查询字典数据列表
        Map<String, List<DictDataDTO>> dictDataMap = dictionaryService.batchFindDictionaryDataByTypes(pullDataListReqDTO.getDirtTypes());

        Map<String, List<DictsVO>> dictMap = new HashMap<>();
        for (Map.Entry<String, List<DictDataDTO>> entry : dictDataMap.entrySet()) {
            List<DictsVO> list = entry.getValue().stream()
                    .map(dictDataDTO -> {
                        DictsVO dictsVO = new DictsVO();
                        dictsVO.setId(dictDataDTO.getId());
                        dictsVO.setKey(dictDataDTO.getDataKey());
                        dictsVO.setName(dictDataDTO.getValue());
                        return dictsVO;
                    }).collect(Collectors.toList());
            dictMap.put(entry.getKey(), list);
        }
        pullDataListVO.setDictMap(dictMap);

        return pullDataListVO;
    }

    @Override
    public BasePageVO<HouseDescVO> houseList(HouseListReqDTO reqDTO) {
        // 1. 查询数据
        SearchHouseListReqDTO searchHouseListReqDTO = new SearchHouseListReqDTO();
        BeanCopyUtil.copyProperties(reqDTO, searchHouseListReqDTO);
        R<BasePageVO<HouseDetailVO>> houseDatas = houseFeignClient.searchList(searchHouseListReqDTO);

        if (houseDatas == null || houseDatas.getData() == null
        ||  houseDatas.getCode() != ResultCode.SUCCESS.getCode()) {
            log.error("房源查询错误");
            throw new ServiceException("房源查询错误");
        }

        BasePageVO<HouseDescVO> basePageVO = new BasePageVO<>();
        basePageVO.setTotals(houseDatas.getData().getTotals());
        basePageVO.setTotalPages(houseDatas.getData().getTotalPages());
        basePageVO.setList(convertHouseList(houseDatas.getData().getList()));

        return basePageVO;
    }

    private List<HouseDescVO> convertHouseList(List<HouseDetailVO> list) {
        //1. 构建datakeys
        List<String> datakeys = list.stream().flatMap(houseDetailVO ->
                        Stream.of(houseDetailVO.getRentType(),
                                houseDetailVO.getPosition()))
                .collect(Collectors.toList());

        //2. 查询datakeys
        Map<String, DictDataDTO> dicDataMap = dictionaryService.batchFindDictionaryData(datakeys);

        List<HouseDescVO> res = list.stream().map(house -> {
            HouseDescVO houseDescVO = new HouseDescVO();
            BeanCopyUtil.copyProperties(house, houseDescVO);
            houseDescVO.setRentType(dicDataMap.get(house.getRentType()).getValue());
            houseDescVO.setPosition(dicDataMap.get(house.getPosition()).getValue());
            return houseDescVO;
        }).collect(Collectors.toList());
        return res;
    }
}
