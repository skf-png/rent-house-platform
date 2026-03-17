package framework.portal.house.service.impl;

import framework.admin.api.house.domain.VO.HouseDetailVO;
import framework.admin.api.house.feign.HouseFeignClient;
import framework.domain.R;
import framework.domain.ResultCode;
import framework.portal.house.domain.VO.HouseDataVO;
import framework.portal.house.service.HouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HouseServiceImpl implements HouseService {
    @Autowired
    private HouseFeignClient houseFeignClient;

    @Override
    public HouseDataVO houseDetail(Long houseId) {
        if (null == houseId) {
            return null;
        }

        // 调用feign接口查询房源数据
        R<HouseDetailVO> r = houseFeignClient.detail(houseId);
        if (null == r
                || r.getCode() != ResultCode.SUCCESS.getCode()
                || null == r.getData()) {
            log.error("查询房源详情失败！");
            return null;
        }

        HouseDataVO houseDataVO = new HouseDataVO();
        BeanUtils.copyProperties(r.getData(), houseDataVO);

        return houseDataVO;
    }
}
