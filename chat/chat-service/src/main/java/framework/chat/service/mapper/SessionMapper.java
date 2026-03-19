package framework.chat.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import framework.chat.service.domain.entity.Session;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionMapper extends BaseMapper<Session> {

}
