package framework.chat.service.service;

import framework.chat.service.domain.DTO.SessionAddReqDTO;
import framework.chat.service.domain.DTO.SessionGetReqDTO;
import framework.chat.service.domain.DTO.SessionListReqDTO;
import framework.chat.service.domain.VO.SessionAddResVO;
import framework.chat.service.domain.VO.SessionGetResVO;

import java.util.List;

public interface SessionService {
    /**
     * 新建咨询会话
     *
     * @param sessionAddReqDTO
     * @return
     */
    SessionAddResVO add(SessionAddReqDTO sessionAddReqDTO);

    /**
     * 查询咨询会话
     * @param sessionGetReqDTO
     * @return
     */
    SessionGetResVO get(SessionGetReqDTO sessionGetReqDTO);

    /**
     * 获取用户的会话列表
     * @param sessionListReqDTO
     * @return
     */
    List<SessionGetResVO> list(SessionListReqDTO sessionListReqDTO);
}
