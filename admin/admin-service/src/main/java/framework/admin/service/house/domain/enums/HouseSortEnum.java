package framework.admin.service.house.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum HouseSortEnum {
    DISTANCE("距离优先"),
    PRICE_DESC("价格从高到低"),
    PRICE_ASC("价格从低到高"),
    ;

    private String desc;
}
