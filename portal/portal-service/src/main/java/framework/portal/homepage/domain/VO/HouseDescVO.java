package framework.portal.homepage.domain.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class HouseDescVO implements Serializable {
    private Long houseId;
    private String headImage;
    private String title;
    private String rentType;
    private Double area;
    private String position;
    private String regionName;
    private Double price;
}
