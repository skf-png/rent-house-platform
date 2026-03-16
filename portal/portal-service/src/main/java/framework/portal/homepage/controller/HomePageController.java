package framework.portal.homepage.controller;

import framework.domain.R;
import framework.domain.domain.VO.BasePageVO;
import framework.portal.homepage.domain.DTO.HouseListReqDTO;
import framework.portal.homepage.domain.DTO.PullDataListReqDTO;
import framework.portal.homepage.domain.VO.CityDescVO;
import framework.portal.homepage.domain.VO.HouseDescVO;
import framework.portal.homepage.domain.VO.PullDataListVO;
import framework.portal.homepage.service.HomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/homepage")
public class HomePageController {

    @Autowired
    private HomePageService homePageService;

    /**
     * 根据经纬度获取城市信息
     */
    @GetMapping("/city_desc/get/nologin")
    public R<CityDescVO> getCityDesc(Double lat, Double lng) {
        return R.success(homePageService.getCityDesc(lat, lng));
    }

    /**
     * 获取下拉筛选数据列表
     */
    @PostMapping("/pull_list/get/nologin")
    public R<PullDataListVO> getPullData(@Validated @RequestBody PullDataListReqDTO pullDataListReqDTO) {
        return R.success(homePageService.getPullData(pullDataListReqDTO));
    }

    /**
     * 查询房源列表
     */
    @PostMapping("/house_list/search/nologin")
    public R<BasePageVO<HouseDescVO>> houseList( @RequestBody HouseListReqDTO reqDTO) {
        System.out.println(reqDTO.toString());
        return R.success(homePageService.houseList(reqDTO));
    }
}
