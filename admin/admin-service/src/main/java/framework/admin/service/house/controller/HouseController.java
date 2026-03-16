package framework.admin.service.house.controller;

import framework.admin.api.house.domain.DTO.SearchHouseListReqDTO;
import framework.admin.api.house.domain.VO.HouseDetailVO;
import framework.admin.api.house.feign.HouseFeignClient;
import framework.admin.service.house.domain.DTO.*;
import framework.admin.service.house.domain.VO.HouseVO;
import framework.admin.service.house.service.HouseService;
import framework.core.DTO.BasePageDTO;
import framework.core.utils.BeanCopyUtil;
import framework.domain.R;
import framework.domain.domain.VO.BasePageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/house")
@Slf4j
public class HouseController implements HouseFeignClient {
    @Autowired
    private HouseService houseService;

    /**
     * 新增或编辑房源
     */
    @PostMapping("/add_edit")
    public R<Long> addOrEdit(@Validated @RequestBody HouseAddOrEditReqDTO houseAddOrEditReqDTO) {
        Long houseId = houseService.addOrEdit(houseAddOrEditReqDTO);
        return R.success(houseId);
    }

    /**
     * 查询房源详情（带缓存）
     */
    @GetMapping("/detail")
    public R<HouseDetailVO> detail(Long houseId) {
        HouseDTO houseDTO = houseService.detail(houseId);
        if (null == houseDTO) {
            log.warn("要查询的房源不存在，houseId:{}", houseId);
            return R.fail("房源详情不存在！");
        }
        return R.success(houseDTO.convertToVO());
    }

    /**
     * 根据条件查询房源
     * @param houseListReqDTO
     * @return
     */
    @PostMapping("/list")
    public R<BasePageVO<HouseVO>> list(@Validated @RequestBody HouseListReqDTO houseListReqDTO) {
        BasePageDTO<HouseDescDTO> houseDescList = houseService.list(houseListReqDTO);
        BasePageVO<HouseVO> result = new BasePageVO<>();
        BeanUtils.copyProperties(houseDescList, result);
        return R.success(result);
    }

    /**
     * 更新房源状态
     */
    @PostMapping("/status/edit")
    public R<?> editStatus(@Validated @RequestBody HouseStatusEditReqDTO houseStatusEditReqDTO) {
        houseService.editStatus(houseStatusEditReqDTO);
        return R.success();
    }

    /**
     * 刷新所有缓存
     */
    /**
     * 刷新房源缓存
     **/
    @GetMapping("/refresh")
    public R<Void> refreshHouseIds() {
        houseService.refreshHouseIds();
        return R.success();
    }

    @Override
    public R<BasePageVO<HouseDetailVO>> searchList(SearchHouseListReqDTO searchHouseListReqDTO) {
        BasePageVO<HouseDetailVO> result = new BasePageVO<>();
        BasePageDTO<HouseDTO> searchDTO =  houseService.searchList(searchHouseListReqDTO);
        result.setTotals(searchDTO.getTotals());
        result.setTotalPages(searchDTO.getTotalPages());
        result.setList(BeanCopyUtil.copyListProperties(searchDTO.getList(), HouseDetailVO::new));
        return R.success(result);
    }
}
