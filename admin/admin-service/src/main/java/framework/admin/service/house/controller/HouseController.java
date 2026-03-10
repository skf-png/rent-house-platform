package framework.admin.service.house.controller;

import framework.admin.service.house.domain.DTO.HouseAddOrEditReqDTO;
import framework.admin.service.house.service.HouseService;
import framework.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/house")
public class HouseController {
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
}
