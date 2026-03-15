package framework.portal.homepage.service.impl;

import com.alibaba.nacos.shaded.io.grpc.internal.JsonUtil;
import framework.admin.api.config.domain.DTO.DicDataDTO;
import framework.admin.api.config.feign.DicFeignClient;
import framework.core.utils.BeanCopyUtil;
import framework.core.utils.JsonUtils;
import framework.portal.homepage.domain.DTO.DictDataDTO;
import framework.portal.homepage.service.DictionaryService;
import framework.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DictionaryServiceImpl implements DictionaryService {
    private static final String DICT_TYPE_PREFIX = "applet:dict:type:";
    private static final Long DICT_TYPE_TIMEOUT = 5L;
    private static final String DICT_DATA_PREFIX = "applet:dict:data:";
    private static final Long DICT_DATA_TIMEOUT = 5L;

    @Autowired
    private DicFeignClient dictionaryFeignClient;
    @Autowired
    private RedisService redisService;

    @Override
    public Map<String, List<DictDataDTO>> batchFindDictionaryDataByTypes(List<String> types) {
        Map<String, List<DictDataDTO>> resultMap = new HashMap<>();

        // 从缓存获取
        // type1: [data1, data2...]
        // type2: [data1, data2...]
        List<String> notCacheTypes = new ArrayList<>();
        for (String type : types) {
            List<DictDataDTO> dataDTOList = getCacheList(type);
            if (CollectionUtils.isEmpty(dataDTOList)) {
                notCacheTypes.add(type);
            } else {
                resultMap.put(type, dataDTOList);
            }
        }

        // 全部存在：返回
        if (CollectionUtils.isEmpty(notCacheTypes)) {
            return resultMap;
        }

        // 不存在：feign,
        Map<String, List<DicDataDTO>> map =
                dictionaryFeignClient.getDicDataByTypes(notCacheTypes);
        if (CollectionUtils.isEmpty(map)) {
            log.error("字典类型不存在！ notCacheTypes:{}", JsonUtils.ObjectToString(notCacheTypes));
            // throw new ServiceException();
            return resultMap;
        }

        // 缓存结果
        for (Map.Entry<String, List<DicDataDTO>> entry : map.entrySet()) {
            List<DictDataDTO> dataDTOList = BeanCopyUtil.copyListProperties(entry.getValue(), DictDataDTO::new);
            cacheList(entry.getKey(), dataDTOList);
            resultMap.put(entry.getKey(), dataDTOList);
        }
        return resultMap;


    }

    private void cacheList(String type, List<DictDataDTO> copyListProperties) {
        if (StringUtils.isBlank(type)) {
            return;
        }

        redisService.setCacheObject(
                DICT_TYPE_PREFIX + type,
                JsonUtils.ObjectToString(copyListProperties),
                DICT_TYPE_TIMEOUT, TimeUnit.MINUTES);
    }

    private List<DictDataDTO> getCacheList(String type) {
        if (StringUtils.isBlank(type)) {
            return Arrays.asList();
        }

        String str = redisService.getCacheObject(DICT_TYPE_PREFIX + type, String.class);
        if (StringUtils.isBlank(str)) {
            return Arrays.asList();
        }
        return JsonUtils.StringToList(str, DictDataDTO.class);
    }
}
