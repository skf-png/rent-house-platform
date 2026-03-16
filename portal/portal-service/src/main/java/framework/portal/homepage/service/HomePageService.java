package framework.portal.homepage.service;

import framework.domain.domain.VO.BasePageVO;
import framework.portal.homepage.domain.DTO.HouseListReqDTO;
import framework.portal.homepage.domain.DTO.PullDataListReqDTO;
import framework.portal.homepage.domain.VO.CityDescVO;
import framework.portal.homepage.domain.VO.HouseDescVO;
import framework.portal.homepage.domain.VO.PullDataListVO;

public interface HomePageService {

    CityDescVO getCityDesc(Double lat, Double lng);

    PullDataListVO getPullData(PullDataListReqDTO pullDataListReqDTO);

    BasePageVO<HouseDescVO> houseList(HouseListReqDTO reqDTO);
}
