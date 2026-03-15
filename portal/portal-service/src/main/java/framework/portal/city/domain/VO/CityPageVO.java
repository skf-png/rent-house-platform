package framework.portal.city.domain.VO;

import framework.portal.homepage.domain.VO.CityDescVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class CityPageVO implements Serializable {

    /**
     * 热门城市列表
     */
    private List<CityDescVO> hotCityList;

    /**
     * a-z 城市列表
     */
    private Map<String, List<CityDescVO>> allCityMap;
}