package framework.admin.service.house.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class HouseStatusEditReqDTO implements Serializable {

    /**
     * 房源Id
     */
    @NotNull(message = "房源Id不能为空！")
    private Long houseId;

    /**
     * 要修改的类型
     */
    @NotBlank(message = "要修改的类型不能为空！")
    private String status;

    /**
     * 出租时长
     */
    private String rentTimeCode;

}
