package framework.portal.city.service;

import framework.portal.city.domain.VO.CityPageVO;

public interface CityService {
    /**
     * 获取热门城市与全城市列表
     *
     * @return
     */
    CityPageVO getCityPage();
}
