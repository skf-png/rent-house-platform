package framework.chat.service.domain.VO;

import framework.admin.api.appuser.domain.VO.AppUserVo;
import lombok.Data;

@Data
public class SessionAddResVO {
    private Long sessionId;

    private AppUserVo loginUser;

    private AppUserVo otherUser;

}
