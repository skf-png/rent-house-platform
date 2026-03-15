package framework.portal.city.service.impl;

import framework.admin.api.map.domain.VO.RegionVO;
import framework.admin.api.map.feign.MapFeignClient;
import framework.core.utils.BeanCopyUtil;
import framework.domain.R;
import framework.domain.ResultCode;
import framework.portal.city.domain.VO.CityPageVO;
import framework.portal.city.service.CityService;
import framework.portal.homepage.domain.VO.CityDescVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class CityServiceImpl implements CityService {
    @Autowired
    private MapFeignClient mapFeignClient;

    @Resource(name = "threadPoolTaskExecutor")
    private Executor threadPlloTaskExecutor;

    @Override
    public CityPageVO getCityPage() {

        CityPageVO cityPageVO = new CityPageVO();

        //1. 获取热门城市
        CompletableFuture<List<CityDescVO>> cityPageFuture = CompletableFuture.supplyAsync(
                this::getHotCity, threadPlloTaskExecutor
        );
        //2. 获取拼音排序
        CompletableFuture<Map<String, List<CityDescVO>>> mapCompletableFuture = CompletableFuture.supplyAsync(
                this::getCityPyMap, threadPlloTaskExecutor
        );

        //3. 异步执行
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(cityPageFuture, mapCompletableFuture);

        //4. 等待
        try {
            completableFuture.get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        //5. 获取结果
        List<CityDescVO> cityDescVOS = cityPageFuture.join();
        Map<String, List<CityDescVO>> listMap = mapCompletableFuture.join();

        //6. 构造返回
        cityPageVO.setAllCityMap(listMap);
        cityPageVO.setHotCityList(cityDescVOS);

        return cityPageVO;
    }

    private List<CityDescVO> getHotCity() {
        List<CityDescVO> list = new ArrayList<>();
        R<List<RegionVO>> cityHotList = mapFeignClient.getCityHotList();

        if (cityHotList == null || cityHotList.getData() == null
        || cityHotList.getCode() != ResultCode.SUCCESS.getCode()) {
            log.error("获取热门城市错误！");
            return list;
        }

        list = BeanCopyUtil.copyListProperties(cityHotList.getData(), CityDescVO::new);
        return list;
    }

    private Map<String, List<CityDescVO>> getCityPyMap() {
        Map<String, List<CityDescVO>> map = new HashMap<>();

        R<Map<String, List<RegionVO>>> cityPyList = mapFeignClient.getCityPyList();

        if (cityPyList == null || cityPyList.getData() == null
        || cityPyList.getCode() != ResultCode.SUCCESS.getCode()) {
            log.error("获取拼音列表错误");
            return map;
        }

        for (Map.Entry<String, List<RegionVO>> entry : cityPyList.getData().entrySet()) {
            List<CityDescVO> cityDescVOS = BeanCopyUtil.copyListProperties(entry.getValue(), CityDescVO::new);
            map.put(entry.getKey(), cityDescVOS);
        }

        return map;
    }


}
