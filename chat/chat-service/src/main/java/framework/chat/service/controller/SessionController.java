package framework.chat.service.controller;

import framework.chat.service.domain.DTO.SessionAddReqDTO;
import framework.chat.service.domain.DTO.SessionGetReqDTO;
import framework.chat.service.domain.DTO.SessionHouseReqDTO;
import framework.chat.service.domain.DTO.SessionListReqDTO;
import framework.chat.service.domain.VO.SessionAddResVO;
import framework.chat.service.domain.VO.SessionGetResVO;
import framework.chat.service.service.SessionService;
import framework.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/session")
public class SessionController {
    @Autowired
    private SessionService sessionService;

    /**
     * 新建咨询会话
     */
    @PostMapping("/add")
    public R<SessionAddResVO> add(@Validated @RequestBody SessionAddReqDTO sessionAddReqDTO) {
        return R.success(sessionService.add(sessionAddReqDTO));
    }

    /**
     * 查询咨询会话
     */
    @PostMapping("/get")
    public R<SessionGetResVO> get(@Validated @RequestBody SessionGetReqDTO sessionGetReqDTO ) {
        return R.success(sessionService.get(sessionGetReqDTO));
    }

    /**
     * 查询咨询会话列表
     */
    @PostMapping("/list")
    public R<List<SessionGetResVO>> list(@Validated @RequestBody SessionListReqDTO sessionListReqDTO ) {
        return R.success(sessionService.list(sessionListReqDTO));
    }

    /**
     * 查看会话下是否聊过某房源
     *
     * @param sessionHouseReqDTO
     * @return
     */
    @PostMapping("/has_house")
    public R<Boolean> hasHouse(@Validated @RequestBody SessionHouseReqDTO sessionHouseReqDTO) {
        return R.success(sessionService.hasHouse(sessionHouseReqDTO));
    }

}
