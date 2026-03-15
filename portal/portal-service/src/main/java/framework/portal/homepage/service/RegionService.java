package framework.portal.homepage.service;

import framework.portal.homepage.domain.DTO.CityDescDTO;

import java.util.List;

public interface RegionService {
    List<CityDescDTO> regionChildren(Long parentId);
}
