package framework.admin.service.house.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import framework.admin.service.house.domain.entity.HouseStatus;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseStatusMapper extends BaseMapper<HouseStatus> {
}
