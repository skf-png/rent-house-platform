package framework.admin.api.house.feign;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.api.house.domain.VO.HouseDetailVO;
import framework.domain.R;
import framework.domain.domain.VO.BasePageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "houseFeignClient", value = "admin", path = "/house")
public interface HouseFeignClient {
    /**
     * 查询房源列表，支持筛选、排序、翻页
     */
    @PostMapping("/list/search")
    R<BasePageVO<HouseDetailVO>> searchList(@Validated @RequestBody SearchHouseListReqDTO searchHouseListReqDTO);
}
