package framework.admin.service.house.service.strategy;

import framework.admin.service.house.domain.enums.HouseSortEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 排序策略的工厂类
 */
@Slf4j
public class SortStrategyFactory {
    public static ISortStrategy getInstance(String sort) {

        if (StringUtils.isNotEmpty(sort)) {
            if (HouseSortEnum.DISTANCE.name().equalsIgnoreCase(sort)) {
                return DistanceSortStrategy.getInstance();
            } else if (HouseSortEnum.PRICE_ASC.name().equalsIgnoreCase(sort)) {
                return PriceSortStrategy.getInstance(true);
            } else if (HouseSortEnum.PRICE_DESC.name().equalsIgnoreCase(sort)) {
                return PriceSortStrategy.getInstance(false);
            } else {
                log.error("排序策略错误！将使用距离排序");
                return DistanceSortStrategy.getInstance();
            }
        }

        log.error("排序策略为空！将使用距离排序");
        return DistanceSortStrategy.getInstance();
    }
}
