package framework.chat.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import feign.Client;
import framework.admin.api.appuser.domain.DTO.AppUserDTO;
import framework.admin.api.appuser.domain.VO.AppUserVo;
import framework.admin.api.appuser.feign.AppUserFeignClient;
import framework.chat.service.domain.DTO.*;
import framework.chat.service.domain.VO.MessageVO;
import framework.chat.service.domain.VO.SessionAddResVO;
import framework.chat.service.domain.VO.SessionGetResVO;
import framework.chat.service.domain.entity.Session;
import framework.chat.service.mapper.SessionMapper;
import framework.chat.service.service.ChatCacheService;
import framework.chat.service.service.SessionService;
import framework.domain.R;
import framework.domain.ResultCode;
import framework.domain.ServiceException;
import framework.security.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionServiceImpl implements SessionService {
    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private ChatCacheService chatCacheService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AppUserFeignClient appUserFeignClient;


    @Override
    public SessionAddResVO add(SessionAddReqDTO sessionAddReqDTO) {

        // 获取登录用户的id
        Long loginUserId = tokenService.getLoginUser().getUserId();

        // 校验
        if (sessionAddReqDTO == null || sessionAddReqDTO.getUserId1() == null
                || sessionAddReqDTO.getUserId2() == null) {
            throw new ServiceException(ResultCode.INVALID_PARA);
        }

        // 排序，保证第一个是最小的哪个
        Long userId1 = sessionAddReqDTO.getUserId1();
        Long userId2 = sessionAddReqDTO.getUserId2();

        if (userId2 < userId1) {
            Long tmp = userId1;
            userId1 = userId2;
            userId2 = tmp;
        }

        // 校验会话是否存在
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getUserId1, userId1)
                .eq(Session::getUserId2, userId2));

        // 如果存在，就去查缓存
        if (session != null) {
            SessionStatusDetailDTO sessionDTO = chatCacheService.getSessionDTOByCache(session.getId());
            if (sessionDTO == null) {
                throw new ServiceException("缓存中会话不存在");
            }

            SessionAddResVO sessionAddResVO = new SessionAddResVO();
            sessionAddResVO.setSessionId(sessionDTO.getSessionId());
            sessionAddResVO.setLoginUser(sessionDTO.getFromUser(loginUserId).getUser().convertToVO());
            sessionAddResVO.setOtherUser(sessionDTO.getToUser(loginUserId).getUser().convertToVO());

            return sessionAddResVO;
        }

        // 不存在就新建一个会话
        Session insertSession = new Session();
        insertSession.setUserId1(userId1);
        insertSession.setUserId2(userId2);
        sessionMapper.insert(insertSession);

        // 获取用户
        List<Long> ids = List.of(userId1, userId2);
        R<List<AppUserVo>> listUserVo = appUserFeignClient.list(ids);

        if (listUserVo == null || listUserVo.getData() == null
        || listUserVo.getCode() != ResultCode.SUCCESS.getCode()
        || listUserVo.getData().size() != ids.size()) {
            throw new ServiceException("用户不存在！");
        }

        // 用户转成map类型
        Map<Long, AppUserDTO> userMap = listUserVo.getData().stream()
                .map(appUserVo -> {
                    AppUserDTO appUserDTO = new AppUserDTO();
                    BeanUtils.copyProperties(appUserVo, appUserDTO);
                    return appUserDTO;
                }).collect(Collectors.toMap(AppUserDTO::getUserId, Function.identity()));

        // 新建后添加详情，缓存redis
        SessionStatusDetailDTO sessionDetailDTO = new SessionStatusDetailDTO();
        sessionDetailDTO.setSessionId(insertSession.getId());

        SessionStatusDetailDTO.UserInfo userInfo1 = new SessionStatusDetailDTO.UserInfo();
        userInfo1.setUser(userMap.get(userId1));
        sessionDetailDTO.setUser1(userInfo1);

        SessionStatusDetailDTO.UserInfo userInfo2 = new SessionStatusDetailDTO.UserInfo();
        userInfo2.setUser(userMap.get(userId2));
        sessionDetailDTO.setUser2(userInfo2);

        chatCacheService.cacheSessionDTO(insertSession.getId(), sessionDetailDTO);

        // 构建返回结果
        SessionAddResVO sessionAddResVO = new SessionAddResVO();
        sessionAddResVO.setSessionId(insertSession.getId());
        sessionAddResVO.setLoginUser(sessionDetailDTO.getFromUser(loginUserId).getUser().convertToVO());
        sessionAddResVO.setOtherUser(sessionDetailDTO.getToUser(loginUserId).getUser().convertToVO());

        return sessionAddResVO;
    }

    @Override
    public SessionGetResVO get(SessionGetReqDTO sessionGetReqDTO) {
        SessionGetResVO res = new SessionGetResVO();

        // 获取登录用户的id
        Long loginUserId = tokenService.getLoginUser().getUserId();

        // 校验
        if (sessionGetReqDTO == null || sessionGetReqDTO.getUserId1() == null
                || sessionGetReqDTO.getUserId2() == null) {
            throw new ServiceException(ResultCode.INVALID_PARA);
        }

        // 排序，保证第一个是最小的哪个
        Long userId1 = sessionGetReqDTO.getUserId1();
        Long userId2 = sessionGetReqDTO.getUserId2();

        if (userId2 < userId1) {
            Long tmp = userId1;
            userId1 = userId2;
            userId2 = tmp;
        }

        // 校验会话是否存在
        Session session = sessionMapper.selectOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getUserId1, userId1)
                .eq(Session::getUserId2, userId2));

        // 不存在返回空
        if (session == null) {
            return res;
        }

        // 存在查询缓存
        SessionStatusDetailDTO cacheSession = chatCacheService.getSessionDTOByCache(session.getId());

        if (cacheSession == null) {
            throw new ServiceException("缓存会话不存在");
        }

        // 返回结果
        res.setSessionId(cacheSession.getSessionId());
        if (cacheSession.getLastSessionTime() != null) {
            res.setLastSessionTime(cacheSession.getLastSessionTime());
        }
        if (cacheSession.getLastMessageDTO() != null) {
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(cacheSession.getLastMessageDTO(), messageVO);
            res.setLastMessageVO(messageVO);
        }

        res.setNotVisitedCount(cacheSession.getFromUser(loginUserId).getNotVisitedCount());
        res.setOtherUser(cacheSession.getToUser(loginUserId).getUser().convertToVO());
        return res;
    }

    @Override
    public List<SessionGetResVO> list(SessionListReqDTO sessionListReqDTO) {
        Long loginUserId = tokenService.getLoginUser().getUserId();

        // 查询用户的会话
        Set<Long> userSession = chatCacheService.getUserSessionByCache(loginUserId);

        if (CollectionUtils.isEmpty(userSession)) {
            return Collections.emptyList();
        }

        // 根据会话id查询缓存
        return userSession.stream()
                .map(session->chatCacheService.getSessionDTOByCache(session))
                .filter(session->session!= null && session.getLastMessageDTO() != null)
                .map(sessionDTO->{
                    SessionGetResVO sessionGetResVO = new SessionGetResVO();
                    sessionGetResVO.setSessionId(sessionDTO.getSessionId());
                    MessageVO lastMessageVO = new MessageVO();
                    BeanUtils.copyProperties(sessionDTO.getLastMessageDTO(), lastMessageVO);
                    sessionGetResVO.setLastMessageVO(lastMessageVO);
                    sessionGetResVO.setLastSessionTime(sessionDTO.getLastSessionTime());
                    sessionGetResVO.setNotVisitedCount(
                            sessionDTO.getFromUser(loginUserId).getNotVisitedCount());
                    sessionGetResVO.setOtherUser(
                            sessionDTO.getToUser(loginUserId).getUser().convertToVO());
                    return sessionGetResVO;
                }).collect(Collectors.toList());
    }

    @Override
    public Boolean hasHouse(SessionHouseReqDTO sessionHouseReqDTO) {

        // 校验参数
        if (sessionHouseReqDTO == null || sessionHouseReqDTO.getHouseId() == null
        || sessionHouseReqDTO.getSessionId() == null) {
            return false;
        }

        // 获取缓存数据
        SessionStatusDetailDTO sessionDetail = chatCacheService.getSessionDTOByCache(sessionHouseReqDTO.getSessionId());
        if (sessionDetail == null) {
            throw new ServiceException("会话id有误，不存在其会话信息！");
        }

        // 获取房源id
        Set<Long> houseIds = sessionDetail.getHouseIds();
        if (CollectionUtils.isEmpty(houseIds)) {
            return false;
        }

        // 判断返回
        return houseIds.contains(sessionHouseReqDTO.getHouseId());
    }
}
