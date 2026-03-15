package framework.portal.homepage.service.impl;

import framework.admin.api.map.domain.DTO.LocationReqDTO;
import framework.admin.api.map.domain.DTO.PlaceSearchReqDTO;
import framework.admin.api.map.domain.VO.RegionCityVo;
import framework.admin.api.map.feign.MapFeignClient;
import framework.core.utils.BeanCopyUtil;
import framework.domain.R;
import framework.domain.ResultCode;
import framework.portal.homepage.domain.DTO.CityDescDTO;
import framework.portal.homepage.domain.DTO.DictDataDTO;
import framework.portal.homepage.domain.DTO.PullDataListReqDTO;
import framework.portal.homepage.domain.VO.CityDescVO;
import framework.portal.homepage.domain.VO.DictsVO;
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

@Service
@Slf4j
public class HomePageServiceImpl implements HomePageService {
    @Autowired
    private MapFeignClient mapFeignClient;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private RegionService regionService;

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
}
