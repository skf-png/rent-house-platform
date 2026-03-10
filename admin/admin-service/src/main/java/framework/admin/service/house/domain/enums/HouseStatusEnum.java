package framework.admin.service.house.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@AllArgsConstructor
public enum HouseStatusEnum {
    UP("上架中"),
    DOWN("已下架"),
    RENTING("出租中"),

            ;

    /**
     * 描述
     */
    private String desc;

    public static HouseStatusEnum getByName(String name) {
        for (HouseStatusEnum houseStatusEnum : HouseStatusEnum.values()) {
            if (houseStatusEnum.name().equalsIgnoreCase(name)) {
                return houseStatusEnum;
            }
        }
        return null;
    }

}
