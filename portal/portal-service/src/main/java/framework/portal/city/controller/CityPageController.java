package framework.portal.city.controller;

import framework.domain.R;
import framework.portal.city.domain.VO.CityPageVO;
import framework.portal.city.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/citypage")
public class CityPageController {
    @Autowired
    private CityService cityService;

    /**
     * 查询热门城市与全城市列表
     */
    @GetMapping("/get/nologin")
    public R< CityPageVO> cityPage() {
        return R.success(cityService.getCityPage());
    }
}
