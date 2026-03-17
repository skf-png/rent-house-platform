package framework.portal.house.controller;

import framework.domain.R;
import framework.portal.house.domain.VO.HouseDataVO;
import framework.portal.house.service.HouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/housepage")
public class HousePageController {

    @Autowired
    private HouseService houseService;

    /**
     * C端查询房源详情
     */
    @GetMapping("/get/nologin")
    public R<HouseDataVO> houseDetail(Long houseId) {
        return R.success(houseService.houseDetail(houseId));
    }

}