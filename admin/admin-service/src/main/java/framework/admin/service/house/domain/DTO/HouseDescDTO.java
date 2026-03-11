package framework.admin.service.house.domain.DTO;

import lombok.Data;

@Data
public class HouseDescDTO {
    private Long houseId;
    private Long userId;
    private String title;
    private String rentType;
    private Double price;
    private String cityName;
    private String regionName;
    private String communityName;
    private String detailAddress;
    private String status;
    private String rentTimeCode;
}
